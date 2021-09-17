package me.Neoblade298.MLMCCustomFoods;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.player.PlayerData;
import com.sucy.skill.api.util.FlagManager;
import com.sucy.skill.api.util.StatusFlag;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

public class MLMCCustomFoodsMain extends JavaPlugin implements Listener {
	static MLMCCustomFoodsMain main;
	FileManager fm;
	HashMap<String, Food> foods = new HashMap<String, Food>();
	HashMap<UUID, HashMap<String, int[]>> effects = new HashMap<UUID, HashMap<String, int[]>>();
	HashMap<UUID, Long> playerCooldowns = new HashMap<UUID, Long>();
	HashMap<UUID, ArrayList<Food>> recentlyUsed = new HashMap<UUID, ArrayList<Food>>();
	boolean isInstance = false;

	public void onEnable() {
		main = this;
		this.fm = new FileManager();
		this.foods = this.fm.loadFoods();
		File instanceFile = new File(getDataFolder(), "instance.yml");
		isInstance = instanceFile.exists();
		Bukkit.getPluginManager().registerEvents(this, this);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				for (Iterator<UUID> uuidIterator = MLMCCustomFoodsMain.this.effects.keySet().iterator(); uuidIterator
						.hasNext();) {
					UUID u = (UUID) uuidIterator.next();
					Player p = Bukkit.getPlayer(u);
					if (p != null) {
						PlayerData data = SkillAPI.getPlayerData(p);
						HashMap<String, int[]> playerAttribs = (HashMap<String, int[]>) MLMCCustomFoodsMain.this.effects
								.get(p.getUniqueId());
						for (Iterator<String> attribIterator = playerAttribs.keySet().iterator(); attribIterator
								.hasNext();) {
							String attrib = (String) attribIterator.next();
							int[] time = (int[]) playerAttribs.get(attrib);
							if (time[0] <= 0) {
								data.addBonusAttributes(attrib, -time[1]);
								attribIterator.remove();
							} else {
								playerAttribs.put(attrib, new int[] { time[0] - 1, time[1] });
							}
						}
					}
				}
			}
		}, 0L, 1L);
	}

	@EventHandler
	public void onPlayerDeathEvent(PlayerDeathEvent e) {
		UUID id = e.getEntity().getUniqueId();
		this.effects.remove(id);
	}

	@EventHandler
	public void onPlayerInteractEvent(PlayerInteractEvent e) {
		final Player p = e.getPlayer();
		if ((!e.getAction().equals(Action.RIGHT_CLICK_AIR)) && (!e.getAction().equals(Action.RIGHT_CLICK_BLOCK))) {
			return;
		}
		if (!e.getHand().equals(EquipmentSlot.HAND)) {
			return;
		}
		if (!SkillAPI.isLoaded(p)) {
			return;
		}
		ItemStack item = p.getInventory().getItemInMainHand();
		if ((!item.hasItemMeta()) || (!item.getItemMeta().hasDisplayName())) {
			return;
		}
		ItemMeta meta = item.getItemMeta();
		boolean quickEat = false;
		if (meta.hasLore()) {
			for (String line : meta.getLore()) {
				if (line.contains("Quick Eat") && !item.getType().equals(Material.PRISMARINE_CRYSTALS)) {
					quickEat = true;
				}
			}
		}

		// Find the food in the inv
		Food food = null;
		if (quickEat) {
			ItemStack[] contents = p.getInventory().getContents();
			for (int i = 0; i < 36 && food == null; i++) {
				ItemStack invItem = contents[i];
				if (invItem != null && invItem.hasItemMeta() && invItem.getItemMeta().hasLore()) {
					ItemMeta invMeta = invItem.getItemMeta();
					for (String name : this.foods.keySet()) {
						food = (Food) this.foods.get(name);
						if (food.canEat(p)) {
							if (name.equalsIgnoreCase(invMeta.getDisplayName())) {
								item = invItem;
								meta = invItem.getItemMeta();
								break;
							}
						}
					}
				}
			}
		} else {
			for (String name : this.foods.keySet()) {
				if (meta.getDisplayName().contains(name)) {
					food = (Food) this.foods.get(name);
					break;
				}
			}
		}

		if (food == null) {
			return;
		}
		if ((!meta.hasLore()) && (!food.getLore().isEmpty())) {
			return;
		}
		if (!food.getLore().isEmpty()) {
			if (!meta.hasLore()) return;
			
			ArrayList<String> flore = food.getLore();
			ArrayList<String> mlore = (ArrayList<String>) meta.getLore();
			
			for (int i = 0; i < flore.size(); i++) {
				String fLine = food.getLore().get(i);
				String mLine = mlore.get(i);
				if (!mLine.contains(fLine)) {
					return;
				}
			}
		}
		if (!isOffCooldown(p) && !food.getName().contains("Chest")) {

			long remainingCooldown = 20000L;
			remainingCooldown -= System.currentTimeMillis()
					- ((Long) this.playerCooldowns.get(p.getUniqueId())).longValue();
			remainingCooldown /= 1000L;
			String message = "&cYou cannot eat for another " + remainingCooldown + " seconds";
			message = message.replaceAll("&", "�");
			p.sendMessage(message);
			return;
		}
		if (!food.canEat(p)) {
			long remainingCooldown = food.cooldown;
			remainingCooldown -= System.currentTimeMillis() - ((Long) food.lastEaten.get(p.getUniqueId())).longValue();
			remainingCooldown /= 1000L;
			String message = "&cYou cannot eat this food for another " + remainingCooldown + " seconds";
			message = message.replaceAll("&", "�");
			p.sendMessage(message);
			return;
		}
		if ((!food.quaffable()) && (p.getFoodLevel() == 20)) {
			return;
		}
		if (!food.getWorlds().contains(p.getWorld().getName())) {
			return;
		}
		if (food.getName().contains("Chest") && isInstance) {
			String message = "&cYou cannot open chests in a boss fight!";
			message = message.replaceAll("&", "�");
			p.sendMessage(message);
			return;
		}

		// Food can be eaten
		boolean isGarnished = false, isSpiced = false;
		double garnishMultiplier = 1, preserveMultiplier = 1, spiceMultiplier = 1;
		for (String line : meta.getLore()) {
			if (line.contains("Garnished")) {
				isGarnished = true;
				String toParse = line.substring(line.indexOf('(') + 1, line.indexOf('x'));
				garnishMultiplier = Double.parseDouble(toParse);
			}
			if (line.contains("Preserved")) {
				String toParse = line.substring(line.indexOf('(') + 1, line.indexOf('x'));
				preserveMultiplier = Double.parseDouble(toParse);
			}
			if (line.contains("Spiced")) {
				isSpiced = true;
				String toParse = line.substring(line.indexOf('(') + 1, line.indexOf('x'));
				spiceMultiplier = Double.parseDouble(toParse);
			}
			if (line.contains("Remedies")) {
				if (line.contains("Remedies stun")) {
					FlagManager.removeFlag(p, StatusFlag.STUN);
				} else if (line.contains("Remedies curse")) {
					FlagManager.removeFlag(p, "curse");
				} else if (line.contains("Remedies root")) {
					FlagManager.removeFlag(p, StatusFlag.ROOT);
				} else if (line.contains("Remedies silence")) {
					FlagManager.removeFlag(p, StatusFlag.SILENCE);
				}
			}
		}
		food.eat(p, preserveMultiplier);
		e.setCancelled(true);

		// Do not add cooldowns for chests
		if (!food.getName().contains("Chest")) {
			this.playerCooldowns.put(p.getUniqueId(), Long.valueOf(System.currentTimeMillis()));
			p.sendMessage(food.getName() + " �7was eaten");
		}
		p.setFoodLevel(Math.min(20, p.getFoodLevel() + food.getHunger()));
		p.setSaturation((float) Math.min(p.getFoodLevel(), food.getSaturation() + p.getSaturation()));
		for (PotionEffect effect : food.getEffect()) {
			p.addPotionEffect(effect);
		}

		// Work on skillapi attributes
		HashMap<String, int[]> playerAttribs;
		if (!this.effects.containsKey(p.getUniqueId())) {
			playerAttribs = new HashMap<String, int[]>();
			this.effects.put(p.getUniqueId(), playerAttribs);
		} else {
			playerAttribs = (HashMap<String, int[]>) this.effects.get(p.getUniqueId());
		}
		PlayerData data = SkillAPI.getPlayerData(p);
		for (AttributeEffect attrib : food.getAttributes()) {
			// If an attribute currently exists, remove it
			int duration = attrib.getDuration();
			int amp = attrib.getAmp();
			if (isGarnished) {
				amp *= garnishMultiplier;
			}
			if (playerAttribs.containsKey(attrib.getName())) {
				int[] oldData = (int[]) playerAttribs.get(attrib.getName());
				data.addBonusAttributes(attrib.getName(), -oldData[1]);
			}
			data.addBonusAttributes(attrib.getName(), amp);
			playerAttribs.put(attrib.getName(), new int[] { duration, amp });
		}

		// Work on health and mana regen
		final Food foodItem = food;
		int health = foodItem.getHealth();
		int mana = foodItem.getMana();
		if (isSpiced) {
			health *= spiceMultiplier;
			mana *= spiceMultiplier;
		}
		final int finalHealth = health;
		final int finalMana = mana;
		if (food.getHealthTime() == 0) {
			if (p.isValid()) {
				p.setHealth(Math.min(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(), p.getHealth() + finalHealth));
			}
		} else {
			BukkitRunnable healthTask = new BukkitRunnable() {
				int rep = foodItem.getHealthTime();

				public void run() {
					if (p.isValid()) {
						this.rep -= 1;
						p.setHealth(Math.min(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(), p.getHealth() + finalHealth));
						if (this.rep <= 0) {
							this.cancel();
						}
					}
				}
			};
			healthTask.runTaskTimer(this, 0, foodItem.getHealthDelay());
		}
		final PlayerData fdata = data;
		if (data.getMainClass().getData().getManaName().contains("MP")) {
			if (food.getManaTime() == 0) {
				if (p.isValid()) {
					fdata.setMana(Math.min(fdata.getMaxMana(), fdata.getMana() + finalMana));
				}
			} else {
				BukkitRunnable manaTask = new BukkitRunnable() {
					int rep = foodItem.getManaTime();

					public void run() {
						if (p.isValid()) {
							this.rep -= 1;
							fdata.setMana(Math.min(fdata.getMaxMana(), fdata.getMana() + finalMana));
							if (this.rep <= 0) {
								this.cancel();
							}
						}
					}
				};
				manaTask.runTaskTimer(this, 0, foodItem.getManaDelay());
			}
		}
		for (Sound sound : food.getSounds()) {
			p.getWorld().playSound(p.getEyeLocation(), sound, 1.0F, 1.0F);
		}
		food.executeCommands(p);
		item.setAmount(item.getAmount() - 1);
		
		// Add recently used if in an instance to cache
		if (isInstance) {
			UUID id = p.getUniqueId();
			if (recentlyUsed.containsKey(id)) {
				recentlyUsed.get(id).add(food);
			}
			else {
				recentlyUsed.put(id, new ArrayList<Food>());
				recentlyUsed.get(id).add(food);
			}
		}
	}

	@EventHandler
	public void onPlayerQuitEvent(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		if (this.effects.containsKey(p.getUniqueId())) {
			PlayerData data = SkillAPI.getPlayerData(p);
			HashMap<String, int[]> playerAttribs = this.effects.get(p.getUniqueId());
			for (String s : playerAttribs.keySet()) {
				data.addBonusAttributes(s, ((int[]) playerAttribs.get(s))[1]);
			}
			this.effects.remove(p.getUniqueId());
		}
	}
	
	@EventHandler
	public void onPlayerJoinEvent(PlayerLoginEvent e) {
		Player p = e.getPlayer();
		if (isInstance) {
			UUID id = p.getUniqueId();
			if (recentlyUsed.containsKey(id)) {
				clearPlayerCooldowns(recentlyUsed.get(id), id);
				recentlyUsed.clear();
			}
			else {
				recentlyUsed.put(id, new ArrayList<Food>());
			}
		}
	}
	
	@EventHandler
	public void onPlayerPlace(BlockPlaceEvent e) {
		ItemStack item = e.getItemInHand();
		if (item.hasItemMeta() && item.getItemMeta().hasLore() && item.getItemMeta().getLore().get(0).contains("Potential Rewards")) {
			e.setCancelled(true);
		}
	}

	public boolean isOffCooldown(Player p) {
		if (this.playerCooldowns.containsKey(p.getUniqueId())) {
			return System.currentTimeMillis() - ((Long) this.playerCooldowns.get(p.getUniqueId())).longValue() > 20000L;
		}
		return true;
	}

	public static MLMCCustomFoodsMain getMain() {
		return main;
	}
	
	private void clearPlayerCooldowns(ArrayList<Food> foods, UUID id) {
		for (Food food : foods) {
			food.lastEaten.remove(id);
		}
	}
}

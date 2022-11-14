package me.neoblade298.neosapiaddons;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.ImmutableList;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.SkillPlugin;
import com.sucy.skill.api.enums.ExpSource;
import com.sucy.skill.api.event.PlayerAccountChangeEvent;
import com.sucy.skill.api.event.PlayerLoadCompleteEvent;
import com.sucy.skill.api.event.SkillHealEvent;
import com.sucy.skill.api.player.PlayerClass;
import com.sucy.skill.api.player.PlayerData;
import com.sucy.skill.api.skills.Skill;
import com.sucy.skill.api.util.FlagManager;
import com.sucy.skill.dynamic.custom.CustomEffectComponent;
import com.sucy.skill.dynamic.trigger.Trigger;

import me.neoblade298.neosapiaddons.conditions.AbsorptionCondition;
import me.neoblade298.neosapiaddons.conditions.AttackChargeCondition;
import me.neoblade298.neosapiaddons.conditions.BlockingCondition;
import me.neoblade298.neosapiaddons.conditions.ManaNameCondition;
import me.neoblade298.neosapiaddons.conditions.StringCondition;
import me.neoblade298.neosapiaddons.mechanics.AddShieldsMechanic;
import me.neoblade298.neosapiaddons.mechanics.IncreasePotionMechanic;
import me.neoblade298.neosapiaddons.mechanics.SpawnMythicmobMechanic;
import me.neoblade298.neosapiaddons.mechanics.SudoSkillMechanic;
import me.neoblade298.neosapiaddons.mechanics.ValueAttackSpeedMechanic;
import me.neoblade298.neosapiaddons.mechanics.ValueMaxMechanic;
import me.neoblade298.neosapiaddons.mechanics.ValueSkillLevelMechanic;

@SuppressWarnings("deprecation")
public class SAPIAddons extends JavaPlugin implements Listener, SkillPlugin {
	private HungerController hc;
	public static HashMap<Integer, PointSet> skillPoints = new HashMap<Integer, PointSet>();
	public static HashMap<Integer, PointSet> attrPoints = new HashMap<Integer, PointSet>();
	public static boolean debug = false;
	public static SAPIAddons inst;
	
	
	public void onEnable() {
		super.onEnable();
		hc = new HungerController(this);
		Bukkit.getServer().getLogger().info("NeoSAPIAddons Enabled");
		getServer().getPluginManager().registerEvents(this, this);
		getServer().getPluginManager().registerEvents(hc, this);
		loadConfig();

		getCommand("nsapi").setExecutor(new Commands(this));
		inst = this;
	}
	
	public void loadConfig() {
		File file = new File(getDataFolder(), "config.yml");

		// Save config if doesn't exist
		if (!file.exists()) {
			saveResource("config.yml", false);
		}
		ConfigurationSection cfg = YamlConfiguration.loadConfiguration(file);

		// Skill points
		ConfigurationSection skillCfg = cfg.getConfigurationSection("skill-points");
		for (String spset : skillCfg.getKeys(false)) {
			ConfigurationSection spCfg = skillCfg.getConfigurationSection(spset);
			skillPoints.put(spCfg.getInt("max-lvl"), new PointSet(spCfg.getInt("base-points"), spCfg.getInt("points-per-lvl"), spCfg.getInt("base-lvl")));
		}

		// Attribute points
		ConfigurationSection attrCfg = cfg.getConfigurationSection("attribute-points");
		for (String apset : attrCfg.getKeys(false)) {
			ConfigurationSection apCfg = attrCfg.getConfigurationSection(apset);
			attrPoints.put(apCfg.getInt("max-lvl"), new PointSet(apCfg.getInt("base-points"), apCfg.getInt("points-per-lvl"), apCfg.getInt("base-lvl")));
		}
	}

	public void onDisable() {
		Bukkit.getServer().getLogger().info("NeoSAPIAddons Disabled");
		super.onDisable();
	}

	// Implement curse status properly
	@EventHandler
	public void onHeal(SkillHealEvent e) {
		LivingEntity target = e.getTarget();
		if (FlagManager.hasFlag(target, "curse") && !e.ignoresCurse()) {
			e.setCancelled(true);
			target.damage(e.getAmount());
		}
	}

	// Stop nether portals from working in a skillapi area
	@EventHandler
	public void onPlayerPortal(PlayerPortalEvent e) {
		if (SkillAPI.getSettings().isWorldEnabled(e.getFrom().getWorld())) {
			e.setCancelled(true);
		}
	}
	
	// Stop players from blocking mythicmobs
	@EventHandler(ignoreCancelled=false)
	public void onDamage(EntityDamageByEntityEvent e) {
		if (e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			
			// Make damage go through shields
			if (p.isBlocking()) {
				String world = p.getWorld().getName();
				if (world.equals("Argyll") || world.equals("ClassPVP") || world.equals("Dev")) {
					e.setDamage(DamageModifier.BLOCKING, e.getDamage(DamageModifier.BLOCKING) * 0.2);
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onAnyDamage(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			double damage = Math.max(0, ShieldManager.useShields(p, e.getDamage()));
			e.setDamage(damage);
			new BukkitRunnable() {
				public void run() {
					ShieldManager.updatePlayerShields(p);
				}
			}.runTask(SAPIAddons.inst);
		}
	}
	
	@EventHandler
	public void onEat(PlayerItemConsumeEvent e) {
		if (e.getPlayer().getWorld().getName().equalsIgnoreCase("ClassPVP") ||
				e.getPlayer().getWorld().getName().equalsIgnoreCase("Argyll")) {
			if (e.getItem().getType().equals(Material.GOLDEN_APPLE) ||
					e.getItem().getType().equals(Material.ENCHANTED_GOLDEN_APPLE)) {
				e.setCancelled(true);
				e.getPlayer().sendMessage("§4[§c§lMLMC§4] §cGolden apples are restricted in the quest world.");
				return;
			}
		}
	}
	
	public static void correctStats(PlayerData data) {
		Player p = data.getPlayer();
		if (data == null || data.getClass("class") == null) return;
		int max = data.getClass("class").getData().getMaxLevel();
		
		PointSet setSP = skillPoints.get(max);
		PointSet setAP = attrPoints.get(max);
		
		int expectedSP = setSP.getBasePoints() + setSP.getPointsPerLvl() * (data.getClass("class").getLevel() - setSP.getBaseLvl());
		int expectedAP = setAP.getBasePoints() + setAP.getPointsPerLvl() * (data.getClass("class").getLevel() - setAP.getBaseLvl());
		if (debug) {
			Bukkit.getLogger().log(Level.INFO, "SAPIAddons update " + data.getPlayerName() +
					" expected SP: " + expectedSP + ", expected AP: " + expectedAP);
		}
		
		int currSP = data.getInvestedSkillPoints() + data.getClass("class").getPoints();
		if (debug) {
			Bukkit.getLogger().log(Level.INFO, "SAPIAddons update " + data.getPlayerName() +
					" current SP: " + data.getInvestedSkillPoints() + " + " + data.getClass("class").getPoints() + " = " + currSP);
		}
		int currAP = data.getAttributePoints();
		HashMap<String, Integer> invested = data.getInvestedAttributes();
		for (String attr : invested.keySet()) {
			currAP += invested.get(attr);
			if (debug) {
				Bukkit.getLogger().log(Level.INFO, "SAPIAddons update " + data.getPlayerName() +
						" current AP + " + attr + ", " + invested.get(attr) + ": " + currAP);
			}
		}
		
		if (currSP > expectedSP) {
			PlayerClass pc = data.getClass("class");
			int diff = currSP - expectedSP - pc.getPoints();

			// If we need to unvest skill points, do it, otherwise just take from unvested points
			if (diff > 0) {
				for (Skill skill : pc.getData().getSkills()) {
					if (diff <= 0) {
						break;
					}
					
					while (diff > 0) {
						int cost = skill.getCost(data.getSkillLevel(skill.getName()) - 1);
						boolean success = data.downgradeSkill(skill);
						if (success) {
							diff -= cost;
						}
						else {
							break;
						}
					}
				}
			}
			pc.setPoints(pc.getPoints() - (currSP - expectedSP));
			p.sendMessage("§cNote: /skills points have been adjusted lower. You may want to double check them.");
		}
		else if (currSP < expectedSP) {
			data.givePoints(expectedSP - currSP, ExpSource.MOB);
			p.sendMessage("§cNote: /skills points have been adjusted higher. You may want to double check them.");
		}
		
		if (currAP > expectedAP) {
			if (data.getAttributePoints() > currAP - expectedAP) {
				data.setAttribPoints(data.getAttributePoints() - (currAP - expectedAP));
			}
			else {
				data.resetAttribs();
				data.setAttribPoints(expectedAP);
			}
			p.sendMessage("§cNote: /attr points have been adjusted lower. You may want to double check them.");
		}
		else if (currAP < expectedAP){
			data.setAttribPoints(data.getAttributePoints() + (expectedAP - currAP));
			p.sendMessage("§cNote: /attr points have been adjusted higher. You may want to double check them.");
		}
	}
	
	@EventHandler
	public void onPlayerLoad(PlayerLoadCompleteEvent e) {
		correctStats(SkillAPI.getPlayerData(e.getPlayer()));
	}
	
	@EventHandler
	public void onPlayerSwitchAccount(PlayerAccountChangeEvent e) {
		correctStats(e.getNewAccount());
	}

	@Override
	public void registerClasses(SkillAPI api) {
		
	}

	@Override
	public void registerSkills(SkillAPI arg0) {

	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public List<Trigger> getTriggers() {
		return ImmutableList.of(
		);
	}
    
    @Override
    public List<CustomEffectComponent> getComponents() {
        return ImmutableList.of(
            new ValueMaxMechanic(),
            new SpawnMythicmobMechanic(),
            new SudoSkillMechanic(),
            new AddShieldsMechanic(),
            new IncreasePotionMechanic(),
            new ValueAttackSpeedMechanic(),
            new ValueSkillLevelMechanic(),
            new AbsorptionCondition(),
            new AttackChargeCondition(),
            new ManaNameCondition(),
            new BlockingCondition(),
            new StringCondition()
        );
    }
    
    public boolean isQuestWorld(World w) {
    	String name = w.getName();
    	return name.equalsIgnoreCase("Argyll") || name.equalsIgnoreCase("Dev") || name.equalsIgnoreCase("ClassPVP");
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onShootCrossbow(EntityShootBowEvent e) {
    	if (e.getEntity() instanceof Player &&
    			e.getBow().getType().equals(Material.CROSSBOW) &&
    			isQuestWorld(e.getEntity().getWorld())) {
        	((Player) e.getEntity()).getInventory().addItem(new ItemStack(Material.ARROW));
    	}
    }
}
package me.neoblade298.neoresearch;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import me.neoblade298.neoresearch.ResearchItem;
import me.neoblade298.neoresearch.inventories.InventoryListeners;
import me.neoblade298.neoresearch.inventories.ResearchInventory;

import com.sucy.skill.api.event.PlayerAttributeLoadEvent;
import com.sucy.skill.api.event.PlayerAttributeUnloadEvent;
import com.sucy.skill.api.event.PlayerLoadCompleteEvent;
import com.sucy.skill.api.event.PlayerSaveEvent;

import de.tr7zw.nbtapi.NBTItem;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.MobManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class Research extends JavaPlugin implements org.bukkit.event.Listener {
	// SQL
	public String url, user, pass;
	public static HashMap<Player, ResearchInventory> viewingInventory = new HashMap<Player, ResearchInventory>();
	public HashMap<String, ResearchItem> researchItems;
	public HashMap<String, ArrayList<ResearchItem>> mobMap;
	public HashMap<String, String> displayNameMap;
	public HashMap<UUID, PlayerStats> playerStats;
	public HashMap<Integer, Integer> toNextLvl;
	public HashMap<UUID, StoredAttributes> playerAttrs;
	public HashMap<String, HashMap<String, Integer>> converter;
	public static ArrayList<String> attrs;
	ArrayList<String> enabledWorlds;
	public HashSet<String> minibosses;
	public Random rand;
	public boolean isInstance;
	
	public HashMap<UUID, Long> lastSave;

	public String broadcast, levelup, discovery;

	public void onEnable() {
		Bukkit.getServer().getLogger().info("NeoResearch Enabled");
		getServer().getPluginManager().registerEvents(this, this);
		getServer().getPluginManager().registerEvents(new InventoryListeners(this), this);
		attrs = new ArrayList<String>(Arrays.asList("str", "dex", "int", "spr", "end"));
		this.getCommand("nr").setExecutor(new Commands(this));
		enabledWorlds = new ArrayList<String>();
		enabledWorlds.add("Dev");
		enabledWorlds.add("ClassPVP");
		enabledWorlds.add("Argyll");

		loadConfig();
	}

	public void onDisable() {
		saveAll();
		org.bukkit.Bukkit.getServer().getLogger().info("NeoResearch Disabled");
		super.onDisable();
	}

	public void loadConfig() {
		File file = new File(getDataFolder(), "config.yml");
		playerStats = new HashMap<UUID, PlayerStats>();
		playerAttrs = new HashMap<UUID, StoredAttributes>();
		converter = new HashMap<String, HashMap<String, Integer>>();
		minibosses = new HashSet<String>();
		lastSave = new HashMap<UUID, Long>();
		rand = new Random();

		// Save config if doesn't exist
		if (!file.exists()) {
			saveResource("config.yml", false);
		}
		ConfigurationSection cfg = YamlConfiguration.loadConfiguration(file);

		// SQL
		ConfigurationSection sql = cfg.getConfigurationSection("sql");
		url = "jdbc:mysql://" + sql.getString("host") + ":" + sql.getString("port") + "/" + 
				sql.getString("db") + sql.getString("flags");
		user = sql.getString("username");
		pass = sql.getString("password");

		// General
		ConfigurationSection general = cfg.getConfigurationSection("general");
		broadcast = general.getString("research_complete_command").replaceAll("&", "�");
		levelup = general.getString("research_levelup").replaceAll("&", "�");
		discovery = general.getString("discovery");
		if (new File(getDataFolder(), "instance").exists()) {
			isInstance = true;
		}

		// Exp
		toNextLvl = new HashMap<Integer, Integer>();
		ConfigurationSection expSec = cfg.getConfigurationSection("exp");
		for (String lvl : expSec.getKeys(false)) {
			toNextLvl.put(Integer.parseInt(lvl), expSec.getInt(lvl));
		}

		// Load research items into mobMap, researchItems,
		researchItems = new HashMap<String, ResearchItem>();
		mobMap = new HashMap<String, ArrayList<ResearchItem>>();
		displayNameMap = new HashMap<String, String>();
		
		MobManager mm = MythicMobs.inst().getMobManager();
		ConfigurationSection rItems = cfg.getConfigurationSection("research_items");

		for (String rItem : rItems.getKeys(false)) {
			try {
				ConfigurationSection rItemSec = rItems.getConfigurationSection(rItem);
				ResearchItem researchItem = new ResearchItem(rItem);
				
				// exp
				researchItem.setExp(rItemSec.getInt("exp"));
				// id
				researchItem.setId(rItemSec.getString("id"));
				
				// attributes
				researchItem.setAttrs(rItemSec.getInt("attrs"));
				
				// kill goals
				ConfigurationSection goalsSec = rItemSec.getConfigurationSection("goals");
				HashMap<String, Integer> goals = new HashMap<String, Integer>();
				for (String mob : goalsSec.getKeys(false)) {
					goals.put(mob, goalsSec.getInt(mob));
	
					// Add to mob map, research book min, display name map
					if (mobMap.containsKey(mob)) {
						mobMap.get(mob).add(researchItem);
						String mobdisplay = mob;
						if (mm.getMythicMob(mob) != null) {
							 mm.getMythicMob(mob).getDisplayName().get();
						}
						displayNameMap.put(mob, mobdisplay);
					}
					else {
						ArrayList<ResearchItem> list = new ArrayList<ResearchItem>();
						list.add(researchItem);
						mobMap.put(mob, list);
					}
				}
				researchItem.setGoals(goals);
				researchItems.put(researchItem.getId(), researchItem);
			} catch (Exception e) {
				System.out.println("Failed to load research item: " + rItem);
				e.printStackTrace();
			}
		}
	
		// Converter
		ConfigurationSection collections = cfg.getConfigurationSection("converter");
		for (String col : collections.getKeys(false)) {
			String perm = col.replaceAll(",", ".");
			ConfigurationSection colMobs = collections.getConfigurationSection(col);
			HashMap<String, Integer> mobValues = new HashMap<String, Integer>();
			for (String mob : colMobs.getKeys(false)) {
				int points = colMobs.getInt(mob);
				mobValues.put(mob, points);
				if (points == 15) {
					minibosses.add(mob);
				}
			}
			converter.put(perm, mobValues);
		}
		
		
		// Finally, load in all online players
		for (Player p : Bukkit.getOnlinePlayers()) {
			loadPlayer(p);
		}
	}
	
	private void loadPlayer(OfflinePlayer p) {
		UUID uuid = p.getUniqueId();
		
		// Add them to attrs
		if (!playerAttrs.containsKey(uuid)) {
			playerAttrs.put(uuid, new StoredAttributes());
		}
			
		// Asynchronously look up sql and load it in
		Research main = this;
		BukkitRunnable load = new BukkitRunnable() {
			public void run() {
				int level = 5, exp = 0;
				int expectedAttrs = 0;
				HashMap<String, Integer> researchPoints = new HashMap<String, Integer>();
				HashMap<String, Integer> mobKills = new HashMap<String, Integer>();
				HashMap<String, ResearchItem> completedResearchItems = new HashMap<String, ResearchItem>();
				try {
					Class.forName("com.mysql.jdbc.Driver");
					Connection con = DriverManager.getConnection(url, user, pass);
					Statement stmt = con.createStatement();
					ResultSet rs = stmt.executeQuery("SELECT * FROM research_accounts WHERE uuid = '" + uuid + "';");
					
					// Load in account info
					if (rs.next()) {
						level = rs.getInt(2);
						exp = rs.getInt(3);
	
						rs = stmt.executeQuery("SELECT * FROM research_points WHERE uuid = '" + uuid + "';");
						while (rs.next()) {
							researchPoints.put(rs.getString(2), rs.getInt(3));
						}
	
						rs = stmt.executeQuery("SELECT * FROM research_kills WHERE uuid = '" + uuid + "';");
						while (rs.next()) {
							mobKills.put(rs.getString(2), rs.getInt(3));
						}
						
						rs = stmt.executeQuery("SELECT * FROM research_completed WHERE uuid = '" + uuid + "';");
						while (rs.next()) {
							ResearchItem rItem = researchItems.get(rs.getString(2));
							expectedAttrs += rItem.getAttrs();
							completedResearchItems.put(rItem.getId(), rItem);
						}
						
						rs = stmt.executeQuery("SELECT * FROM research_attributes WHERE uuid = '" + uuid + "';");
						StoredAttributes pAttrs = playerAttrs.get(uuid);
						int actualAttrs = 0;
						while (rs.next()) {
							actualAttrs += rs.getInt(3);
							pAttrs.addAttribute(rs.getString(2), rs.getInt(3));
						}
						System.out.println(expectedAttrs + " " + actualAttrs);
						
						if (expectedAttrs > actualAttrs) {
							pAttrs.addAttribute("unused", expectedAttrs - actualAttrs);
						}
						else if (expectedAttrs < actualAttrs) {
							pAttrs.removeStoredAttributes();
							pAttrs.addAttribute("unused", expectedAttrs);
						}
	
						playerStats.put(uuid, new PlayerStats(main, level, exp, completedResearchItems, researchPoints, mobKills));
					}
					con.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				} finally {
					playerStats.put(uuid, new PlayerStats(main, level, exp, completedResearchItems, researchPoints, mobKills));
				}
			}
		};
		load.runTaskAsynchronously(this);
	}

	private void handleLeave(UUID uuid) {
		if (lastSave.containsKey(uuid)) {
			if (lastSave.get(uuid) + 10000 >= System.currentTimeMillis()) {
				// If saved less than 10 seconds ago, don't save again
				return;
			}
		}

		lastSave.put(uuid, System.currentTimeMillis());
		BukkitRunnable save = new BukkitRunnable() {
			public void run() {
				if (playerStats.containsKey(uuid)) {
					try {
						Class.forName("com.mysql.jdbc.Driver");
						Connection con = DriverManager.getConnection(url, user, pass);
						Statement stmt = con.createStatement();

						// Save account
						save(uuid, con, stmt, true);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		};
		save.runTaskAsynchronously(this);
	}
	
	private void saveAll() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection con = DriverManager.getConnection(url, user, pass);
			Statement stmt = con.createStatement();
			for (Player p : Bukkit.getOnlinePlayers()) {
				save(p.getUniqueId(), con, stmt, false);
			}
			stmt.executeBatch();
			con.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void save(UUID uuid, Connection con, Statement stmt, boolean save) {
		try {
			PlayerStats stats = playerStats.get(uuid);
			if (playerStats.containsKey(uuid)) {
	
				// Save account
				stmt.addBatch("REPLACE INTO research_accounts VALUES ('" + uuid + "','" + stats.getLevel()
				+ "','" + stats.getExp() + "');");
	
				// Save research points
	
				HashMap<String, Integer> mobKills = stats.getMobKills();
				for (String mob : mobKills.keySet()) {
					stmt.addBatch("REPLACE INTO research_kills values('" + uuid + "','" + mob + "'," + mobKills.get(mob) + ");");
				}
				HashMap<String, Integer> researchPoints = stats.getResearchPoints();
				for (String mob : researchPoints.keySet()) {
					stmt.addBatch("REPLACE INTO research_points values('" + uuid + "','" + mob + "'," + researchPoints.get(mob) + ");");
				}
	
				for (Entry<String, ResearchItem> entry : stats.getCompletedResearchItems().entrySet()) {
					String name = entry.getValue().getId();
					stmt.addBatch("REPLACE INTO research_completed values('" + uuid + "','" + name + "');");
				}
			
				// Save attrs
				for (Entry<String, Integer> attr : playerAttrs.get(uuid).getStoredAttrs().entrySet()) {
					stmt.addBatch("REPLACE INTO research_attributes values('" + uuid + "','" + attr.getKey() + "'," + attr.getValue() + ");");
				}
				playerAttrs.remove(uuid);
				
				// Set to false if you're saving several accounts at once
				if (save) {
					stmt.executeBatch();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if (!e.getAction().equals(Action.RIGHT_CLICK_AIR) && !e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			return;
		}
		Player p = e.getPlayer();
		ItemStack main = p.getInventory().getItemInMainHand().clone();
		main.setAmount(1);

		if (main.getType().equals(Material.BOOK) && main.hasItemMeta() && main.getItemMeta().hasCustomModelData() && 
				main.getItemMeta().getCustomModelData() == 100 && main.getItemMeta().hasLore()) {
			String[] args = main.getItemMeta().getLore().get(0).split(" ");

			// "Grants x research points for [mob display name]"
			int amount = Integer.parseInt(args[1]);
			String display = main.getItemMeta().getLore().get(1);

			if (!playerStats.containsKey(p.getUniqueId())) {
				p.sendMessage("�4[�c�lMLMC�4] �cError, player stats not found.");
				return;
			}
			NBTItem nbti = new NBTItem(main);
			String mob = nbti.getString("internalmob");
			int level = nbti.getInteger("level");
			PlayerStats pStat = playerStats.get(p.getUniqueId());
			HashMap<String, Integer> researchPoints = pStat.getResearchPoints();
			HashMap<String, Integer> mobKills = pStat.getMobKills();
			int pLevel = pStat.getLevel();
			
			if (level > pLevel) {
				p.sendMessage("�4[�c�lMLMC�4] �cYou are too low level to research this mob!");
				return;
			}
			
			// Check if the player is sufficiently high level
			int points = researchPoints.containsKey(mob) ? researchPoints.get(mob) + amount : amount;
			
			// Reveal mob in kills if not there
			if (!mobKills.containsKey(mob)) {
				mobKills.put(mob, 0);
			}
			
			researchPoints.put(mob, points);
			p.getInventory().removeItem(main);
			p.playSound(p.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, SoundCategory.BLOCKS, 1, 1);
			p.sendMessage("�4[�c�lMLMC�4] �7You gained �e" + amount + " �7research points for " + display + "�7!");
			checkItemCompletion(mob, p, points, display);
		}
	}

	public void checkItemCompletion(String mob, Player p, int totalPoints, String display) {
		// Check if new discovery
		PlayerStats stats = playerStats.get(p.getUniqueId());
		HashMap<String, Integer> researchPoints = stats.getResearchPoints();
		
		// Check for research goals that need it
		HashMap<String, ResearchItem> completedItems = stats.getCompletedResearchItems();
		if (mobMap.containsKey(mob)) {
			for (ResearchItem researchItem : mobMap.get(mob)) { // For each relevant research item
				if (!completedItems.containsKey(researchItem.getId())) { // If the player hasn't completed it
					// Check if research goal is completed for specific mob
					HashMap<String, Integer> goals = researchItem.getGoals();
					if (goals.get(mob) <= totalPoints) {
						for (String rMob : goals.keySet()) { // Check every objective
							if (!researchPoints.containsKey(rMob) || researchPoints.get(rMob) < goals.get(rMob)) {
								return; // Haven't completed every item
							}
						}
	
						// Completed a research item
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
								broadcast.replaceAll("%player%", p.getName()).replaceAll("%item%", researchItem.getName()));
						completedItems.put(researchItem.getId(), researchItem);
						p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.BLOCKS, 1, 1);
						stats.addExp(p, researchItem.getExp());
						updateBonuses(p);
					}
				}
			}
		}
	}
	
	public void checkItemCompletionSilent(String mob, Player p, int totalPoints) {
		// Check if new discovery
		PlayerStats stats = playerStats.get(p.getUniqueId());
		HashMap<String, Integer> researchPoints = stats.getResearchPoints();
		
		// Check for research goals that need it
		HashMap<String, ResearchItem> completedItems = stats.getCompletedResearchItems();
		if (mobMap.containsKey(mob)) {
			for (ResearchItem researchItem : mobMap.get(mob)) { // For each relevant research item
				if (!completedItems.containsKey(researchItem.getId())) { // If the player hasn't completed it
					// Check if research goal is completed for specific mob
					HashMap<String, Integer> goals = researchItem.getGoals();
					if (goals.get(mob) <= totalPoints) {
						for (String rMob : goals.keySet()) { // Check every objective
							if (!researchPoints.containsKey(rMob) || researchPoints.get(rMob) < goals.get(rMob)) {
								return; // Haven't completed every item
							}
						}
	
						// Completed a research item
						completedItems.put(researchItem.getId(), researchItem);
						stats.addExp(p, researchItem.getExp());
						updateBonuses(p);
					}
				}
			}
		}
	}

	// Strictly for taking away research points/kills
	public void checkItemDecompletion(String mob, Player p, int totalPoints) {
		// Check for research goals that need it
		PlayerStats stats = playerStats.get(p.getUniqueId());
		HashMap<String, ResearchItem> completedItems = stats.getCompletedResearchItems();
		if (mobMap.containsKey(mob)) {
			for (ResearchItem researchItem : mobMap.get(mob)) { // For each relevant research item
				if (completedItems.containsKey(researchItem.getId())) { // If the player has completed it
					// Check if research goal is completed for specific mob
					HashMap<String, Integer> goals = researchItem.getGoals();
					if (goals.get(mob) > totalPoints) {
						stats.takeExp(p, researchItem.getExp());
						completedItems.remove(researchItem.getId());
					}
				}
			}
		}
		updateBonuses(p);
	}
	
	public void giveResearchPoints(Player p, int amount, String mob, int lvl, boolean announce, String via) {
		UUID uuid = p.getUniqueId();
		if (playerStats.containsKey(uuid)) {
			PlayerStats pStats = playerStats.get(uuid);
			HashMap<String, Integer> mobKills = pStats.getMobKills();
			HashMap<String, Integer> researchPoints = pStats.getResearchPoints();
			String display = MythicMobs.inst().getMobManager().getMythicMob(mob).getDisplayName().get();
			
			// New discovery
			if (!researchPoints.containsKey(mob)) {
				p.sendMessage(discovery.replaceAll("%mob%", display).replaceAll("&", "�"));
				researchPoints.put(mob, 0);
			}
			if (!mobKills.containsKey(mob)) {
				mobKills.put(mob, 0);
			}
			
			int pLevel = pStats.getLevel();
			if (pLevel >= lvl) {
				int points = researchPoints.containsKey(mob) ? researchPoints.get(mob) + amount : amount;
				researchPoints.put(mob, points);
				if (announce) {
					String msg = new String("&4[&c&lMLMC&4] &7You gained &e" + amount + " &7extra research points for " + display);
					if (via != null) {
						msg += " &7via " + via;
					}
					msg += "&7!";
					msg = msg.replaceAll("&", "�");
					p.sendMessage(msg);
				}
				String msg = display + " - �e" + points + " Research Pts";
				p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(msg));
				checkItemCompletion(mob, p, points, display);
			}
		}
	}
	
	public void giveResearchPointsAlias(Player p, int amount, String mob, int lvl, String display, boolean announce) {
		UUID uuid = p.getUniqueId();
		if (playerStats.containsKey(uuid)) {
			PlayerStats pStats = playerStats.get(uuid);
			HashMap<String, Integer> mobKills = pStats.getMobKills();
			HashMap<String, Integer> researchPoints = pStats.getResearchPoints();
			int pLevel = pStats.getLevel();
			
			// Discovery
			if (!researchPoints.containsKey(mob)) {
				p.sendMessage(discovery.replaceAll("%mob%", display).replaceAll("&", "�"));
				researchPoints.put(mob, 0);
			}
			if (!mobKills.containsKey(mob)) {
				mobKills.put(mob, 0);
			}
			
			if (pLevel >= lvl) {
				int points = researchPoints.containsKey(mob) ? researchPoints.get(mob) + amount : amount;
				researchPoints.put(mob, points);
				if (announce) {
					String msg = new String("&4[&c&lMLMC&4] &7You gained &e" + amount + " &7extra research points for " + display + "&7!");
					msg = msg.replaceAll("&", "�");
					p.sendMessage(msg);
				}
				String msg = display + " - �e" + points + " Research Pts";
				p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(msg));
				checkItemCompletion(mob, p, points, display);
			}
			else {
				String msg = display + " - �cResearch level too low!";
				p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(msg));
			}
		}
	}
	
	public void giveResearchPointsBypass(Player p, int amount, String mob) {
		UUID uuid = p.getUniqueId();
		if (playerStats.containsKey(uuid)) {
			PlayerStats pStats = playerStats.get(uuid);
			HashMap<String, Integer> mobKills = pStats.getMobKills();
			HashMap<String, Integer> researchPoints = pStats.getResearchPoints();
			int points = researchPoints.containsKey(mob) ? researchPoints.get(mob) + amount : amount;
			researchPoints.put(mob, points);
			if (!mobKills.containsKey(mob)) {
				mobKills.put(mob, 0);
			}
			checkItemCompletionSilent(mob, p, points);
		}
	}

	public void giveResearchKills(Player p, int amount, String mob) {
		UUID uuid = p.getUniqueId();
		if (playerStats.containsKey(uuid)) {
			PlayerStats pStats = playerStats.get(uuid);
			HashMap<String, Integer> mobKills = pStats.getMobKills();
			HashMap<String, Integer> researchPoints = pStats.getResearchPoints();
			if (!researchPoints.containsKey(mob)) {
				researchPoints.put(mob, 0);
			}
			int kills = mobKills.containsKey(mob) ? mobKills.get(mob) + amount : amount;
			mobKills.put(mob, kills);
		}
	}
	
	public void setResearchPoints(Player p, int amount, String mob) {
		UUID uuid = p.getUniqueId();
		if (playerStats.containsKey(uuid)) {
			HashMap<String, Integer> researchPoints = playerStats.get(uuid).getResearchPoints();
			researchPoints.put(mob, amount);
			checkItemDecompletion(mob, p, amount);
		}
	}

	public void setResearchKills(Player p, int amount, String mob) {
		UUID uuid = p.getUniqueId();
		if (playerStats.containsKey(uuid)) {
			HashMap<String, Integer> mobKills = playerStats.get(uuid).getMobKills();
			mobKills.put(mob, amount);
		}
	}

	public void updateBonuses(Player p) {
		UUID uuid = p.getUniqueId();
		if (playerAttrs.containsKey(uuid)) {
			// Make sure the player has no bonuses already equipped
			removeBonuses(p);

			// Activate the player's stored attrs
			playerAttrs.get(uuid).applyAttributes(p);
		}
	}
	
	public void resetBonuses(Player p) {
		UUID uuid = p.getUniqueId();
		if (playerAttrs.containsKey(uuid)) {
			playerAttrs.get(uuid).resetAttributes();
		}
	}
	
	public void removeBonuses(Player p) {
		UUID uuid = p.getUniqueId();
		if (playerAttrs.containsKey(uuid)) {
			StoredAttributes pAttrs = playerAttrs.get(uuid);
			pAttrs.removeAttributes(p);
			pAttrs.resetAttributes();
		}
	}
	
	public PlayerStats getPlayerStats(Player p) {
		return playerStats.get(p.getUniqueId());
	}
	
	public int getNumResearchItems() {
		return researchItems.size();
	}
	
	public HashMap<Integer, Integer> getNextLvl() {
		return toNextLvl;
	}
	
	public StoredAttributes getPlayerAttributes(Player p) {
		return playerAttrs.get(p.getUniqueId());
	}
	
	public boolean isCompleted(Player p, String id) {
		return playerStats.get(p.getUniqueId()).getCompletedResearchItems().containsKey(id);
	}
	
	// Below are situations where research should load
	
	@EventHandler
	public void onLoadSQL(PlayerLoadCompleteEvent e) {
		updateBonuses(e.getPlayer());
	}
	
	@EventHandler
	public void onSaveSQL(PlayerSaveEvent e) {
		handleLeave(e.getUUID());
	}
	
	@EventHandler
	public void onAttributeLoad(PlayerAttributeLoadEvent e) {
		updateBonuses(e.getPlayer());
	}
	
	@EventHandler
	public void onAttributeUnload(PlayerAttributeUnloadEvent e) {
		resetBonuses(e.getPlayer());
	}
	
	@EventHandler
	public void onLeave(PlayerQuitEvent e) {
		handleLeave(e.getPlayer().getUniqueId());
	}
	
	@EventHandler
	public void onKick(PlayerKickEvent e) {
		handleLeave(e.getPlayer().getUniqueId());
	}

	@EventHandler
	public void onJoin(AsyncPlayerPreLoginEvent e) {
		OfflinePlayer p = Bukkit.getOfflinePlayer(e.getUniqueId());
		loadPlayer(p);
	}
}
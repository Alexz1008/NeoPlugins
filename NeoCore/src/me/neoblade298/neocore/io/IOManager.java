package me.neoblade298.neocore.io;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.sucy.skill.api.event.PlayerLoadCompleteEvent;
import com.sucy.skill.api.event.PlayerSaveEvent;

import me.neoblade298.neocore.NeoCore;
import me.neoblade298.neocore.bungee.BungeeAPI;
import me.neoblade298.neocore.player.PlayerTags;

public class IOManager implements Listener {
	private static final String IO_CHANNEL = "neocore_io", START_SAVE_ID = "startsave", END_SAVE_ID = "endsave";
	// private static final int SAVE_TIMEOUT = 10000; // Time before we throw out a save start msg and don't count it for performance
	private static String connection;
	private static Properties properties;
	private static HashMap<UUID, Long> lastSave = new HashMap<UUID, Long>();
	private static HashMap<String, IOComponent> components = new HashMap<String, IOComponent>();
	private static TreeSet<IOComponent> orderedComponents;
	private static HashMap<IOType, HashSet<String>> disabledIO = new HashMap<IOType, HashSet<String>>();
	private static HashMap<IOType, HashSet<UUID>> performingIO = new HashMap<IOType, HashSet<UUID>>();
	private static HashMap<IOType, HashMap<UUID, ArrayList<PostIOTask>>> postIORunnables = new HashMap<IOType, HashMap<UUID, ArrayList<PostIOTask>>>();
	private static HashSet<UUID> isSaving = new HashSet<UUID>();
	private static boolean debug = false;
	
	static {
		for (IOType type : IOType.values()) {
			performingIO.put(type, new HashSet<UUID>());
			postIORunnables.put(type, new HashMap<UUID, ArrayList<PostIOTask>>());
			disabledIO.put(type, new HashSet<String>());
		}
		

		Comparator<IOComponent> comp = new Comparator<IOComponent>() {
			@Override
			public int compare(IOComponent i1, IOComponent i2) {
				if (i1.getPriority() > i2.getPriority()) {
					return -1;
				}
				else if (i1.getPriority() < i2.getPriority()) {
					return 1;
				}
				else {
					return i1.getKey().compareTo(i2.getKey());
				}
			}
		};
		orderedComponents = new TreeSet<IOComponent>(comp);
	}
	
	public IOManager(String connection, Properties properties) {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		IOManager.connection = connection;
		IOManager.properties = properties;
	}
	
	public static void register(JavaPlugin plugin, IOComponent component) {
		components.put(plugin.getName() + "-" + component.getKey(), component);
		orderedComponents.add(component);
	}
	
	@EventHandler
	public void onLeave(PlayerQuitEvent e) {
		save(e.getPlayer());
	}
	
	@EventHandler
	public void onKick(PlayerKickEvent e) {
		save(e.getPlayer());
	}
	
	@EventHandler
	public void onSAPISave(PlayerSaveEvent e) {
		save(e.getPlayer());
	}

	@EventHandler
	public void onPrejoin(AsyncPlayerPreLoginEvent e) {
		preload(Bukkit.getOfflinePlayer(e.getUniqueId()));
	}

	@EventHandler
	public void onSkillAPILoad(PlayerLoadCompleteEvent e) {
		load(e.getPlayer());
	}
	
	/* Currently doesn't work as pluginmessage fails if last player logs off (e.g. boss instance)
	@EventHandler
	public void onPluginMessage(PluginMessageEvent e) {
		if (!e.getChannel().equals(IO_CHANNEL)) return;

		ArrayList<String> msgs = e.getMessages();
		long timestamp = Long.parseLong(msgs.get(2));
		if (timestamp + SAVE_TIMEOUT < System.currentTimeMillis()) return;
		UUID uuid = UUID.fromString(msgs.get(1));
		
		if (msgs.get(0).equals(START_SAVE_ID)) {
			isSaving.add(uuid);
		}
		else if (msgs.get(0).equals(END_SAVE_ID)) {
			isSaving.remove(uuid);
		}
	}
	*/
	
	private void save(Player p) {
		HashSet<String> disabledKeys = disabledIO.get(IOType.SAVE);
		if (disabledKeys.contains("*")) {
			return;
		}
		
		UUID uuid = p.getUniqueId();
		// If somehow the person is already saving, don't try saving again
		if (isSaving.contains(uuid)) return;
		if (lastSave.getOrDefault(uuid, 0L) + 10000 >= System.currentTimeMillis()) {
			// If saved less than 10 seconds ago, don't save again
			return;
		}
		
		BungeeAPI.sendPluginMessage(IO_CHANNEL, new String[] { START_SAVE_ID, uuid.toString(), Long.toString(System.currentTimeMillis()) });
		isSaving.add(uuid);
		lastSave.put(uuid, System.currentTimeMillis());
		IOType type = IOType.SAVE;
		performingIO.get(type).add(uuid);
		long timestamp = System.currentTimeMillis();
		
		new BukkitRunnable() {
			public void run() {
				try {
					Connection con = DriverManager.getConnection(connection, properties);
					Statement insert = con.createStatement();
					Statement delete = con.createStatement();

					// Save account
					long timestamp = System.currentTimeMillis();
					for (IOComponent io : orderedComponents) {
						if (!disabledKeys.contains(io.getKey().toUpperCase())) {
							try {
								io.savePlayer(p, insert, delete);
								int deleted = 0, inserted = 0;
								for (int i : delete.executeBatch()) {
									deleted += i;
								}
								for (int i : insert.executeBatch()) {
									inserted += i;
								}
								if (io instanceof PlayerTags) {
									Bukkit.getLogger().info("[NeoCore Debug] Inserted " + inserted + " and deleted " + deleted);
								}
								if (debug) Bukkit.getLogger().info("[NeoCore Debug] Component " + io.getKey() + " saved player " + uuid + " in " + 
										(System.currentTimeMillis() - timestamp) + "ms");
							}
							catch (Exception ex) {
								Bukkit.getLogger().log(Level.WARNING, "[NeoCore] Failed to handle save for component " + io.getKey() + " for player " + uuid);
								ex.printStackTrace();
							}
						}
					}
				} catch (Exception ex) {
					Bukkit.getLogger().log(Level.WARNING, "[NeoCore] Failed to handle saving for player " + uuid);
					ex.printStackTrace();
				}
				finally {
					endIOTask(type, uuid);
					BungeeAPI.sendPluginMessage(IO_CHANNEL, new String[] { END_SAVE_ID, uuid.toString(), Long.toString(System.currentTimeMillis()) });
					Bukkit.getLogger().info("[NeoCore] Finished saving player " + uuid + ", took " + (System.currentTimeMillis() - timestamp) + "ms");
					if (debug) Bukkit.getLogger().info("[NeoCore Debug] Finished saving at time " + System.currentTimeMillis());
					isSaving.remove(uuid);
				}
			}
		}.runTaskAsynchronously(NeoCore.inst());
	}
	
	public void autosave(Player p) {
		HashSet<String> disabledKeys = disabledIO.get(IOType.AUTOSAVE);
		if (disabledKeys.contains("*")) {
			return;
		}
		
		UUID uuid = p.getUniqueId();
		if (lastSave.getOrDefault(uuid, 0L) + 10000 >= System.currentTimeMillis()) {
			// If saved less than 10 seconds ago, don't save again
			return;
		}
		lastSave.put(uuid, System.currentTimeMillis());
		IOType type = IOType.AUTOSAVE;
		performingIO.get(type).add(uuid);
		
		new BukkitRunnable() {
			public void run() {
				try {
					Connection con = DriverManager.getConnection(connection, properties);
					Statement insert = con.createStatement();
					Statement delete = con.createStatement();

					// Save account
					for (IOComponent io : orderedComponents) {
						if (!disabledKeys.contains(io.getKey().toUpperCase())) {
							try {
								io.autosavePlayer(p, insert, delete);
								delete.executeBatch();
								insert.executeBatch();
							}
							catch (Exception ex) {
								Bukkit.getLogger().log(Level.WARNING, "[NeoCore] Failed to handle autosave for component " + io.getKey());
								ex.printStackTrace();
							}
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				finally {
					endIOTask(type, p.getUniqueId());
				}
			}
		}.runTaskAsynchronously(NeoCore.inst());
	}
	
	private void preload(OfflinePlayer p) {
		IOType type = IOType.PRELOAD;
		HashSet<String> disabledKeys = disabledIO.get(type);
		if (disabledKeys.contains("*")) {
			return;
		}
		performingIO.get(type).add(p.getUniqueId());
		
		new BukkitRunnable() {
			public void run() {
				try {
					Connection con = DriverManager.getConnection(connection, properties);
					Statement stmt = con.createStatement();

					// Save account
					for (IOComponent io : orderedComponents) {
						if (!disabledKeys.contains(io.getKey().toUpperCase())) {
							try {
								io.preloadPlayer(p, stmt);
								stmt.executeBatch();
							}
							catch (Exception ex) {
								Bukkit.getLogger().log(Level.WARNING, "[NeoCore] Failed to handle preload for component " + io.getKey());
								ex.printStackTrace();
							}
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				finally {
					endIOTask(type, p.getUniqueId());
				}
			}
		}.runTaskAsynchronously(NeoCore.inst());
	}
	
	private void load(Player p) {
		IOType type = IOType.LOAD;
		HashSet<String> disabledKeys = disabledIO.get(type);
		if (disabledKeys.contains("*")) {
			return;
		}
		performingIO.get(type).add(p.getUniqueId());
		
		new BukkitRunnable() {
			int count = 0;
			public void run() {
				try {
					if (isSaving.contains(p.getUniqueId())) {
						Bukkit.getLogger().warning("[NeoCore] Player " + p.getName() + " is still saving, skipping attempt " + count);
						return;
					}
					if (++count > 5) {
						this.cancel();
						endIOTask(type, p.getUniqueId());
						Bukkit.getLogger().warning("[NeoCore] Failed to load player " + p.getName());
						return;
					}
					Connection con = DriverManager.getConnection(connection, properties);
					Statement stmt = con.createStatement();
					if (debug) Bukkit.getLogger().info("[NeoCore Debug] Began loading at time " + System.currentTimeMillis());

					// Save account
					for (IOComponent io : orderedComponents) {
						if (!disabledKeys.contains(io.getKey().toUpperCase())) {
							try {
								io.loadPlayer(p, stmt);
								stmt.executeBatch();
							}
							catch (Exception ex) {
								Bukkit.getLogger().log(Level.WARNING, "[NeoCore] Failed to handle load for component " + io.getKey());
								ex.printStackTrace();
							}
						}
					}
					Bukkit.getLogger().info("[NeoCore] Successfully loaded player " + p.getName() + " in " + count + " attempts");
					this.cancel();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				finally {
					endIOTask(type, p.getUniqueId());
				}
			}
		}.runTaskTimerAsynchronously(NeoCore.inst(), 0L, 20L);
	}
	
	public static void handleDisable() {
		HashSet<String> disabledKeys = disabledIO.get(IOType.CLEANUP);
		if (disabledKeys.contains("*")) {
			return;
		}
		
		try {
			Connection con = DriverManager.getConnection(connection, properties);
			Statement insert = con.createStatement();
			Statement delete = con.createStatement();
			
			// Any final cleanup
			for (IOComponent io : orderedComponents) {
				if (!disabledKeys.contains(io.getKey().toUpperCase())) {
					try {
						Bukkit.getLogger().info("[NeoCore] Cleaning up component " + io.getKey());
						io.cleanup(insert, delete);
						delete.executeBatch();
						insert.executeBatch();
					}
					catch (Exception ex) {
						Bukkit.getLogger().log(Level.WARNING, "[NeoCore] Failed to handle cleanup for component " + io.getKey());
						ex.printStackTrace();
					}
				}
				else {
					Bukkit.getLogger().info("[NeoCore] Skipping cleanup for component " + io.getKey());
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static Statement getStatement() {
		try {
			Connection con = DriverManager.getConnection(connection, properties);
			return con.createStatement();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public static void disableIO(IOType type, String key) {
		disabledIO.get(type).add(key.toUpperCase());
	}
	
	public static void disableIO(IOType type) {
		disableIO(type, "*");
	}

	public static void enableIO(IOType type, String key) {
		disabledIO.get(type).remove(key.toUpperCase());
	}

	public static void enableIO(IOType type) {
		enableIO(type, "*");
	}
	
	public static void addPostIORunnable(BukkitRunnable task, IOType type, UUID uuid, boolean async) {
		ArrayList<PostIOTask> tasks = postIORunnables.get(type).getOrDefault(uuid, new ArrayList<PostIOTask>());
		tasks.add(new PostIOTask(task, async));
		postIORunnables.get(type).putIfAbsent(uuid,	tasks);
	}
	
	public static boolean isPerformingIO(UUID uuid, IOType type) {
		return performingIO.get(type).contains(uuid);
	}
	
	private static void endIOTask(IOType type, UUID uuid) {
		performingIO.get(type).remove(uuid);
		if (postIORunnables.get(type).containsKey(uuid)) {
			for (PostIOTask task : postIORunnables.get(type).get(uuid)) {
				if (task.isAsync()) {
					task.getRunnable().runTaskAsynchronously(NeoCore.inst());
				}
				else {
					task.getRunnable().runTask(NeoCore.inst());
				}
			}
			postIORunnables.get(type).remove(uuid);
		}
	}
	
	public static TreeSet<IOComponent> getComponents() {
		return orderedComponents;
	}
	
	// Applies to cross-server saves, unlike isPerformingIO. Use to make sure you load AFTER save is complete
	public static boolean isSaving(Player p) {
		return isSaving.contains(p.getUniqueId());
	}
	
	public static HashSet<UUID> getSavingUsers() {
		return isSaving;
	}
	
	public static boolean toggleDebug() {
		debug = !debug;
		return debug;
	}
	
	public static HashMap<IOType, HashSet<String>> getDisabledIO() {
		return disabledIO;
	}
}

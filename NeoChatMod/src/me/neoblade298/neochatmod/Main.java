package me.neoblade298.neochatmod;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.mineacademy.chatcontrol.api.ChatChannelEvent;

public class Main extends JavaPlugin implements Listener {
	File file = null;
	FileConfiguration conf = null;
	ArrayList<String> bannedWords = null;
	boolean muteGlobal = false;
	
	boolean muteTutorial = false;
	List<String> punishCmds = null; 

	int QUEST_X_BOUND_1 = 955;
	int QUEST_X_BOUND_2 = 992;
	int QUEST_Z_BOUND_1 = -237;
	int QUEST_Z_BOUND_2 = -200;

	public void onEnable() {
		Bukkit.getServer().getLogger().info("NeoChatMod Enabled");
		getServer().getPluginManager().registerEvents(this, this);
	    this.getCommand("servermute").setExecutor(new Commands(this));

		// Save config if doesn't exist
		file = new File(getDataFolder(), "config.yml");
		if (!file.exists()) {
			saveResource("config.yml", false);
		}
		conf = YamlConfiguration.loadConfiguration(file);
		
		// Load settings
		muteTutorial = getConfig().getBoolean("mute-tutorial");
		punishCmds = getConfig().getStringList("punish-commands");
		
		// Load banned words
		bannedWords = (ArrayList<String>) getConfig().getStringList("banned-words");
		
		for (int i = 0; i < bannedWords.size(); i++) {
			bannedWords.set(i, bannedWords.get(i).toUpperCase());
		}
	}

	public void onDisable() {
		Bukkit.getServer().getLogger().info("NeoChatMod Disabled");
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onChat(ChatChannelEvent e) {
		if (this.muteGlobal && !e.getSender().hasPermission("towny.chat.mod")) {
			e.getSender().sendMessage("§4[§c§lMLMC§4] §cThere is currently a server mute!");
			e.setCancelled(true);
			return;
		}
		if (!(e.getSender() instanceof Player)) {
			return;
		}
		Player sender = (Player) e.getSender();
		String msg = e.getMessage();
		double x = sender.getLocation().getX();
		double z = sender.getLocation().getZ();
		World w = sender.getWorld();
		
		// Check if they're in the tutorial world
		if (w.getName().equalsIgnoreCase("Argyll")) {
			if ((QUEST_X_BOUND_1 <= x && x <= QUEST_X_BOUND_2) &&
			(QUEST_Z_BOUND_1 <= z && z <= QUEST_Z_BOUND_2) &&
			!sender.hasPermission("tutorial.chat.receive")) {
				for (Player p : Bukkit.getServer().getOnlinePlayers()) {
					if (p.hasPermission("tutorial.chat.receive")) {
						p.sendMessage("§4[§c§lMLMC§4] §c" + sender.getName() + " §7spoke in tutorial: §c" + msg);
					}
				}
				sender.sendMessage("§4[§c§lMLMC§4] §cYou cannot speak in the tutorial, but staff can still hear you!");
				e.setCancelled(true);
			}
		}
		
		// Check if it contained a curse word
		for (String word : bannedWords) {
			if (msg.toUpperCase().contains(word)) {
				e.setCancelled(true);
				for (String cmd : punishCmds) {
					Bukkit.getScheduler().callSyncMethod(this, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replaceAll("%player%", sender.getName())
							.replaceAll("%word%", word).replaceAll("%msg%", msg)));
				}
				
				for (Player p : Bukkit.getServer().getOnlinePlayers()) {
					if (p.hasPermission("tutorial.staff.receive")) {
						p.sendMessage("§4[§c§lMLMC§4] §c" + sender.getName() + " §7was punished for saying: §c" + msg);
					}
				}
				return;
			}
		}
	}
	
	public void toggleMute() {
		this.muteGlobal = !this.muteGlobal;
	}
	
	public boolean getMute() {
		return this.muteGlobal;
	}
}
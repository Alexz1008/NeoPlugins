package me.neoblade298.neoquests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import org.bukkit.Bukkit;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.session.SessionManager;
import com.sucy.skill.SkillAPI;

import me.neoblade298.neocore.NeoCore;
import me.neoblade298.neocore.commands.CommandManager;
import me.neoblade298.neocore.instancing.InstanceType;
import me.neoblade298.neocore.interfaces.Manager;
import me.neoblade298.neocore.player.PlayerTags;
import me.neoblade298.neoquests.actions.ActionManager;
import me.neoblade298.neoquests.commands.*;
import me.neoblade298.neoquests.conditions.ConditionManager;
import me.neoblade298.neoquests.conversations.ConversationManager;
import me.neoblade298.neoquests.listeners.GeneralListener;
import me.neoblade298.neoquests.listeners.NavigationListener;
import me.neoblade298.neoquests.listeners.ConversationListener;
import me.neoblade298.neoquests.listeners.ObjectiveListener;
import me.neoblade298.neoquests.listeners.QuesterListener;
import me.neoblade298.neoquests.navigation.NavigationManager;
import me.neoblade298.neoquests.objectives.ObjectiveManager;
import me.neoblade298.neoquests.quests.QuestsManager;
import me.neoblade298.neoquests.worldguard.RequiredTagFlagHandler;

public class NeoQuests extends JavaPlugin implements org.bukkit.event.Listener {
	public static Random rand = new Random();

	private static NeoQuests inst;
	private static HashSet<Player> debuggers = new HashSet<Player>();
	private static ArrayList<Manager> managers = new ArrayList<Manager>();
	private static HashMap<String, CommandManager> commands = new HashMap<String, CommandManager>();
	private static PlayerTags[] accountTags = new PlayerTags[12];
	private static PlayerTags globalTags;
	public static StringFlag REQ_TAG_FLAG;

	public void onEnable() {
		inst = this;
		Bukkit.getServer().getLogger().info("NeoQuests Enabled");

		// Minimized initialization if session host
		if (NeoCore.getInstanceType() == InstanceType.SESSIONS) {
			for (int i = 1; i <= 12; i++) {
				accountTags[i - 1] = NeoCore.createPlayerTags("questaccount_" + i, this, true);
			}
			return;
		}

		getServer().getPluginManager().registerEvents(new ConversationListener(), this);
		getServer().getPluginManager().registerEvents(new ObjectiveListener(), this);
		getServer().getPluginManager().registerEvents(new GeneralListener(), this);
		getServer().getPluginManager().registerEvents(new NavigationListener(), this);
		getServer().getPluginManager().registerEvents(new QuesterListener(), this);

		initCommands();
		

		// Managers
		try {
			new ActionManager();
			new ObjectiveManager();
			new ConditionManager();
			managers.add(new ConversationManager());
			managers.add((Manager) NeoCore.registerIOComponent(this, new QuestsManager()));
			managers.add(new NavigationManager());
		} catch (Exception e) {
			showWarning("Failed to enable managers on startup", e);
		}
		
		// Playerdata
		for (int i = 1; i <= 12; i++) {
			accountTags[i - 1] = NeoCore.createPlayerTags("questaccount_" + i, this, true);
		}
		globalTags = NeoCore.createPlayerTags("questaccount_global", this, true);
		
		// WorldGuard
	    SessionManager sessionManager = WorldGuard.getInstance().getPlatform().getSessionManager();
	    sessionManager.registerHandler(RequiredTagFlagHandler.FACTORY, null);
	}
	
	@Override
	public void onLoad() {
		// WorldGuard
		FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
	    try {
	        // create a flag with the name "my-custom-flag"
	        StringFlag flag = new StringFlag("required-tag");
	        registry.register(flag);
	        REQ_TAG_FLAG = flag; // only set our field if there was no error
	    } catch (FlagConflictException e) {
	        // some other plugin registered a flag by the same name already.
	        // you can use the existing flag, but this may cause conflicts - be sure to check type
	    	e.printStackTrace();
	    	
	    } catch (IllegalStateException e) {
	    	e.printStackTrace();
	    }
	}

	private void initCommands() {
		String cmd = "quest";
		CommandManager quest = new CommandManager(cmd, this);
		quest.register(new CmdQuestBase());
		commands.put(cmd, quest);

		cmd = "quests";
		CommandManager quests = new CommandManager(cmd, this);
		quests.registerCommandList("");
		quests.register(new CmdQuestsQuit(),
				new CmdQuestsTake(),
				new CmdQuestsLog(),
				new CmdQuestsView(),
				new CmdQuestsGuide(),
				new CmdQuestsRecommended(),
				new CmdQuestsChallenges(),
				new CmdQuestsList());
		this.getCommand(cmd).setExecutor(quests);
		commands.put(cmd, quests);

		cmd = "questadmin";
		CommandManager questadmin = new CommandManager(cmd, ChatColor.DARK_RED, this);
		questadmin.registerCommandList("");
		questadmin.register(new CmdQuestAdminReload(),
				new CmdQuestAdminStart(),
				new CmdQuestAdminCompleteQL(),
				new CmdQuestAdminAddTag(),
				new CmdQuestAdminRemoveTag(),
				new CmdQuestAdminStart(),
				new CmdQuestAdminReset(),
				new CmdQuestAdminComplete(),
				new CmdQuestAdminIsComplete(),
				new CmdQuestAdminSetStage());
		this.getCommand(cmd).setExecutor(questadmin);
		commands.put(cmd, questadmin);

		cmd = "navigation";
		CommandManager navigation = new CommandManager(cmd, this);
		navigation.registerCommandList("");
		navigation.register(new CmdNavigationTo(),
				new CmdNavigationFrom(),
				new CmdNavigationStart(),
				new CmdNavigationStop());
		this.getCommand(cmd).setExecutor(navigation);
		commands.put(cmd, navigation);

		cmd = "adminnavigation";
		CommandManager anavigation = new CommandManager(cmd, ChatColor.DARK_RED, this);
		anavigation.registerCommandList("");
		anavigation.register(new CmdANavigationSave(),
				new CmdANavigationStart(),
				new CmdANavigationEditor(),
				new CmdANavigationExit(),
				new CmdANavigationTo(),
				new CmdANavigationFrom(),
				new CmdANavigationAddPathway(),
				new CmdANavigationClear());
		this.getCommand(cmd).setExecutor(anavigation);
		commands.put(cmd, anavigation);
		
		cmd = "conversation";
		CommandManager cm = new CommandManager(cmd, this);
		cm.register(new CmdConvAnswer());
	}

	public void onDisable() {
		for (Manager mngr : managers) {
			mngr.cleanup();
		}
		org.bukkit.Bukkit.getServer().getLogger().info("NeoQuests Disabled");
		super.onDisable();
	}

	public static NeoQuests inst() {
		return inst;
	}

	public static void showWarning(String warning) {
		Bukkit.getLogger().warning("[NeoQuests] " + warning);
		for (Player p : debuggers) {
			p.sendMessage(warning);
		}
	}

	public static void showWarning(String warning, Exception e) {
		Bukkit.getLogger().warning("[NeoQuests] " + warning);
		Bukkit.getLogger().warning("[NeoQuests] " + e.getMessage());
		for (Player p : debuggers) {
			p.sendMessage(warning);
			p.sendMessage(e.getMessage());
		}
		e.printStackTrace();
	}

	public static boolean reloadAll() {
		for (Manager mngr : managers) {
			mngr.reload();
			Bukkit.getLogger().info("[NeoQuests] Reloaded manager " + mngr.getKey());
		}
		return true;
	}

	public static void addDebugger(Player p) {
		debuggers.add(p);
	}

	public static HashMap<String, CommandManager> getCommands() {
		return commands;
	}
	
	public static PlayerTags getPlayerTags(Player p) {
		int account = SkillAPI.getPlayerAccountData(p).getActiveId();
		return getPlayerTags(account);
	}
	
	public static PlayerTags getPlayerTags(int account) {
		return accountTags[account - 1];
	}
	
	public static PlayerTags[] getAllPlayerTags() {
		return accountTags;
	}
	
	public static PlayerTags getGlobalPlayerTags() {
		return globalTags;
	}
}

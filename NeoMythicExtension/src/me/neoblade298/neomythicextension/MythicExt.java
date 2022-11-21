package me.neoblade298.neomythicextension;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ISkillMechanic;
import io.lumine.mythic.api.skills.conditions.ISkillCondition;
import io.lumine.mythic.api.skills.targeters.ISkillTargeter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.events.MythicConditionLoadEvent;
import io.lumine.mythic.bukkit.events.MythicDropLoadEvent;
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent;
import io.lumine.mythic.bukkit.events.MythicTargeterLoadEvent;
import me.neoblade298.neobossinstances.BossInstances;
import me.neoblade298.neomythicextension.conditions.*;
import me.neoblade298.neomythicextension.drops.StoredItemDrop;
import me.neoblade298.neomythicextension.mechanics.*;
import me.neoblade298.neomythicextension.objects.SpawnerMaker;
import me.neoblade298.neomythicextension.targeters.*;
import me.neoblade298.neomythicextension.triggers.StatusTrigger;
import net.milkbowl.vault.economy.Economy;

public class MythicExt extends JavaPlugin implements Listener {

	private Logger log;
	public ConcurrentHashMap<String, Integer> globalscores;
	// ConcurrentHashMap of objectives which leads to ConcurrentHashMap of uuids to
	// integers
	public ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> scores;
	public HashMap<UUID, SpawnerMaker> spawnermakers;
	public static MythicExt inst;
	public static Economy econ;
	public static boolean debug = false;
	private BossInstances nbi;

	@Override
	public void onEnable() {
		log = this.getLogger();
		Bukkit.getPluginManager().registerEvents(this, this);
		Bukkit.getPluginManager().registerEvents(new StatusTrigger(this), this);
		globalscores = new ConcurrentHashMap<String, Integer>();
		scores = new ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>>();
		spawnermakers = new HashMap<UUID, SpawnerMaker>();
		nbi = (BossInstances) Bukkit.getPluginManager().getPlugin("NeoBossInstances");

		// Get command listener
		this.getCommand("nme").setExecutor(new Commands(this));
		log.info("NeoMythicExtensions Enabled!");
		inst = this;

		// Setup bungee pluginmsging
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

		if (!setupEconomy()) {
			log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
	}

	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = rsp.getProvider();
		return econ != null;
	}

	public void onDisable() {
	    this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
		log.info("NeoMythicExtensions Disabled!");
		super.onDisable();
	}

	@EventHandler
	public void onMythicConditionLoad(MythicConditionLoadEvent event) {
		String name = event.getConditionName().toLowerCase();
		ISkillCondition condition = null;
		MythicLineConfig cfg = event.getConfig();
		switch (name) {
		case "hasflag":
			condition = new SkillAPIFlagCondition(cfg, this);
			break;
		case "nglobalscore":
			condition = new GlobalScoreCondition(cfg, this);
			break;
		case "nscore":
			condition = new ScoreCondition(cfg);
			break;
		case "playersinboss":
			condition = new PlayersInBossCondition(cfg, nbi);
			break;
		case "aboveblock":
			condition = new AboveBlockCondition(cfg);
			break;
		case "strongplayerwithin":
			condition = new StrongPlayerWithin(cfg);
			break;
		case "fightingboss":
			condition = new FightingBossCondition(cfg);
			break;
		case "isinstance":
			condition = new IsInstanceCondition(cfg);
			break;
		case "hasaccounttag":
			condition = new HasAccountTagCondition(cfg);
			break;
		}
		if (condition != null) {
			event.register(condition);
		}
	}

	@EventHandler
	public void onMythicDropLoad(MythicDropLoadEvent event) {
		String name = event.getDropName();
		if (name.equalsIgnoreCase("storeditem")) {
			event.register(new StoredItemDrop(event.getConfig()));
		}
	}

	@EventHandler
	public void onMythicTargeterLoad(MythicTargeterLoadEvent event) {
		String name = event.getTargeterName().toLowerCase();
		ISkillTargeter targeter = null;
		MythicLineConfig cfg = event.getConfig();
		switch (name) {
		case "playersinboss":
			targeter = new PlayersInBossTargeter(cfg, nbi);
			break;
		case "pib":
			targeter = new PlayersInBossTargeter(cfg, nbi);
			break;
		case "offset":
			targeter = new OffsetTargeter(cfg);
			break;
		}
		if (targeter != null) {
			event.register(targeter);
		}
	}

	@EventHandler
	public void onMythicMechanicLoad(MythicMechanicLoadEvent event) {
		String name = event.getMechanicName().toLowerCase();
		ISkillMechanic mechanic = null;
		MythicLineConfig cfg = event.getConfig();

		switch (name) {
		case "elementdamage":
			mechanic = new ElementDamage(MythicBukkit.inst().getSkillManager(), cfg.getLine(), cfg);
			break;
		case "nscore":
			mechanic = new ModScore(cfg, this);
			break;
		case "nglobalscore":
			mechanic = new ModGlobalScore(cfg, this);
			break;
		case "warn":
			mechanic = new WarnMechanic(cfg);
			break;
		case "taunt":
			mechanic = new TauntMechanic(cfg);
			break;
		case "reducethreat":
			mechanic = new ReduceThreatMechanic(cfg);
			break;
		case "flag":
			mechanic = new FlagMechanic(cfg);
			break;
		case "giveaccounttag":
			mechanic = new GiveAccountTagMechanic(cfg);
			break;
		case "giveattribute":
			mechanic = new GiveAttributeMechanic(cfg);
			break;
		case "removeflag":
			mechanic = new RemoveFlagMechanic(cfg);
			break;
		case "scalechest":
			mechanic = new ScaleChestMechanic(cfg);
			break;
		case "scaletolevel":
			mechanic = new ScaleToLevelMechanic(cfg, nbi);
			break;
		case "scalegold":
			mechanic = new ScaleGoldMechanic(cfg, nbi);
			break;
		case "researchpoints":
			mechanic = new ResearchPointsMechanic(cfg);
			break;
		case "researchpointsboss":
			mechanic = new ResearchPointsBossMechanic(cfg);
			break;
		case "researchpointschance":
			mechanic = new ResearchPointsChanceMechanic(cfg);
			break;
		case "researchkills":
			mechanic = new ResearchKillsMechanic(cfg);
			break;
		case "modmana":
			mechanic = new ModManaMechanic(cfg);
			break;
		case "scaleheal":
			mechanic = new ScaleHealMechanic(cfg);
			break;
		case "scaleexp":
			mechanic = new ScaleExpMechanic(cfg);
			break;
		case "givestoreditem":
			mechanic = new GiveStoredItemMechanic(cfg);
			break;
		case "pluginmessage":
			mechanic = new PluginMessageMechanic(cfg);
			break;
		}
		if (mechanic != null) {
			event.register(mechanic);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onDummyDamage(EntityDamageByEntityEvent e) {
		String name = e.getEntity().getCustomName();
		if (name != null && name.contains("(Damage)")) {
			if (e.getDamager() instanceof Player) {
				Player p = (Player) e.getDamager();
				for (Entity ent : e.getEntity().getNearbyEntities(5, 5, 5)) {
					if (ent instanceof Player) {
						ent.sendMessage("§e" + p.getName() + " §7did §c" + (Math.round(e.getFinalDamage() * 100) / 100)
								+ " §7damage");
					}
				}
			}
		}
	}

}
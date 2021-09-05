package me.Neoblade298.NeoProfessions;

import java.io.File;
import java.util.Iterator;
import java.util.Properties;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import me.Neoblade298.NeoProfessions.Commands.BlacksmithCommands;
import me.Neoblade298.NeoProfessions.Commands.CulinarianCommands;
import me.Neoblade298.NeoProfessions.Commands.MasonCommands;
import me.Neoblade298.NeoProfessions.Commands.NeoprofessionsCommands;
import me.Neoblade298.NeoProfessions.Commands.StonecutterCommands;
import me.Neoblade298.NeoProfessions.Listeners.BlacksmithListeners;
import me.Neoblade298.NeoProfessions.Listeners.CulinarianListeners;
import me.Neoblade298.NeoProfessions.Listeners.GeneralListeners;
import me.Neoblade298.NeoProfessions.Listeners.MasonListeners;
import me.Neoblade298.NeoProfessions.Listeners.SkillapiListeners;
import me.Neoblade298.NeoProfessions.Methods.BlacksmithMethods;
import me.Neoblade298.NeoProfessions.Methods.CulinarianMethods;
import me.Neoblade298.NeoProfessions.Methods.MasonMethods;
import me.Neoblade298.NeoProfessions.Methods.ProfessionsMethods;
import me.Neoblade298.NeoProfessions.Methods.StonecutterMethods;
import me.Neoblade298.NeoProfessions.Recipes.CulinarianRecipes;
import me.Neoblade298.NeoProfessions.Utilities.MasonUtils;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

public class Main extends JavaPlugin implements Listener {
	public boolean debug = false;
	public boolean isInstance = false;

	private Economy econ;
	private Permission perms;
	private Chat chat;
	private YamlConfiguration cfg;

	static String sqlUser = "neoblade298";
	static String sqlPass = "7H56480g09&Z01pz";
	static String connection = "jdbc:mysql://66.70.180.136:3306/MLMC?useSSL=false";
	static Properties properties = new Properties();

	public BlacksmithMethods blacksmithMethods;
	public MasonMethods masonMethods;
	public StonecutterMethods stonecutterMethods;
	public CulinarianMethods culinarianMethods;
	public ProfessionsMethods professionsMethods;

	public CulinarianRecipes culinarianRecipes;
	public CulinarianListeners culinarianListeners;

	public MasonListeners masonListeners;
	public MasonUtils masonUtils;

	public GeneralListeners generalListeners;
	
	public CurrencyManager cManager;
	
	public me.neoblade298.neogear.Main neogear;

	public void onEnable() {
		super.onEnable();
		Bukkit.getServer().getLogger().info("NeoProfessions Enabled");

		// Setup vault
		if (!setupEconomy()) {
			this.getLogger().severe("Disabled due to no Vault dependency found!");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		this.setupPermissions();
		this.setupChat();
		
		neogear = (me.neoblade298.neogear.Main) Bukkit.getPluginManager().getPlugin("NeoGear");
		
		// Configuration// Save config if doesn't exist
		File file = new File(getDataFolder(), "config.yml");
		if (!file.exists()) {
			saveResource("config.yml", false);
		}
		this.cfg = YamlConfiguration.loadConfiguration(file);
		isInstance = cfg.getBoolean("is-instance");

		// Set up required listeners
		getServer().getPluginManager().registerEvents(new BlacksmithListeners(this), this);
		getServer().getPluginManager().registerEvents(new CulinarianListeners(this), this);
		getServer().getPluginManager().registerEvents(new SkillapiListeners(this), this);
		if (!isInstance) {
			// Currency
			cManager = new CurrencyManager(this);
		}

		// Must be after currencymanager is created but before masonMethods
		masonListeners = new MasonListeners(this);
			
		if (!isInstance) {
			// NeoGear
			neogear = (me.neoblade298.neogear.Main) Bukkit.getServer().getPluginManager().getPlugin("NeoGear");
	
			masonUtils = new MasonUtils();
	
			// Connect method classes to main
			blacksmithMethods = new BlacksmithMethods(this);
			masonMethods = new MasonMethods(this);
			stonecutterMethods = new StonecutterMethods(this);
			culinarianMethods = new CulinarianMethods(this);
			professionsMethods = new ProfessionsMethods(this);
	
			// Command listeners for all classes
			this.getCommand("blacksmith").setExecutor(new BlacksmithCommands(this));
			this.getCommand("mason").setExecutor(new MasonCommands(this));
			this.getCommand("scutter").setExecutor(new StonecutterCommands(this));
			this.getCommand("culinarian").setExecutor(new CulinarianCommands(this));
			this.getCommand("prof").setExecutor(new NeoprofessionsCommands(this, blacksmithMethods,
					stonecutterMethods, culinarianMethods, masonMethods));
			
			
			// Setup Event Listeners
			getServer().getPluginManager().registerEvents(new GeneralListeners(this), this);
	
			// Setup recipes (make sure the recipes haven't been added before)
			culinarianRecipes = new CulinarianRecipes(this);
			Iterator<Recipe> iter = getServer().recipeIterator();
			boolean canAdd = true;
			while (iter.hasNext()) {
				Recipe r = iter.next();
				if (r instanceof ShapedRecipe && ((ShapedRecipe) r).getKey().getKey().equalsIgnoreCase("Vodka")) {
					canAdd = false;
				}
			}
			if (canAdd) {
				for (Recipe recipe : culinarianRecipes.getRecipes()) {
					Bukkit.addRecipe(recipe);
				}
			}
		}
		getServer().getPluginManager().registerEvents(masonListeners, this);


		// Setup charm timer
		Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
			public void run() {
				for (World w : Bukkit.getWorlds()) {
					if (w.getName().equalsIgnoreCase("Argyll") || w.getName().equalsIgnoreCase("ClassPVP")) {
						for (Player p : w.getPlayers()) {
							// First check what charms the player has
							ItemStack item = p.getInventory().getItemInMainHand();
							// Then make sure it's not a literal hunger charm
							if (item.getType() != Material.PRISMARINE_CRYSTALS) {
								String charmLine = null;
								if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
									for (String line : item.getItemMeta().getLore()) {
										if (line.contains("Hunger Charm")) {
											charmLine = line;
											break;
										}
									}
								}
								if (charmLine != null) {
									p.setFoodLevel(19);
								}
							}
						}
					}
				}
			}
		}, 0, 20L);
		
		// SQL
		properties.setProperty("useSSL", "false");
		properties.setProperty("user", sqlUser);
		properties.setProperty("password", sqlPass);
		properties.setProperty("useSSL", "false");
	}

	public void onDisable() {
		super.onDisable();
		try {
			cManager.cleanup();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Bukkit.getServer().getLogger().info("NeoProfessions Disabled");
	}

	private boolean setupEconomy() {
		if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
			return false;
		}

		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = rsp.getProvider();
		return econ != null;
	}

	private boolean setupChat() {
		RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
		chat = rsp.getProvider();
		return chat != null;
	}

	private boolean setupPermissions() {
		RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
		perms = rsp.getProvider();
		return perms != null;
	}

	public Economy getEconomy() {
		return econ;
	}

	public Permission getPermissions() {
		return perms;
	}

	public Chat getChat() {
		return chat;
	}
}
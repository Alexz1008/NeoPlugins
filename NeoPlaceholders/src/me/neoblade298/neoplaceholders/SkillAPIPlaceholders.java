package me.neoblade298.neoplaceholders;

import java.text.DecimalFormat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.classes.RPGClass;
import com.sucy.skill.api.player.PlayerClass;
import com.sucy.skill.api.player.PlayerData;
import com.sucy.skill.api.util.FlagManager;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import be.maximvdw.placeholderapi.PlaceholderReplaceEvent;
import be.maximvdw.placeholderapi.PlaceholderReplacer;
import me.mrmaurice.cts.CrossTownyServer;

public class SkillAPIPlaceholders {
	private Main main;
	boolean hasCts = false;
	CrossTownyServer cts = null;
	
	public SkillAPIPlaceholders (Main main) {
		this.main = main;
		cts = (CrossTownyServer) Bukkit.getPluginManager().getPlugin("CrossTownyServer");
		if (cts != null) {
			hasCts = true;
		}
	}

	public void registerPlaceholders() {
		DecimalFormat df = new DecimalFormat("0");
		DecimalFormat df1k = new DecimalFormat("0.00k");
		DecimalFormat df10k = new DecimalFormat("00.0k");
		DecimalFormat pct = new DecimalFormat("00.00");

		// Current health placeholder for animatednames
		PlaceholderAPI.registerPlaceholder(this.main, "MLMCHealth", new PlaceholderReplacer() {
			@Override
			public String onPlaceholderReplace(PlaceholderReplaceEvent e) {
				boolean online = e.isOnline();
				Player p = e.getPlayer();
				String placeholder = "Loading...";

				if (online && p != null) {
					return "" + (int) p.getHealth();
				}
				return placeholder;
			}
		});
		// Current health placeholder
		PlaceholderAPI.registerPlaceholder(this.main, "CurrentHealth", new PlaceholderReplacer() {
			@Override
			public String onPlaceholderReplace(PlaceholderReplaceEvent e) {
				boolean online = e.isOnline();
				Player p = e.getPlayer();
				String placeholder = "Loading...";
				

				if (online && p != null) {
					double health = p.getHealth();
					placeholder = "";
					// Check if cursed
					if (FlagManager.hasFlag(p, "curse")) {
						placeholder = "�8";
					}
					else if (FlagManager.hasFlag(p, "stun") || FlagManager.hasFlag(p, "root") || FlagManager.hasFlag(p, "silence")) {
						placeholder = "�b";
					}
					
					if (health >= 10000) {
						placeholder += df10k.format(health / 1000);
					}
					else if (health >= 1000) {
						placeholder += df1k.format(health / 1000);
					}
					else {
						placeholder += df.format(health);
					}
					return placeholder;
				}
				return placeholder;
			}
		});

		// Max health placeholder
		PlaceholderAPI.registerPlaceholder(this.main, "MaxHealth", new PlaceholderReplacer() {
			@Override
			public String onPlaceholderReplace(PlaceholderReplaceEvent e) {
				boolean online = e.isOnline();
				Player p = e.getPlayer();
				String placeholder = "Loading...";

				if (online && p != null) {
					@SuppressWarnings("deprecation")
					double health = p.getMaxHealth();
					if (health >= 10000) {
						placeholder = df10k.format(health / 1000);
					}
					else if (health >= 1000) {
						placeholder = df1k.format(health / 1000);
					}
					else {
						placeholder = df.format(health);
					}
					return placeholder;
				}
				return placeholder;
			}
		});

		// Exp placeholder
		PlaceholderAPI.registerPlaceholder(this.main, "CurrentExp", new PlaceholderReplacer() {
			@Override
			public String onPlaceholderReplace(PlaceholderReplaceEvent e) {
				boolean online = e.isOnline();
				Player p = e.getPlayer();
				String placeholder = "";
				

				try {
					if (online && p != null) {
						if (!hasCts || !cts.hasManager(p)) {
							try {
								return "" + SkillAPI.getPlayerData(p).getClass("class").getExp();
							}
							catch (NullPointerException ex) {
								return placeholder;
							}
						}
					}
				}
				catch (Exception ex) {}
				return placeholder;
			}
		});

		// Required exp placeholder
		PlaceholderAPI.registerPlaceholder(this.main, "RequiredExp", new PlaceholderReplacer() {
			@Override
			public String onPlaceholderReplace(PlaceholderReplaceEvent e) {
				boolean online = e.isOnline();
				Player p = e.getPlayer();
				String placeholder = "";

				try {
					if (online && p != null) {
						if (!hasCts || !cts.hasManager(p)) {
							try {
								return "" + SkillAPI.getPlayerData(p).getClass("class").getRequiredExp();
							}
							catch (NullPointerException ex) {
								return placeholder;
							}
						}
					}
				}
				catch (Exception ex) {}
				return placeholder;
			}
		});

		// Current mana placeholder
		PlaceholderAPI.registerPlaceholder(this.main, "CurrentMana", new PlaceholderReplacer() {
			@Override
			public String onPlaceholderReplace(PlaceholderReplaceEvent e) {
				boolean online = e.isOnline();
				Player p = e.getPlayer();
				String placeholder = "0";

				try {
					if (online && p != null) {
						if (!hasCts || !cts.hasManager(p)) {
							try {
								return df.format(SkillAPI.getPlayerData(p).getMana());
							}
							catch (NullPointerException ex) {
								return placeholder;
							}
						}
					}
				}
				catch (Exception ex) {}
				return placeholder;
			}
		});

		// Max mana placeholder
		PlaceholderAPI.registerPlaceholder(this.main, "MaxMana", new PlaceholderReplacer() {
			@Override
			public String onPlaceholderReplace(PlaceholderReplaceEvent e) {
				boolean online = e.isOnline();
				Player p = e.getPlayer();
				String placeholder = "0";

				try {
					if (online && p != null) {
						if (!hasCts || !cts.hasManager(p)) {
							try {
								return df.format(SkillAPI.getPlayerData(p).getMaxMana());
							}
							catch (NullPointerException ex) {
								return placeholder;
							}
						}
					}
				}
				catch (Exception ex) {}
				return placeholder;
			}
		});

		// Next level % placeholder
		PlaceholderAPI.registerPlaceholder(this.main, "NextLevel", new PlaceholderReplacer() {
			@Override
			public String onPlaceholderReplace(PlaceholderReplaceEvent e) {
				boolean online = e.isOnline();
				Player p = e.getPlayer();
				String placeholder = "N/A";

				try {
					if (online && p != null) {
						if (!hasCts || !cts.hasManager(p)) {
							PlayerData pData = SkillAPI.getPlayerData(p);
							if (pData != null) {
								PlayerClass pClass = pData.getClass("class");
								if (pClass != null) {
									RPGClass rClass = pClass.getData();
									if (rClass != null) {
										int max = rClass.getMaxLevel();
										int lvl = pClass.getLevel();
										if (max != lvl) {
											double reqExp = pClass.getRequiredExp();
											double currExp = pClass.getExp();
											placeholder = pct.format((currExp / reqExp) * 100) + "%";
											return placeholder;
										}
									}
								}
							}
						}
					}
				}
				catch (Exception ex) {}
				return placeholder;
			}
		});
		
		// Current level placeholder
		PlaceholderAPI.registerPlaceholder(this.main, "CurrentLevel", new PlaceholderReplacer() {
			@Override
			public String onPlaceholderReplace(PlaceholderReplaceEvent e) {
				boolean online = e.isOnline();
				Player p = e.getPlayer();
				String placeholder = "N/A";

				try {
					if (online && p != null) {
						if (!hasCts || !cts.hasManager(p)) {
							try {
								return "" + SkillAPI.getPlayerData(p).getClass("class").getLevel();
							}
							catch (NullPointerException ex) {
								return placeholder;
							}
						}
					}
				}
				catch (Exception ex) {}
				return placeholder;
			}
		});
		
		// Resource placeholder
		PlaceholderAPI.registerPlaceholder(this.main, "Resource", new PlaceholderReplacer() {
			@Override
			public String onPlaceholderReplace(PlaceholderReplaceEvent e) {
				boolean online = e.isOnline();
				Player p = e.getPlayer();
				String placeholder = "MP";
				
				try {
					if (online && p != null) {
						if (!hasCts || !cts.hasManager(p)) {
							try {
								return SkillAPI.getPlayerData(p).getClass("class").getData().getManaName();
							}
							catch (Exception ex) {
								return placeholder;
							}
						}
					}
				}
				catch (Exception ex) {}
				return placeholder;
			}
		});
	}
}
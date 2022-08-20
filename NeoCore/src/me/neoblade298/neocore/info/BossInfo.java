package me.neoblade298.neocore.info;

import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

import io.lumine.mythic.api.mobs.MobManager;
import io.lumine.mythic.bukkit.MythicBukkit;
import me.neoblade298.neocore.util.Util;

public class BossInfo {
	private String key, display, displayWithLvl, displayWithLvlRounded;
	private int level;
	private List<String> healthComponents;
	private double health = 0;
	
	public BossInfo(ConfigurationSection cfg) {
		this.key = cfg.getName();
		this.display = Util.translateColors("&c" + cfg.getString("display", "DEFAULT"));
		this.level = cfg.getInt("level");
		int levelRounded = level - (level % 5);
		this.healthComponents = cfg.getStringList("health-components");
		this.displayWithLvl = "§6[Lv " + level + "] " + display;
		this.displayWithLvlRounded = "§6[Lv " + levelRounded + "] " + display;
	}

	public String getKey() {
		return key;
	}

	public String getDisplay() {
		return display;
	}
	
	public String getDisplayWithLevel(boolean roundDown) {
		return roundDown ? displayWithLvlRounded : displayWithLvl;
	}

	public int getLevel(boolean roundDown) {
		return roundDown ? level - (level % 5) : level;
	}

	public double getTotalHealth() {
		if (health == 0) {
			MobManager mm = MythicBukkit.inst().getMobManager();
			for (String mob : healthComponents) {
				health += mm.getMythicMob(mob).get().getHealth().get();
			}
		}
		return health;
	}
}

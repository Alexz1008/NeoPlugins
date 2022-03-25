package me.Neoblade298.NeoProfessions.Managers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import me.Neoblade298.NeoProfessions.Professions;
import me.Neoblade298.NeoProfessions.Minigames.Minigame;
import me.Neoblade298.NeoProfessions.Minigames.MinigameDrops;
import me.Neoblade298.NeoProfessions.Minigames.MinigameParameters;
import me.Neoblade298.NeoProfessions.Storage.StoredItem;

public class MinigameManager {
	public static Professions main;
	private static HashMap<Integer, Minigame> games = new HashMap<Integer, Minigame>();
	
	public MinigameManager(Professions main) {
		MinigameManager.main = main;
		
		loadMinigames(new File(main.getDataFolder(), "minigames"));
	}
	
	private void loadMinigames(File dir) {
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				loadMinigames(file);
			}
			else {
				YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
				for (String key : yaml.getKeys(false)) {
					ConfigurationSection itemCfg = yaml.getConfigurationSection(key);
					String display = itemCfg.getString("display");
					String type = itemCfg.getString("type");
					int numDrops = itemCfg.getInt("num-drops");
					int difficulty = itemCfg.getInt("difficulty");
					
					// Parse drops
					ArrayList<String> drops = (ArrayList<String>) itemCfg.getStringList("drops");
					ArrayList<MinigameDrops> parsed = new ArrayList<MinigameDrops>();
					for (String line : drops) {
						String[] lineArgs = line.split(" ");
						int itemId = 0;
						int minAmt = 1;
						int maxAmt = 1;
						int weight = 1;
						int exp = 1;
						for (String lineArg : lineArgs) {
							String[] args = lineArg.split(":");
							switch (args[0]) {
							case "id":
								itemId = Integer.parseInt(args[1]);
								break;
							case "amount":
								String[] amts = args[1].split("-");
								minAmt = Integer.parseInt(amts[0]);
								maxAmt = amts.length == 2 ? Integer.parseInt(amts[1]) : minAmt;
								break;
							case "weight":
								weight = Integer.parseInt(args[1]);
								break;
							case "exp":
								exp = Integer.parseInt(args[1]);
								break;
							}
						}
						StoredItem sitem = StorageManager.getItem(itemId);
						sitem.addSource(display, false);
						parsed.add(new MinigameDrops(sitem, minAmt, maxAmt, weight, exp));
					}
					games.put(Integer.parseInt(key), new Minigame(display, type, parsed, numDrops, difficulty));
				}
			}
		}
	}
	
	public static void startMinigame(Player p, int key) {
		games.get(key).startMinigame(p, null);
	}
	
	public static void startMinigame(Player p, MinigameParameters params) {
		games.get(params.getId()).startMinigame(p, params);
	}
}

package me.neoblade298.neosapiaddons;

import java.util.HashMap;
import java.util.HashSet;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class HungerController implements Listener {
	
	HashSet<Player> sprintingPlayers;
	HashMap<Player, Integer> hunger;
	
	// How many seconds it takes for food to decrease by 1
	final int FOOD_SCALE = 10;
	
	private Main main;
	public HungerController(Main main) {
		this.main = main;
		sprintingPlayers = new HashSet<Player>();
		hunger = new HashMap<Player, Integer>();
		
		BukkitRunnable foodTimer = new BukkitRunnable() {
			public void run() {
				for (Player p : sprintingPlayers) {
					if (hunger.containsKey(p)) {
						int pHunger = hunger.get(p);
						if (pHunger % FOOD_SCALE == 0) {
							p.setFoodLevel(pHunger / FOOD_SCALE);
						}
						hunger.put(p, hunger.get(p) - 1);
					}
					else {
						hunger.put(p, (p.getFoodLevel() * FOOD_SCALE) - 1);
					}
				}
			}
		};
		foodTimer.runTaskTimerAsynchronously(main, 0, 20);
	}

	@EventHandler
	public void onHungerChange(FoodLevelChangeEvent e) {
		Player p = (Player) e.getEntity();
		int newFoodLevel = Math.min(20, e.getFoodLevel());
		if (main.isQuestWorld(p.getWorld())) {
			if (newFoodLevel < p.getFoodLevel()) {
				e.setCancelled(true);
				return;
			}
			else {
				if (hunger.containsKey(p)) {
					int oldHunger = hunger.get(p);
					hunger.put(p, oldHunger + ((newFoodLevel - p.getFoodLevel()) * FOOD_SCALE));
				}
				else {
					hunger.put(p, newFoodLevel * FOOD_SCALE);
				}
			}
		}
	}
	
	@EventHandler
	public void onHungerRegen(EntityRegainHealthEvent e) {
		if (e.getEntity() instanceof Player && main.isQuestWorld(e.getEntity().getWorld())) {
			if (e.getRegainReason().equals(RegainReason.SATIATED)) {
				e.setCancelled(true);
				return;
			}
		}
	}
	
	@EventHandler
	public void onSprint(PlayerToggleSprintEvent e) {
		Player p = e.getPlayer();
		if (main.isQuestWorld(p.getWorld())) {
			if (e.isSprinting()) {
				sprintingPlayers.add(p);
			}
			else {
				sprintingPlayers.remove(p);
			}
		}
	}
}

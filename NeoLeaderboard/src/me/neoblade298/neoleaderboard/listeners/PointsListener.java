package me.neoblade298.neoleaderboard.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;

import me.neoblade298.neobossinstances.Boss;
import me.neoblade298.neobossinstances.BossInstances;
import me.neoblade298.neocore.bungee.PluginMessageEvent;
import me.neoblade298.neoleaderboard.NeoLeaderboard;
import me.neoblade298.neoleaderboard.points.PlayerPointType;
import me.neoblade298.neoleaderboard.points.PointsManager;

public class PointsListener implements Listener {
	public static final double BLOCK_EDIT_POINTS = 0.002;
	public static final double KILL_PLAYER_POINTS = 5;
	public static final double KILL_BOSS_BASE_POINTS = 0.01;
	public static final double MINUTES_ONLINE_POINTS = 0.1;
	private static final HashMap<UUID, Long> deathCooldowns = new HashMap<UUID, Long>();
	private static final long DEATH_COOLDOWN = 600000L;
	
	private HashMap<Player, Long> playtime = new HashMap<Player, Long>();
	
	public PointsListener() {
		new BukkitRunnable() {
			public void run() {
				for (Player p : Bukkit.getOnlinePlayers()) {
					PointsManager.addPlayerPoints(p.getUniqueId(), MINUTES_ONLINE_POINTS, PlayerPointType.PLAYTIME);
				}
			}
		}.runTaskTimerAsynchronously(NeoLeaderboard.inst(), 0L, 20 * 60);
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		PointsManager.addPlayerPoints(e.getPlayer().getUniqueId(), BLOCK_EDIT_POINTS, PlayerPointType.EDIT_BLOCK);
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		PointsManager.addPlayerPoints(e.getPlayer().getUniqueId(), BLOCK_EDIT_POINTS, PlayerPointType.EDIT_BLOCK);
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerKill(PlayerDeathEvent e) {
		Player victim = e.getEntity();
		Player killer = victim.getKiller();

		if (killer == null) return;
		TownyAPI api = TownyAPI.getInstance();
		Resident vr = api.getResident(victim);
		Resident kr = api.getResident(killer);
		Nation vn = vr.getNationOrNull();
		Nation kn = kr.getNationOrNull();

		if (vn == null || kn == null) return;
		if (vn == kn) return;
		if (deathCooldownActive(victim.getUniqueId())) return;
		deathCooldowns.put(victim.getUniqueId(), System.currentTimeMillis() + DEATH_COOLDOWN);
		PointsManager.addPlayerPoints(killer.getUniqueId(), KILL_PLAYER_POINTS, PlayerPointType.KILL_PLAYER);
	}

	private boolean deathCooldownActive(UUID uuid) {
		if (!deathCooldowns.containsKey(uuid)) return false;
		return System.currentTimeMillis() > deathCooldowns.get(uuid);
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		playtime.put(e.getPlayer(), System.currentTimeMillis());
	}
	
	@EventHandler
	public void onLeave(PlayerQuitEvent e) {
		handleLeave(e.getPlayer());
	}
	
	@EventHandler
	public void onKick(PlayerKickEvent e) {
		handleLeave(e.getPlayer());
	}
	
	private void handleLeave(Player p) {
		// Does nothing atm
	}

	@EventHandler
	public void onPluginMessage(PluginMessageEvent e) {
		String subchannel = e.getChannel();
		
		if (!subchannel.startsWith("leaderboard") && !subchannel.equals("neocore_bosskills")) return;
		
		if (subchannel.equals("neocore_bosskills")) handleBossKillPM(e.getMessages());
		else if (subchannel.equals("leaderboard_playtime")) handlePlaytimePM(e.getMessages());
	}
	
	// UUID, boss
	private void handleBossKillPM(ArrayList<String> msgs) {
		UUID uuid = UUID.fromString(msgs.get(0));
		String boss = msgs.get(1);
		Boss b = BossInstances.getBoss(boss);
		if (b != null) {
			String ph = b.getPlaceholder();
			double lv = Integer.parseInt(b.getPlaceholder().substring(ph.indexOf("Lv ") + 3, ph.indexOf(']'))); 
			PointsManager.addPlayerPoints(uuid, KILL_BOSS_BASE_POINTS * lv, PlayerPointType.KILL_BOSS);
		}
	}
	
	// UUID, long timeOnline
	private void handlePlaytimePM(ArrayList<String> msgs) {
		UUID uuid = UUID.fromString(msgs.get(0));
		int minutes = Integer.parseInt(msgs.get(1));
		PointsManager.addPlayerPoints(uuid, MINUTES_ONLINE_POINTS * minutes, PlayerPointType.PLAYTIME);
	}
}

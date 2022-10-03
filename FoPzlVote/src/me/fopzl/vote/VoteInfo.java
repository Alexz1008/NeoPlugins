package me.fopzl.vote;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

public class VoteInfo {
	public Map<UUID, VoteStats> playerStats;
	public Map<UUID, Map<String, Integer>> queuedRewards;

	private static VoteInfo instance;
	
	public VoteInfo() {
		playerStats = new HashMap<UUID, VoteStats>();
		queuedRewards = new HashMap<UUID, Map<String, Integer>>();
		
		instance = this;
	}
	
	public static VoteInfo getInstance() {
		return instance;
	}
	
	public VoteStats getStats(Player p) {
		UUID uuid = p.getUniqueId();
		if(playerStats.containsKey(uuid)) {
			return playerStats.get(uuid);
		} else {
			VoteStats vs = new VoteStats();
			playerStats.put(uuid, vs);
			return vs;
		}
	}
}
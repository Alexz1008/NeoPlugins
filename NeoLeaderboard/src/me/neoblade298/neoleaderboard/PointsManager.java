package me.neoblade298.neoleaderboard;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;

import me.neoblade298.neocore.NeoCore;
import me.neoblade298.neocore.io.IOComponent;

public class PointsManager implements IOComponent {
	private static HashMap<UUID, PlayerPoints> playerPoints = new HashMap<UUID, PlayerPoints>();
	private static HashMap<UUID, NationEntry> nationEntries = new HashMap<UUID, NationEntry>();
	private static HashMap<UUID, Long> lastSaved = new HashMap<UUID, Long>();
	private static final double MAX_PLAYER_CONTRIBUTION = 1000;
	
	public PointsManager() {
		// Ground rules:
		// PlayerPoints only exists once a player gets their first points, but it is created even if the player gets them while offline
		// PlayerPoints persists through login/logout
		// Nationent exists on startup, listens to nation create and delete
		// Nationent only increments numContributors if PlayerPoints was 0
		
		new BukkitRunnable() {
			public void run() {
				try {
					// Initialize all nations
					Statement stmt = NeoCore.getStatement();
					ResultSet rs = stmt.executeQuery("SELECT * FROM neoleaderboard_nations");
					while (rs.next()) {
						UUID uuid = UUID.fromString(rs.getString(1));
						nationEntries.put(uuid, new NationEntry(uuid, rs.getInt(3)));
					}
					
					// Add nationwide points
					rs = stmt.executeQuery("SELECT * FROM neoleaderboard_points WHERE isNation = 1 AND isNationent = 1;");
					while (rs.next()) {
						UUID uuid = UUID.fromString(rs.getString(1));
						NationEntry np = nationEntries.get(uuid);
						np.addNationPoints(rs.getDouble(3), NationPointType.valueOf(rs.getString(4)));
					}
					
					// Add player points
					rs = stmt.executeQuery("SELECT * FROM neoleaderboard_points WHERE isNation = 1 AND isNationent = 0;");
					while (rs.next()) {
						UUID uuid = UUID.fromString(rs.getString(1));
						NationEntry ne = nationEntries.get(uuid);
						TownyAPI api = TownyAPI.getInstance();
						Resident r = api.getResident(uuid);
						Town town = api.getResidentTownOrNull(r);
						ne.addPlayerPoints(rs.getDouble(3), PlayerPointType.valueOf(rs.getString(4)), town);
					}
				}
				catch (Exception e) {
					Bukkit.getLogger().log(Level.WARNING, "[NeoLeaderboard] Failed to initialize nationwide points");
					e.printStackTrace();
				}
			}
		}.runTaskAsynchronously(NeoLeaderboard.inst());
	}
	
	public static void handleLeaveTown(Nation n, Town town, Resident r) {
		if (!playerPoints.containsKey(r.getUUID())) return;
		
		try {
			nationEntries.get(n.getUUID()).removePlayer(playerPoints.get(r.getUUID()), town);
			Statement delete = NeoCore.getStatement();
			playerPoints.remove(r.getUUID()).clearPoints(delete);
			delete.executeBatch();
		}
		catch (Exception e) {
			Bukkit.getLogger().warning("[NeoLeaderboard] Failed to handle player " + r.getName() + " leaving nation");
			e.printStackTrace();
		}
	}
	
	public static void handleLeaveNation(Nation n, Town town) {
		new BukkitRunnable() {
			public void run() {
				try {
					Statement delete = NeoCore.getStatement();
					for (Resident res : town.getResidents()) {
						UUID uuid = res.getUUID();
						
						if (playerPoints.containsKey(uuid)) {
							playerPoints.remove(uuid).clearPoints(delete);
							nationEntries.get(n.getUUID()).removeTown(town);
						}
					}
					delete.executeBatch();
				}
				catch (Exception e) {
					Bukkit.getLogger().warning("[NeoLeaderboard] Failed to handle town " + town.getName() + " leaving nation");
					e.printStackTrace();
				}
			}
		}.runTaskAsynchronously(NeoLeaderboard.inst());
	}
	
	public static void takePlayerPoints(UUID uuid, double amount, PlayerPointType type) {
		addPlayerPoints(uuid, -amount, type);
	}
	
	public static void addPlayerPoints(UUID uuid, double amount, PlayerPointType type) {
		addPlayerPoints(uuid, amount, type, Bukkit.getPlayer(uuid) != null);
	}
	
	public static void addPlayerPoints(UUID uuid, double amount, PlayerPointType type, boolean online) {
		TownyAPI api = TownyAPI.getInstance();
		Resident r = api.getResident(uuid);
		Nation n = api.getResidentNationOrNull(r);
		Town t = api.getResidentTownOrNull(r);
		if (n == null) return;
		
		NationEntry nent = nationEntries.get(n.getUUID());
		PlayerPoints ppoints = playerPoints.get(uuid);
		
		if (online) {
			if (ppoints == null) {
				ppoints = new PlayerPoints(uuid);
				nent.incrementContributors();
				playerPoints.put(uuid, ppoints);
			}
			nent.addPlayerPoints(amount, type, t);
			ppoints.addPoints(amount, type);
		}
		else {
			try {
				Statement stmt = NeoCore.getStatement();
				ResultSet rs = stmt.executeQuery("SELECT * FROM neoleaderboard_points WHERE uuid = '" + uuid + "';");
				double current = 0;
				
				// If this was a player's first points
				if (!rs.next()) {
					nent.incrementContributors();
				}
				else {
					// Check their current points and add the new points
					rs = stmt.executeQuery("SELECT * FROM neoleaderboard_points WHERE uuid = '" + uuid + "' AND category = '" + type + "';");
					current = rs.next() ? rs.getDouble(4) : 0;
				}

				stmt.executeUpdate("REPLACE INTO neoleaderboard_points VALUES ('"
						+ uuid + "',0,0,'" + type + "'," + (current + amount) + ");");
				nent.addPlayerPoints(amount, type, t);
			}
			catch (Exception e) {
				Bukkit.getLogger().warning("[NeoLeaderboard] Failed to give points to offline player " + uuid + " for type " + type + ", amount " + amount);
			}
		}
	}
	
	public static void addNationent(UUID uuid, double amount, NationPointType type) {
		nationEntries.get(uuid).addNationPoints(amount, type);
	}
	
	private void saveNation(Nation n, Statement insert) {
		new BukkitRunnable() {
			public void run() {
				// Don't save same nation more than once every 10 seconds
				NationEntry nent = nationEntries.getOrDefault(n.getUUID(), new NationEntry(n.getUUID()));
				if (lastSaved.getOrDefault(nent.getUuid(), 0L) + 10000L > System.currentTimeMillis()) {
					return;
				}
				lastSaved.put(nent.getUuid(), System.currentTimeMillis());
				
				try {
					HashMap<NationPointType, Double> points = nent.getAllNationPoints();
					HashMap<PlayerPointType, Double> ppoints = nent.getAllPlayerPoints();

					insert.addBatch("REPLACE INTO neoleaderboard_nations VALUES ('"
										+ nent.getUuid() + "','" + n.getName() + "'," + nent.getContributors() + ");");
					for (Entry<NationPointType, Double> e : points.entrySet()) {
						insert.addBatch("REPLACE INTO neoleaderboard_points VALUES ('"
											+ nent.getUuid() + "',1,1,'" + e.getKey() + "'," + e.getValue() + ");");
					}

					for (Entry<PlayerPointType, Double> e : ppoints.entrySet()) {
						insert.addBatch("REPLACE INTO neoleaderboard_points VALUES ('"
											+ nent.getUuid() + "',1,0,'" + e.getKey() + "'," + e.getValue() + ");");
					}
				}
				catch (Exception e) {
					Bukkit.getLogger().warning("[NeoLeaderboard] Failed to save nation " + n.getName() + " on cleanup.");
					e.printStackTrace();
				}
			}
		}.runTaskAsynchronously(NeoLeaderboard.inst());
	}

	@Override
	public void cleanup(Statement insert, Statement delete) {
		TownyUniverse tu = TownyUniverse.getInstance();
		for (NationEntry nent : nationEntries.values()) {
			saveNation(tu.getNation(nent.getUuid()), insert);
		}
	}

	@Override
	public String getKey() {
		return "PointsManager";
	}

	@Override
	public void loadPlayer(Player arg0, Statement arg1) {}

	@Override
	public void preloadPlayer(OfflinePlayer p, Statement stmt) {
		if (playerPoints.containsKey(p.getUniqueId())) {
			return;
		}
		
		try {
			PlayerPoints ppoints = new PlayerPoints(p.getUniqueId());
			ResultSet rs = stmt.executeQuery("SELECT * FROM neoleaderboard_points WHERE uuid = '" + p.getUniqueId() + "';");
			while (rs.next()) {
				ppoints.addPoints(rs.getDouble(3), PlayerPointType.valueOf(rs.getString(4)));
			}
			
			if (!ppoints.isEmpty()) {
				playerPoints.put(p.getUniqueId(), ppoints);
			}
		}
		catch (Exception e) {
			Bukkit.getLogger().log(Level.WARNING, "[NeoLeaderboard] Failed to load points for player " + p.getName());
			e.printStackTrace();
		}
	}

	@Override
	public void savePlayer(Player p, Statement insert, Statement delete) {
		if (!playerPoints.containsKey(p.getUniqueId())) {
			return;
		}
		try {
			savePlayerData(p.getUniqueId(), insert);
			
			// Also save the nation so we don't get issues if server crash
			TownyAPI api = TownyAPI.getInstance();
			Resident r = api.getResident(p);
			Nation n = api.getResidentNationOrNull(r);
			if (n != null) {
				saveNation(n, insert); // Limited to once every 10 seconds per nation
			}
		}
		catch (Exception e) {
			Bukkit.getLogger().warning("[NeoLeaderboard] Failed to save player " + p.getName() + " on cleanup.");
			e.printStackTrace();
		}
	}
	
	private void savePlayerData(UUID uuid, Statement insert) throws SQLException {
		PlayerPoints ppoints = playerPoints.get(uuid);
		for (Entry<PlayerPointType, Double> e : ppoints.getAllPoints().entrySet()) {
			insert.addBatch("REPLACE INTO neoleaderboard_points VALUES ('"
								+ ppoints.getUuid() + "',0,'" + e.getKey() + "'," + e.getValue() + ");");
		}
	}
	
	public static double getMaxContribution() {
		return MAX_PLAYER_CONTRIBUTION;
	}
}

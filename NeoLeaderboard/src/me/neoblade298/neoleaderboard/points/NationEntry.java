package me.neoblade298.neoleaderboard.points;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.UUID;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;

public class NationEntry implements Comparable<NationEntry> {
	private UUID uuid;
	private Nation nation;
	private HashMap<NationPointType, Double> nationPoints = new HashMap<NationPointType, Double>();
	private HashMap<PlayerPointType, Double> playerPoints = new HashMap<PlayerPointType, Double>();
	private HashMap<UUID, TownEntry> townPoints = new HashMap<UUID, TownEntry>();
	private TreeSet<TownEntry> topTowns = new TreeSet<TownEntry>();;
	private double totalNationPoints, totalPlayerPoints;
	private int numContributors;
	
	public NationEntry(UUID uuid) {
		this(uuid, 0);
	}
	
	public NationEntry(UUID uuid, int numContributors) {
		this.uuid = uuid;
		this.numContributors = numContributors;
		
		this.nation = TownyAPI.getInstance().getNation(uuid);
	}
	
	public void incrementContributors() {
		numContributors++;
	}
	
	public void setNationPoints(double amount, NationPointType type) {
		nationPoints.put(type, amount);
		totalNationPoints = amount;
	}
	
	public void setPlayerPoints(double amount, PlayerPointType type) {
		playerPoints.put(type, amount);
		totalPlayerPoints = amount;
	}
	
	public boolean initializeTown(UUID uuid) {
		return initializeTown(uuid, 0);
	}
	
	public boolean initializeTown(UUID uuid, int contributors) {
		if (townPoints.containsKey(uuid)) {
			return false;
		}
		TownEntry te = new TownEntry(uuid, this.getNation().getUUID(), contributors);
		townPoints.put(uuid, te);
		topTowns.add(te);
		return true;
	}
	
	public void initializeTownPoints(double amount, PlayerPointType type, UUID uuid) {
		// Changes to town points means town uuid must be re-sorted
		TownEntry te = townPoints.getOrDefault(uuid, new TownEntry(uuid, this.getNation().getUUID(), 0));
		topTowns.remove(te);
		
		te.addPlayerPoints(amount, type, uuid);
		townPoints.putIfAbsent(uuid, te);
		
		topTowns.add(te);
	}
	
	public void addNationPoints(double amount, NationPointType type) {
		nationPoints.put(type, nationPoints.getOrDefault(type, 0D) + amount);
		totalNationPoints += amount;
	}
	
	public void takeNationPoints(double amount, NationPointType type) {
		addNationPoints(-amount, type);
	}
	
	public void addPlayerPoints(double amount, PlayerPointType type, Town town, UUID player) {
		playerPoints.put(type, playerPoints.getOrDefault(type, 0D) + amount);
		totalPlayerPoints += amount;
		addTownPoints(amount, type, town, player);
	}
	
	public void takePlayerPoints(double amount, PlayerPointType type, Town town, UUID player) {
		addPlayerPoints(-amount, type, town, player);
	}
	
	public void clearPoints() {
		nationPoints.clear();
	}
	
	public UUID getUuid() {
		return uuid;
	}
	
	public int getContributors() {
		return numContributors;
	}
	
	public HashMap<NationPointType, Double> getAllNationPoints() {
		return nationPoints;
	}
	
	public HashMap<PlayerPointType, Double> getAllPlayerPoints() {
		return playerPoints;
	}
	
	public HashMap<UUID, TownEntry> getAllTownPoints() {
		return townPoints;
	}
	
	public void removePlayer(PlayerEntry ppoints, Town town, UUID player) {
		for (Entry<PlayerPointType, Double> e : ppoints.getContributedPoints().entrySet()) {
			takePlayerPoints(e.getValue(), e.getKey(), town, player);
		}
	}
	
	public void removeTown(Town town) {
		// If town hasn't contributed any points
		if (!townPoints.containsKey(town.getUUID())) {
			return;
		}
		
		// Remove all town points from nation entry
		for (Entry<PlayerPointType, Double> e : townPoints.get(town.getUUID()).getPlayerPoints().entrySet()) {
			playerPoints.put(e.getKey(), playerPoints.get(e.getKey()) - e.getValue());
		}
		townPoints.remove(town.getUUID());
	}
	
	public Nation getNation() {
		return nation;
	}
	
	public void takeTownPoints(double amount, PlayerPointType type, Town town, UUID player) {
		addTownPoints(-amount, type, town, player);
	}
	
	public void addTownPoints(double amount, PlayerPointType type, Town town, UUID player) {
		// Re-sorting happens with PointsManager calling removeFromSort
		TownEntry te = townPoints.getOrDefault(town.getUUID(), new TownEntry(town.getUUID(), this.getNation().getUUID(), 0));
		te.addPlayerPoints(amount, type, player);
		UUID tuuid = town.getUUID();
		townPoints.putIfAbsent(tuuid, te);
		
		topTowns.add(te);
	}
	
	public double getEffectivePoints() {
		return totalNationPoints + PointsManager.calculateEffectivePoints(this, totalPlayerPoints);
	}
	
	public double getTotalPoints() {
		return totalNationPoints + totalPlayerPoints;
	}

	@Override
	public int compareTo(NationEntry o) {
		if (this.getEffectivePoints() > o.getEffectivePoints()) {
			return 1;
		}
		if (this.getEffectivePoints() < o.getEffectivePoints()) {
			return -1;
		}
		else {
			return o.getNation().getName().compareTo(this.getNation().getName());
		}
	}
	
	public TreeSet<TownEntry> getTopTowns() {
		return topTowns;
	}
	
	public void removeFromSort(PlayerEntry pe) {
		if (pe.getTownEntry() != null) {
			topTowns.remove(pe.getTownEntry());
		}
	}
	
	public TownEntry getTownEntry(UUID uuid) {
		return townPoints.get(uuid);
	}
	
	public void clearAllPlayers() {
		for (TownEntry te : topTowns) {
			te.clearAllPlayers();
		}
	}
}	

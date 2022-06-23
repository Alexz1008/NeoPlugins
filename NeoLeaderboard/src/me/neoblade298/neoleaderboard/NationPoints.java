package me.neoblade298.neoleaderboard;

import java.util.HashMap;
import java.util.UUID;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.TownyAPI;

public class NationPoints {
	private UUID uuid;
	private String display;
	private HashMap<NationPointType, Double> nationPoints;
	private HashMap<PlayerPointType, Double> playerPoints;
	
	public NationPoints(UUID uuid) {
		this.uuid = uuid;
		this.display = TownyAPI.getInstance().getNation(uuid).getName();
	}
	
	public void addNationPoints(double amount, NationPointType type) {
		nationPoints.put(type, nationPoints.getOrDefault(type, 0D));
	}
	
	public void takeNationPoints(double amount, NationPointType type) {
		double after = nationPoints.getOrDefault(type, 0D) - amount;
		nationPoints.put(type, Math.max(0, after));
	}
	
	public void addPlayerPoints(double amount, PlayerPointType type) {
		playerPoints.put(type, playerPoints.getOrDefault(type, 0D));
	}
	
	public void takePlayerPoints(double amount, PlayerPointType type) {
		double after = playerPoints.getOrDefault(type, 0D) - amount;
		playerPoints.put(type, Math.max(0, after));
	}
	
	public void clearPoints() {
		nationPoints.clear();
	}
	
	public UUID getUuid() {
		return uuid;
	}
	
	public String getDisplay() {
		return display;
	}
	
	public void setDisplay(String display) {
		this.display = display;
	}
	
	public HashMap<NationPointType, Double> getAllNationPoints() {
		return nationPoints;
	}
	
	public HashMap<PlayerPointType, Double> getAllPlayerPoints() {
		return playerPoints;
	}
}	

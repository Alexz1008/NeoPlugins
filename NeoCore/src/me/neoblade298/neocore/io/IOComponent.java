package me.neoblade298.neocore.io;

import java.sql.Statement;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public interface IOComponent {
	public void savePlayer(Player p, Statement insert, Statement delete);
	public void preloadPlayer(OfflinePlayer p, Statement stmt);
	public void loadPlayer(Player p, Statement stmt);
	public void cleanup(Statement insert, Statement delete);
	public default boolean canSave() { return true; }
	public default boolean canLoad() { return true; }
	public default boolean canPreload() { return true; }
	public default boolean canCleanup() { return true; }
	public String getKey();
}

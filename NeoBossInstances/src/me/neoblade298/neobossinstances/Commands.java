package me.neoblade298.neobossinstances;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.UUID;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class Commands implements CommandExecutor {
	private Main main = null;

	public Commands(Main plugin) {
		main = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
	    if(sender.hasPermission("bossinstances.admin") || sender.isOp()) {
	    	// /boss tp player nameofboss
	    	if (args.length == 3 && args[0].equalsIgnoreCase("tp") && !main.isInstance) {
	    		boolean found = false;
	    		try {
					Connection con = DriverManager.getConnection(Main.connection, Main.sqlUser, Main.sqlPass);
					Statement stmt = con.createStatement();
					ResultSet rs;

		    		// Find available instance
					for (String instance : main.instanceNames) {
						rs = stmt.executeQuery("SELECT * FROM neobossinstances_fights WHERE boss = '" + args[2] + "' AND instance = '" + instance + "';");
						if (!rs.next()) {
							found = true;
	    					String uuid = Bukkit.getPlayer(args[1]).getUniqueId().toString();
	    					main.cooldowns.get(args[2]).put(uuid, System.currentTimeMillis());
				    		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "bungeee send " + args[1] + " " + instance);
				    		
				    		// Wait for everyone to enter, then update sql so the instance still shows as empty until everyone leaves
				    		BukkitRunnable addSql = new BukkitRunnable() {
				    			public void run() {
				    				try {
				    					// Connect
				    					Connection con = DriverManager.getConnection(Main.connection, Main.sqlUser, Main.sqlPass);
				    					Statement stmt = con.createStatement();
				    					
	    								stmt.executeUpdate("INSERT INTO neobossinstances_fights VALUES ('" + uuid + "','" + args[2] + "','" + instance + "');");
				    					con.close();
				    				}
				    				catch (Exception e) {
				    					e.printStackTrace();
				    				}
				    			}
				    		};
				    		addSql.runTaskLater(main, 60L);
							break;
						}
					}
	    		}
	    		catch (Exception e) {
	    			e.printStackTrace();
	    		}
	    		if (!found) {
	    			Bukkit.getPlayer(args[1]).sendMessage("�4[�c�lBosses�4] �7No available instances!");
	    		}
	    		return true;
	    	}
		    // /boss resetcd player boss
		    else if (args.length == 3 && args[0].equalsIgnoreCase("resetcd") && !main.isInstance) {
		    	if (main.cooldowns.get(WordUtils.capitalize(args[2])).containsKey(Bukkit.getPlayer(args[1]).getUniqueId().toString())) {
		    		main.cooldowns.get(WordUtils.capitalize(args[2])).remove(Bukkit.getPlayer(args[1]).getUniqueId().toString());
		    	}
		    }
		    // /boss resetcds player
		    else if (args.length == 2 && args[0].equalsIgnoreCase("resetcds") && !main.isInstance) {
		    	for (String boss : main.cooldowns.keySet()) {
		    		if (main.cooldowns.get(boss).containsKey(Bukkit.getPlayer(args[1]).getUniqueId().toString())) {
		    			main.cooldowns.get(boss).remove(Bukkit.getPlayer(args[1]).getUniqueId().toString());
		    		}
		    	}
		    }
		    // /boss resetallcds
		    else if (args.length == 1 && args[0].equalsIgnoreCase("resetallcds") && !main.isInstance) {
		    	for (String boss : main.cooldowns.keySet()) {
		    		main.cooldowns.get(boss).clear();
		    	}
		    }
	    	// /boss resetinstances
		    else if (args.length == 1 && args[0].equalsIgnoreCase("resetinstances") && !main.isInstance) {
				try {
					Connection con = DriverManager.getConnection(Main.connection, Main.sqlUser, Main.sqlPass);
					Statement stmt = con.createStatement();
					
					// First clear all the cooldowns on the SQL currently
					stmt.executeUpdate("delete from neobossinstances_fights;");
					con.close();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
		    }
	    }
	    
	    
	    if (args.length == 0) {
			sender.sendMessage("�7=== �4[�c�lBosses�4] �7===");
			sender.sendMessage("�c/boss cd [name] �7- Shows cooldown of a specific boss");
			sender.sendMessage("�c/boss cd all �7- Shows cooldown of all bosses");
			sender.sendMessage("�c/boss instances [name] �7- Shows instances for boss");
			if (sender.hasPermission("bossinstances.admin")) {
				sender.sendMessage("�4/boss tp [name] [boss]�7- Teleports player to open boss instance");
				sender.sendMessage("�4/boss tp resetcd [player] [boss]�7- Resets a player cooldown for a boss");
				sender.sendMessage("�4/boss tp resetcds [player] �7- Resets a player cooldown for all bosses");
				sender.sendMessage("�4/boss tp resetallcds �7- Resets all player cooldowns");
				sender.sendMessage("�4/boss tp resetinstances �7- Resets all instances");
			}
			return true;
	    }
	    else if (args.length == 2 && args[0].equalsIgnoreCase("cd") && sender instanceof Player) {
	    	if (!main.isInstance) {
		    	Player p = (Player) sender;
		    	String name = WordUtils.capitalize(args[1]);
		    	if (name.equalsIgnoreCase("all")) {
		    		for (String boss : main.cooldowns.keySet()) {
		    			return main.getCooldown(boss, p);
		    		}
		    	}
		    	else {
		    		return main.getCooldown(name, p);
		    	}
	    	}
	    	else {
				sender.sendMessage("�4[�c�lBosses�4] �7You can only check cooldowns on the main server!");
	    	}
	    }
	    else if (args.length == 2 && args[0].equalsIgnoreCase("instances") && sender instanceof Player) {
	    	if (!main.isInstance) {
		    	Player p = (Player) sender;
		    	String name = WordUtils.capitalize(args[1]);
	    		try {
					Connection con = DriverManager.getConnection(Main.connection, Main.sqlUser, Main.sqlPass);
					Statement stmt = con.createStatement();
					ResultSet rs;

		    		// Find available instance
					for (String instance : main.instanceNames) {
						rs = stmt.executeQuery("SELECT * FROM neobossinstances_fights WHERE boss = '" + name + "' AND instance = '" + instance + "';");
						
						// Empty instance
						if (!rs.next()) {
							p.sendMessage("�e" + instance + "�7: Empty");
						}
						else {
							String temp = "�e" + instance + "�7: �e" + Bukkit.getPlayer(UUID.fromString(rs.getString(1))).getName();
							while (rs.next()) {
								temp += "�7, �e" + Bukkit.getPlayer(UUID.fromString(rs.getString(1))).getName();
							}
							p.sendMessage(temp);
						}
					}
	    		}
	    		catch (Exception e) {
	    			e.printStackTrace();
	    		}
	    	}
	    	else {
				sender.sendMessage("�4[�c�lBosses�4] �7You can only check instances on the main server!");
	    	}
	    }
		return false;
	}
}

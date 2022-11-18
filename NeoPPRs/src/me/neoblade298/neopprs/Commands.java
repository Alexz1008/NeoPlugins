package me.neoblade298.neopprs;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.util.Util;

public class Commands implements CommandExecutor {
	Main main;
	private static DateFormat dateformat = new SimpleDateFormat("MM-dd-yy");

	public Commands(Main main) {
		this.main = main;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender s, Command cmd, String lbl, String[] args) {
		if (!(s instanceof Player)) {
			if (args.length > 1 && args[0].equalsIgnoreCase("auto")) {
				PPR ppr = new PPR(Main.nextPPR, "Console");
				Main.nextPPR++;
				ppr.setUser(args[1]);
				ppr.setOffense("Banned word");
				ppr.setAction("Automuted by bot");
				ppr.setDescription("Said: " + Util.connectArgs(args, 2));
				ppr.postConsole(s);
			}
		}
		if (s.hasPermission("neopprs.admin")) {
			String author = s.getName();
			if (args.length == 0) {
				s.sendMessage("§7--- §cNeoPPRs §7(1/2) ---");
				s.sendMessage(
						"§c/ppr create [name] {xray/racism} §7- Creates PPR, optionally use the xray/racism shortcut");
				s.sendMessage("§c/ppr auto [name] banned-words §7- Automatically creates and posts banned msg PPR");
				s.sendMessage("§c/ppr offense §7- Sets offense for PPR");
				s.sendMessage("§c/ppr action §7- Sets action for PPR");
				s.sendMessage("§c/ppr desc §7- Sets description for PPR");
				s.sendMessage("§c/ppr view §7- Shows a preview of the PPR");
				s.sendMessage("§c/ppr post §7- Saves and posts PPR");
				s.sendMessage("§c/ppr cancel §7- Exits PPR creation mode");
			}
			else {
				if (args.length == 1 && args[0].equals("2")) {
					s.sendMessage("§7--- §cNeoPPRs §7(2/2) ---");
					s.sendMessage("§c/ppr view [player] §7- View all PPRs of player (and alts)");
					s.sendMessage(
							"§c/ppr modify [PPR ID] §7- Puts the PPR into creation mode, allowing you to modify it");
					s.sendMessage("§c/ppr remove [PPR ID] §7- Deletes the specified PPR");
					s.sendMessage("§c/ppr rename [oldname] [newname] §7- Used for name changes out of convenience");
					s.sendMessage("§c/ppr alts add [main account] [alt] §7- Declares an alt for a player");
					s.sendMessage("§c/ppr alts remove [main account] [alt] §7- Removes an alt from a player");
					s.sendMessage("§c/ppr alts list [main account]§7- Lists all known alts of a player");
				}
				if (args.length > 1 && args[0].equalsIgnoreCase("create")) {
					if (args.length == 2) {
						if (Main.pprs.containsKey(author)) {
							s.sendMessage("§4[§c§lMLMC§4] §7You are already creating a PPR! §c/ppr view");
						}
						else {
							main.viewPlayer(s, args[1], true);
							s.sendMessage("§4[§c§lMLMC§4] §7You entered PPR creation mode!");
							PPR ppr = new PPR(Main.nextPPR, author);
							Main.nextPPR++;
							Main.pprs.put(author, ppr);
							Main.isModifying.put(author, false);
							ppr.setUser(args[1]);
							ppr.preview(s);
						}
					}
					else if (args.length == 3 && args[2].equalsIgnoreCase("xray")) {
						if (Main.pprs.containsKey(author)) {
							s.sendMessage("§4[§c§lMLMC§4] §7You are already creating a PPR! §c/ppr view");
						}
						else {
							main.viewPlayer(s, args[1], true);
							s.sendMessage("§4[§c§lMLMC§4] §7You entered PPR creation mode!");
							PPR ppr = new PPR(Main.nextPPR, author);
							Main.nextPPR++;
							Main.pprs.put(author, ppr);
							Main.isModifying.put(author, false);
							ppr.setUser(args[1]);
							ppr.setOffense("Xray");
							ppr.setAction("Banned");
							ppr.preview(s);
						}
					}
					else if (args.length == 3 && args[2].equalsIgnoreCase("racism")) {
						if (Main.pprs.containsKey(author)) {
							s.sendMessage("§4[§c§lMLMC§4] §7You are already creating a PPR! §c/ppr view");
						}
						else {
							main.viewPlayer(s, args[1], true);
							s.sendMessage("§4[§c§lMLMC§4] §7You entered PPR creation mode!");
							PPR ppr = new PPR(Main.nextPPR, author);
							Main.nextPPR++;
							Main.pprs.put(author, ppr);
							Main.isModifying.put(author, false);
							ppr.setUser(args[1]);
							ppr.setOffense("Racism");
							ppr.setAction("Banned");
							ppr.setDescription("Said the n-word");
							ppr.preview(s);
						}
					}
				}
				else if (args.length > 1 && args[0].equalsIgnoreCase("offense")) {
					if (Main.pprs.containsKey(author)) {
						PPR ppr = Main.pprs.get(author);
						String offense = args[1];
						for (int i = 2; i < args.length; i++) {
							offense += " " + args[i];
						}
						ppr.setOffense(offense);
						ppr.preview(s);
					}
					else {
						s.sendMessage("§4[§c§lMLMC§4] §7You are not in PPR creation mode!");
					}
				}
				else if (args.length > 1 && args[0].equalsIgnoreCase("action")) {
					if (Main.pprs.containsKey(author)) {
						PPR ppr = Main.pprs.get(author);
						String action = args[1];
						for (int i = 2; i < args.length; i++) {
							action += " " + args[i];
						}
						ppr.setAction(action);
						ppr.preview(s);
					}
					else {
						s.sendMessage("§4[§c§lMLMC§4] §7You are not in PPR creation mode!");
					}
				}
				else if (args.length > 1
						&& (args[0].equalsIgnoreCase("description") || args[0].equalsIgnoreCase("desc"))) {
					if (Main.pprs.containsKey(author)) {
						PPR ppr = Main.pprs.get(author);
						String desc = args[1];
						for (int i = 2; i < args.length; i++) {
							desc += " " + args[i];
						}
						ppr.setDescription(desc);
						ppr.preview(s);
					}
					else {
						s.sendMessage("§4[§c§lMLMC§4] §7You are not in PPR creation mode!");
					}
				}
				else if (args.length == 1 && args[0].equalsIgnoreCase("view")) {
					if (Main.pprs.containsKey(author)) {
						PPR ppr = Main.pprs.get(author);
						ppr.preview(s);
					}
					else {
						s.sendMessage("§4[§c§lMLMC§4] §7You are not in PPR creation mode!");
					}
				}
				else if (args.length == 1 && args[0].equalsIgnoreCase("cancel")) {
					if (Main.pprs.containsKey(author)) {
						s.sendMessage("§4[§c§lMLMC§4] §7You exited PPR creation mode!");
						Main.pprs.remove(author);
						Main.isModifying.remove(author);
					}
					else {
						s.sendMessage("§4[§c§lMLMC§4] §7You are not in PPR creation mode!");
					}
				}
				else if (args.length == 1 && args[0].equalsIgnoreCase("post")) {
					if (Main.pprs.containsKey(author)) {
						PPR ppr = Main.pprs.get(author);
						if (ppr.isFilled()) {
							if (Main.isModifying.get(author)) {
								ppr.modify(s);
							}
							else {
								ppr.post(s);
							}
							Main.pprs.remove(author);
							Main.isModifying.remove(author);
						}
						else {
							s.sendMessage("§4[§c§lMLMC§4] §7You must fill every part of the PPR to post!");
						}
					}
					else {
						s.sendMessage("§4[§c§lMLMC§4] §7You are not in PPR creation mode!");
					}
				}
				else if (args.length == 2 && args[0].equalsIgnoreCase("view")) {
					main.viewPlayer(s, args[1], false);
				}
				else if (args.length == 2 && args[0].equalsIgnoreCase("modify") && StringUtils.isNumeric(args[1])) {
					if (Main.pprs.containsKey(author)) {
						s.sendMessage("§4[§c§lMLMC§4] §7You are already creating a PPR! §c/ppr view");
					}
					else {
						int id = Integer.parseInt(args[1]);
						PPR ppr = null;
						try {
							Connection con = DriverManager.getConnection(Main.connection, Main.sqlUser, Main.sqlPass);
							Statement stmt = con.createStatement();
							ResultSet rs = stmt.executeQuery("SELECT * FROM neopprs_pprs WHERE id = " + id + ";");
							while (rs.next()) {
								ppr = new PPR(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4),
										rs.getString(5), rs.getString(6), rs.getString(7), rs.getString(8));
							}
							con.close();
						} catch (Exception e) {
							e.printStackTrace();
							s.sendMessage(
									"§4[§c§lMLMC§4] §cSomething went wrong! Report to neo and don't use the plugin anymore!");
						}
						if (ppr != null) {
							s.sendMessage("§4[§c§lMLMC§4] §7You entered PPR creation mode!");
							Main.pprs.put(author, ppr);
							Main.isModifying.put(author, true);
							ppr.preview(s);
						}
						else {
							s.sendMessage("§4[§c§lMLMC§4] §7Could not find that PPR id!");
						}
					}
				}
				else if (args.length == 2 && args[0].equalsIgnoreCase("remove") && StringUtils.isNumeric(args[1])) {
					int id = Integer.parseInt(args[1]);
					try {
						Connection con = DriverManager.getConnection(Main.connection, Main.sqlUser, Main.sqlPass);
						Statement stmt = con.createStatement();
						int deleted = stmt.executeUpdate("delete from neopprs_pprs WHERE id = " + id + ";");
						if (deleted > 0) {
							s.sendMessage("§4[§c§lMLMC§4] §7Successfully removed PPR!");
						}
						else {
							s.sendMessage("§4[§c§lMLMC§4] §7No PPRs matching this id were found.");
						}
						con.close();
					} catch (Exception e) {
						e.printStackTrace();
						s.sendMessage(
								"§4[§c§lMLMC§4] §cSomething went wrong! Report to neo and don't use the plugin anymore!");
					}
				}
				else if (args.length == 3 && args[0].equalsIgnoreCase("rename")) {
					try {
						Connection con = DriverManager.getConnection(Main.connection, Main.sqlUser, Main.sqlPass);
						Statement stmt = con.createStatement();
						int renamed = stmt.executeUpdate("update neopprs_pprs set username = '" + args[2]
								+ "' WHERE upper(username) = '" + args[1].toUpperCase() + "';");
						if (renamed > 0) {
							s.sendMessage("§4[§c§lMLMC§4] §7Successful renaming! " + renamed + " PPRs renamed.");
						}
						else {
							s.sendMessage("§4[§c§lMLMC§4] §7No users matching this name found.");
						}
						con.close();
					} catch (Exception e) {
						e.printStackTrace();
						s.sendMessage(
								"§4[§c§lMLMC§4] §cSomething went wrong! Report to neo and don't use the plugin anymore!");
					}
				}
				else if (args[0].equalsIgnoreCase("alts")) {
					if (args[1].equalsIgnoreCase("add") && args.length == 4) {
						String mainAcc = args[2];
						String altAcc = args[3];
						try {
							Connection con = DriverManager.getConnection(Main.connection, Main.sqlUser, Main.sqlPass);
							Statement stmt = con.createStatement();
							ResultSet rs;

							// Get the UUID of the main account
							String mainuuid = null;
							if (Main.uuids.containsKey(mainAcc.toUpperCase())) {
								Main.uuids.get(mainAcc.toUpperCase());
							}
							else {
								rs = stmt.executeQuery("SELECT * FROM neopprs_pprs WHERE upper(username) = '"
										+ mainAcc.toUpperCase() + "';");
								if (rs.next()) {
									mainuuid = rs.getString(4);
									Main.uuids.put(mainAcc.toUpperCase(), mainuuid);
								}
							}

							// Get the UUID of the alt account
							String altuuid = null;
							if (Main.uuids.containsKey(altAcc.toUpperCase())) {
								Main.uuids.get(altAcc.toUpperCase());
							}
							else {
								rs = stmt.executeQuery("SELECT * FROM neopprs_pprs WHERE upper(username) = '"
										+ altAcc.toUpperCase() + "';");
								if (rs.next()) {
									altuuid = rs.getString(4);
									Main.uuids.put(altAcc.toUpperCase(), altuuid);
								}
							}

							// If either UUID isn't found, just look it up manually
							if (mainuuid == null) {
								mainuuid = Bukkit.getServer().getOfflinePlayer(mainAcc).getUniqueId().toString();
								Main.uuids.put(mainAcc.toUpperCase(), mainuuid);
							}
							if (altuuid == null) {
								altuuid = Bukkit.getServer().getOfflinePlayer(altAcc).getUniqueId().toString();
								Main.uuids.put(altAcc.toUpperCase(), altuuid);
							}

							// Check for duplicate
							rs = stmt.executeQuery("SELECT * FROM neopprs_alts WHERE uuid = '" + mainuuid
									+ "' AND altuuid = '" + altuuid + "';");
							if (rs.next()) {
								s.sendMessage("§4[§c§lMLMC§4] §cThis alt account was already added!");
							}
							else {
								stmt.executeUpdate("INSERT INTO neopprs_alts VALUES (" + Main.nextAlt + ",'"
										+ s.getName() + "','" + mainAcc + "','" + mainuuid + "','" + altAcc + "','"
										+ altuuid + "','" + dateformat.format(new Date()) + "')");
								Main.nextAlt++;
								s.sendMessage("§4[§c§lMLMC§4] §7Successfully added alt account!");
							}
							con.close();
						} catch (Exception e) {
							e.printStackTrace();
							s.sendMessage(
									"§4[§c§lMLMC§4] §cSomething went wrong! Report to neo and don't use the plugin anymore!");
						}
					}
					else if (args.length == 4 && args[1].equalsIgnoreCase("remove")) {
						String mainAcc = args[2];
						String altAcc = args[3];

						try {
							Connection con = DriverManager.getConnection(Main.connection, Main.sqlUser, Main.sqlPass);
							Statement stmt = con.createStatement();
							int deleted = stmt.executeUpdate("delete from neopprs_alts WHERE upper(username) = '"
									+ mainAcc.toUpperCase() + "' AND upper(altname) = '" + altAcc.toUpperCase() + "';");
							if (deleted > 0) {
								s.sendMessage("§4[§c§lMLMC§4] §7Successfully removed alt account!");
							}
							else {
								s.sendMessage("§4[§c§lMLMC§4] §7No alt accounts matching this were found.");
							}
							con.close();
						} catch (Exception e) {
							e.printStackTrace();
							s.sendMessage(
									"§4[§c§lMLMC§4] §cSomething went wrong! Report to neo and don't use the plugin anymore!");
						}
					}
					else if (args.length == 3 && args[1].equalsIgnoreCase("list")) {
						String user = args[2];
						try {
							Connection con = DriverManager.getConnection(Main.connection, Main.sqlUser, Main.sqlPass);
							Statement stmt = con.createStatement();
							ResultSet rs;

							// Get the UUID of the main account
							String uuid = null;
							if (Main.uuids.containsKey(user.toUpperCase())) {
								Main.uuids.get(user.toUpperCase());
							}
							else {
								rs = stmt.executeQuery("SELECT * FROM neopprs_alts WHERE upper(username) = '"
										+ user.toUpperCase() + "';");
								if (rs.next()) {
									uuid = rs.getString(4);
									Main.uuids.put(user.toUpperCase(), uuid);
								}
							}

							// Else just look it up manually
							if (uuid == null) {
								uuid = Bukkit.getServer().getOfflinePlayer(user).getUniqueId().toString();
								Main.uuids.put(user.toUpperCase(), uuid);
							}

							ArrayList<String> alts = new ArrayList<String>();
							rs = stmt.executeQuery("SELECT * FROM neopprs_alts WHERE uuid = '" + uuid + "';");
							while (rs.next()) {
								alts.add(rs.getString(5));
							}

							String message = new String("§4[§c§lMLMC§4] §7Known alts: ");
							if (alts.isEmpty()) {
								message += " None";
							}
							else {
								for (String alt : alts) {
									message += alt + ", ";
								}
							}
							s.sendMessage(message.substring(0, message.length() - 2));
							con.close();
						} catch (Exception e) {
							e.printStackTrace();
							s.sendMessage(
									"§4[§c§lMLMC§4] §cSomething went wrong! Report to neo and don't use the plugin anymore!");
						}
					}
					else {
						s.sendMessage("§7--- §cNeoPPRs §7---");
						s.sendMessage("§c/ppr alts add [main account] [alt] §7- Declares an alt for a player");
						s.sendMessage("§c/ppr alts remove [main account] [alt] §7- Removes an alt from a player");
						s.sendMessage("§c/ppr alts list [main account]§7- Lists all known alts of a player");
					}
				}
				else {
					s.sendMessage("§4[§c§lMLMC§4] §7Invalid commands!");
					return true;
				}
			}
			return true;
		}
		return false;
	}
}

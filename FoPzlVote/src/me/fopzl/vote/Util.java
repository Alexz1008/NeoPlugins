package me.fopzl.vote;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import me.neoblade298.neocore.bungee.BungeeAPI;

public class Util {
	public static void broadcastFormatted(String msg) {
		// temp removed for live testing
		//BungeeAPI.broadcast(msg.replace('&', '§'));
		//Bukkit.broadcastMessage(msg.replace('&', '§'));
	}
	
	public static void sendMessageFormatted(CommandSender sender, String msg) {
		sender.sendMessage(msg.replace('&', '§'));
	}
}

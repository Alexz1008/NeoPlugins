package me.neoblade298.neocore.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.command.CommandSender;

import net.md_5.bungee.api.ChatColor;

public class Util {
	public static final Pattern HEX_PATTERN = Pattern.compile("&(#[A-Fa-f0-9]{6})");
	
	public static void msg(CommandSender s, String msg) {
		s.sendMessage(translateColors(msg));
	}

	public static String translateColors(String textToTranslate) {

		Matcher matcher = HEX_PATTERN.matcher(textToTranslate);
		StringBuffer buffer = new StringBuffer();

		while (matcher.find()) {
			matcher.appendReplacement(buffer, ChatColor.of(matcher.group(1)).toString());
		}

		return ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());

	}
}

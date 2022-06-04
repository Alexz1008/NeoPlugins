package me.neoblade298.neoquests.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import me.neoblade298.neocore.commands.Subcommand;
import me.neoblade298.neocore.commands.SubcommandRunner;
import me.neoblade298.neoquests.NeoQuests;

public class CmdQuestsReload implements Subcommand {

	@Override
	public String getDescription() {
		return "Reloads the plugin";
	}

	@Override
	public String getKey() {
		return "reload";
	}

	@Override
	public String getPermission() {
		return "neoquests.admin";
	}

	@Override
	public SubcommandRunner getRunner() {
		return SubcommandRunner.BOTH;
	}
	
	@Override
	public ChatColor getColor() {
		return ChatColor.RED;
	}

	@Override
	public void run(CommandSender s, String[] args) {
		if (NeoQuests.reloadAll()) {
			s.sendMessage("�4[�c�lMLMC�4] �7Successful reload.");
		}
		else {
			s.sendMessage("�4[�c�lMLMC�4] �cReload failed! Check the error messages!");
		}
	}

}

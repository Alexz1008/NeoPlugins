package me.neoblade298.neoquests.commands;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.neoblade298.neocore.commands.CommandArgument;
import me.neoblade298.neocore.commands.CommandArguments;
import me.neoblade298.neocore.commands.Subcommand;
import me.neoblade298.neocore.commands.SubcommandRunner;
import me.neoblade298.neocore.util.Util;
import me.neoblade298.neoquests.quests.CompletedQuest;
import me.neoblade298.neoquests.quests.Quester;
import me.neoblade298.neoquests.quests.QuestsManager;

public class CmdQuestAdminIsComplete implements Subcommand {
	private static final CommandArguments args = new CommandArguments(Arrays.asList(new CommandArgument("key"), new CommandArgument("player", false)));

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public String getKey() {
		return "iscomplete";
	}

	@Override
	public String getPermission() {
		return null;
	}

	@Override
	public SubcommandRunner getRunner() {
		return SubcommandRunner.BOTH;
	}

	@Override
	public void run(CommandSender s, String[] args) {
		Player p;
		if (args.length == 1) {
			p = (Player) s;
		}
		else {
			p = Bukkit.getPlayer(args[1]);
			if (p == null) {
				Util.msg(s, "&cThis player is not online!");
				return;
			}
		}
		
		Quester q = QuestsManager.getQuester(p);
		if (q == null) {
			Util.msg(s, "&cThis account hasn't loaded in yet! Try again in a few seconds.");
			return;
		}
		CompletedQuest cq = q.getCompletedQuest(args[0]);
		if (cq != null) {
			Util.msg(s, q.getPlayer().getName() + " &7has completed the quest &e" + cq.getQuest().getDisplay());
		}
		else {
			Util.msg(s, q.getPlayer().getName() + " &7has not completed this quest");
		}
	}

	@Override
	public CommandArguments getArgs() {
		return args;
	}

}

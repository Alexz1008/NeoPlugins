package me.neoblade298.neocore.commands.builtin;

import java.util.Arrays;

import org.bukkit.command.CommandSender;

import me.neoblade298.neocore.commands.CommandArgument;
import me.neoblade298.neocore.commands.CommandArguments;
import me.neoblade298.neocore.commands.Subcommand;
import me.neoblade298.neocore.commands.SubcommandRunner;
import me.neoblade298.neocore.io.IOType;
import me.neoblade298.neocore.listeners.IOListener;
import me.neoblade298.neocore.util.Util;

public class CmdCoreDisable implements Subcommand {
	private static final CommandArguments args = new CommandArguments(Arrays.asList(new CommandArgument("save/preload/load/cleanup", false)));

	@Override
	public String getPermission() {
		return "neocore.admin";
	}

	@Override
	public SubcommandRunner getRunner() {
		return SubcommandRunner.BOTH;
	}

	@Override
	public String getKey() {
		return "disable";
	}

	@Override
	public String getDescription() {
		return "Disables an IO action: save, preload, load, cleanup";
	}

	@Override
	public void run(CommandSender s, String[] args) {
		if (args.length == 0) {
			Util.msg(s, "&7Valid IO actions: save, preload, load, cleanup");
		}
		else {
			IOType type = IOType.valueOf(args[0].toUpperCase());
			IOListener.disableIO(type);
			Util.msg(s, "Successfully set " + type + " to disabled.");
		}
	}

	@Override
	public CommandArguments getArgs() {
		return args;
	}
}

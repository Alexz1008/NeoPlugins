package me.neoblade298.neoleaderboard.commands;

import java.util.Arrays;
import java.util.Iterator;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Nation;

import me.neoblade298.neocore.commands.CommandArgument;
import me.neoblade298.neocore.commands.CommandArguments;
import me.neoblade298.neocore.commands.Subcommand;
import me.neoblade298.neocore.commands.SubcommandRunner;
import me.neoblade298.neocore.util.Util;
import me.neoblade298.neoleaderboard.NeoLeaderboard;
import me.neoblade298.neoleaderboard.points.NationEntry;
import me.neoblade298.neoleaderboard.points.PlayerPointType;
import me.neoblade298.neoleaderboard.points.PointsManager;
import me.neoblade298.neoleaderboard.points.TownEntry;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;
import net.md_5.bungee.api.chat.hover.content.Text;

public class CmdNLCNation implements Subcommand {
	private static final CommandArguments args = new CommandArguments(Arrays.asList(
			new CommandArgument("nation"), new CommandArgument("category", false)));

	@Override
	public String getDescription() {
		return "Displays clickable list of categories";
	}

	@Override
	public String getKey() {
		return "nation";
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
	public CommandArguments getArgs() {
		return args;
	}

	@Override
	public void run(CommandSender s, String[] args) {
		Nation n = TownyUniverse.getInstance().getNation(args[0]);
		if (n == null) {
			Util.msg(s, "&cThis nation doesn't exist!");
			return;
		}
		PlayerPointType type = null;
		if (args.length > 1) {
			try {
				type = PlayerPointType.valueOf(args[1].toUpperCase());
			}
			catch (IllegalArgumentException ex) {
				Util.msg(s, "&cThis category doesn't exist!");
				return;
			}
		}
		final PlayerPointType ftype = type;

		new BukkitRunnable() {
			public void run() {
				// No category chosen
				if (args.length == 1) {
					ComponentBuilder builder = new ComponentBuilder("\n§6§l>§8§m--------§c§l» Player Categories «§8§m--------§6§l<");
					for (PlayerPointType type : PlayerPointType.values()) {
						builder.append("\n §6§l» §e" + type.getDisplay(), FormatRetention.NONE)
						.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("/nlc nation " + args[0] + " " + type)))
						.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/nlc nation " + args[0] + " " + type));
					}
					s.spigot().sendMessage(builder.create());
				}
				
				// Chose a category
				else {
					NationEntry ne = PointsManager.getNationEntry(n.getUUID());
					Iterator<TownEntry> iter = ne.getTopTowns(ftype).descendingIterator();

					ComponentBuilder builder = new ComponentBuilder("§c§lTop Towns in " + n.getName() + ": §e" + ftype.getDisplay());
					int i = 1;
					while (iter.hasNext() && i++ < 10) {
						TownEntry e = iter.next();
						String name = e.getTown().getName();
						// double effective = PointsManager.calculateEffectivePoints(ne, e.getPlayerPoints(ftype));
						builder.append("\n§6§l" + i + ". §e" + name + " §7- §f" + PointsManager.formatPoints(e.getPlayerPoints(ftype)), FormatRetention.NONE)
						.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("/nlc town " + name + " " + ftype)))
						.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/nlc town " + name + " " + ftype));
					}
					s.spigot().sendMessage(builder.create());
				}
			}
		}.runTaskAsynchronously(NeoLeaderboard.inst());
	}
}

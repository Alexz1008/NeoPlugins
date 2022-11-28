package me.neoblade298.neoleaderboard.commands;

import java.util.Arrays;
import java.util.Iterator;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;

import me.neoblade298.neocore.commands.CommandArgument;
import me.neoblade298.neocore.commands.CommandArguments;
import me.neoblade298.neocore.commands.Subcommand;
import me.neoblade298.neocore.commands.SubcommandRunner;
import me.neoblade298.neocore.util.Util;
import me.neoblade298.neoleaderboard.NeoLeaderboard;
import me.neoblade298.neoleaderboard.points.NationEntry;
import me.neoblade298.neoleaderboard.points.PlayerEntry;
import me.neoblade298.neoleaderboard.points.PlayerPointType;
import me.neoblade298.neoleaderboard.points.PointsManager;
import me.neoblade298.neoleaderboard.points.TownEntry;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;
import net.md_5.bungee.api.chat.hover.content.Text;

public class CmdNLCTown implements Subcommand {
	private static final CommandArguments args = new CommandArguments(Arrays.asList(
			new CommandArgument("town"), new CommandArgument("category", false)));

	@Override
	public String getDescription() {
		return "Displays clickable list of categories";
	}

	@Override
	public String getKey() {
		return "town";
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
		Town t = TownyUniverse.getInstance().getTown(args[0]);
		if (t == null) {
			Util.msg(s, "&cThis town doesn't exist!");
			return;
		}
		Nation n = t.getNationOrNull();
		if (n == null) {
			Util.msg(s, "&cThis town doesn't have a nation!");
			return;
		}
		NationEntry ne = PointsManager.getNationEntry(n.getUUID());
		TownEntry te = ne.getTownEntry(t.getUUID());
		if (te == null) {
			Util.msg(s, "&cThis town hasn't contributed anything yet!");
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
						builder.append("\n §6§l» §e" + type.getExtendedDisplay(), FormatRetention.NONE)
						.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("/nlc town " + args[0] + " " + type)))
						.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/nlc town " + args[0] + " " + type));
					}
					s.spigot().sendMessage(builder.create());
				}
				
				// Chose a category
				else {
					Iterator<PlayerEntry> iter = te.getTopPlayers(ftype).descendingIterator();

					ComponentBuilder builder = new ComponentBuilder("§c§lTop Players in " + t.getName() + ": §e" + ftype.getDisplay());
					int i = 1;
					while (iter.hasNext() && i++ < 10) {
						PlayerEntry e = iter.next();
						String name = e.getDisplay();
						// double effective = PointsManager.calculateEffectivePoints(ne, e.getContributedPoints(ftype));
						builder.append("\n§6§l" + i + ". §e" + name + " §7- §f" + PointsManager.formatPoints(e.getContributedPoints(ftype)), FormatRetention.NONE)
						.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("/nl " + name)))
						.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/nl " + name));
					}
					s.spigot().sendMessage(builder.create());
				}
			}
		}.runTaskAsynchronously(NeoLeaderboard.inst());
	}
}

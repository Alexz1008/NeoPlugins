package me.Neoblade298.NeoProfessions.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.tr7zw.nbtapi.NBTItem;
import me.Neoblade298.NeoProfessions.Professions;


public class ValueCommand implements CommandExecutor {
	Professions main;
	
	public ValueCommand(Professions main) {
		this.main = main;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
		if (args.length == 0 && sender instanceof Player) {
			Player p = (Player) sender;
			ItemStack item = p.getInventory().getItemInMainHand();
			NBTItem nbti = new NBTItem(item);
			double value = 0;
			if (!nbti.getString("value").isBlank()) {
				value = Double.parseDouble(nbti.getString("value"));
			}
			else {
				value = nbti.getDouble("value");
			}
			String name = item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : item.getType().name();
			p.sendMessage("�7Value of " + name + "�7: �e" + value + "g");
			return true;
		}
		return false;
	}
}
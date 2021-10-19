package me.Neoblade298.NeoConsumables.runnables;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.Neoblade298.NeoConsumables.NeoConsumables;
import me.Neoblade298.NeoConsumables.objects.Attributes;
import me.Neoblade298.NeoConsumables.objects.Consumable;

public class AttributeRunnable extends BukkitRunnable {
	Attributes attr;
	Player p;
	NeoConsumables main;
	Consumable c;
	
	public AttributeRunnable(NeoConsumables main, Player p, Attributes attr, Consumable c) {
		this.main = main;
		this.p = p;
		this.attr = attr;
		this.c = c;
	}

	@Override
	public void run() {
		if (p != null) {
			attr.removeAttributes(p);
			main.attributes.get(p.getUniqueId()).remove(c);
		}
	}
}
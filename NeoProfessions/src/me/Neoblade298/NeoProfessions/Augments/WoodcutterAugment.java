package me.Neoblade298.NeoProfessions.Augments;

import java.util.Arrays;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.Neoblade298.NeoProfessions.PlayerProfessions.ProfessionType;

public class WoodcutterAugment extends Augment implements ModProfessionHarvestAugment {
	
	public WoodcutterAugment() {
		super();
		this.name = "Woodcutter";
		this.etypes = Arrays.asList(new EventType[] {EventType.PROFESSION_HARVEST});
	}

	public WoodcutterAugment(int level) {
		super(level);
		this.name = "Woodcutter";
		this.etypes = Arrays.asList(new EventType[] {EventType.PROFESSION_HARVEST});
	}
	
	@Override
	public double getChance() {
		return (this.level / 5) * 0.01;
	}

	@Override
	public double getAmountMult(Player user) {
		return 1;
	}

	@Override
	public Augment createNew(int level) {
		return new WoodcutterAugment(level);
	}

	@Override
	public boolean canUse(Player user, ProfessionType type) {
		return type.equals(ProfessionType.LOGGER);
	}

	public ItemStack getItem(Player user) {
		ItemStack item = super.getItem(user);
		ItemMeta meta = item.getItemMeta();
		List<String> lore = meta.getLore();
		lore.add("�7" + formatPercentage(getChance()) + "% chance to increase");
		lore.add("�7logger base yield by " + formatPercentage(getAmountMult(user)) + "%.");
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}

}

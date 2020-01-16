package me.Neoblade298.NeoProfessions.Legacy;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.Neoblade298.NeoProfessions.Main;
import me.Neoblade298.NeoProfessions.Items.BlacksmithItems;
import me.Neoblade298.NeoProfessions.Items.CommonItems;
import me.Neoblade298.NeoProfessions.Items.MasonItems;
import me.Neoblade298.NeoProfessions.Items.StonecutterItems;
import me.Neoblade298.NeoProfessions.Utilities.Util;

public class Converter {
	Main main;
	BlacksmithItems bItems;
	StonecutterItems sItems;
	MasonItems mItems;
	MasonItemsLegacy oldMItems;
	CommonItems cItems;
	Util util;
	
	
	public Converter(Main main) {
		this.main = main;
		bItems = new BlacksmithItems();
		sItems = new StonecutterItems();
		mItems = new MasonItems();
		oldMItems = new MasonItemsLegacy();
		cItems = new CommonItems();
		util = new Util();
	}

	public ItemStack convertItem(ItemStack item) {
		if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore() && item.getEnchantmentLevel(Enchantment.DURABILITY) < 10) {
			ItemMeta meta = item.getItemMeta();
			ArrayList<String> lore = (ArrayList<String>) meta.getLore();
			String idLine = meta.getLore().get(0);
			
			if (idLine.contains("Right click")) {	// Repair kit
				System.out.println("Converted repair");
				return convertRepairKit(lore);
			}
			else if (idLine.contains("Durability")) {
				return convertDurabilityItem(lore);
			}
			else if (idLine.contains("Essence")) {
				return convertEssence(lore);
			}
			else if (idLine.contains("Ore")) {
				return convertOre(lore);
			}
			else if (idLine.contains("Gem")) {
				return convertGem(lore);
			}
			else if (idLine.contains("Charm")) {
				return convertCharm(item, lore);
			}
		}
		return item;
	}
	
	private ItemStack convertRepairKit(ArrayList<String> lore) {
		int potency = Integer.parseInt(lore.get(4).split(" ")[1].substring(2,4));
		int oldLevel = ((potency % 25) / 5) + 1;
		int newLevel = (oldLevel + 1) * 10;
		return bItems.getRepairItem(newLevel);
	}
	
	private ItemStack convertDurabilityItem(ArrayList<String> lore) {
		String type = lore.get(1).split(" ")[2];
		int oldLevel = Integer.parseInt(lore.get(0).split(" ")[1].replaceAll("�e", ""));
		int newLevel = (oldLevel + 1) * 10;
		int potency = Integer.parseInt(lore.get(2).split(" ")[1].replaceAll("�e", ""));
		return bItems.getDurabilityItem(newLevel, type, potency);
	}
	
	private ItemStack convertEssence(ArrayList<String> lore) {
		int oldLevel = Integer.parseInt(lore.get(0).split(" ")[1]);
		int newLevel = (oldLevel + 1) * 10;
		return cItems.getEssence(newLevel, true);
	}
	
	private ItemStack convertOre(ArrayList<String> lore) {
		String oreName = lore.get(0).split(" ")[2].replaceAll("�e", "");
		String type = null;
		switch (oreName) {
		case "Ruby": 
			type = "strength";
			break;
		case "Amethyst":
			type = "dexterity";
			break;
		case "Sapphire":
			type = "intelligence";
			break;
		case "Emerald":
			type = "spirit";
			break;
		case "Topaz":
			type = "perception";
			break;
		case "Garnet":
			type = "vitality";
			break;
		case "Adamantium":
			type = "endurance";
			break;
		}
		int oldLevel = Integer.parseInt(lore.get(0).split(" ")[1].replaceAll("�e", ""));
		int newLevel = (oldLevel + 1) * 10;
		return sItems.getOre(type, newLevel);
	}
	
	private ItemStack convertGem(ArrayList<String> lore) {
		int oldLevel = Integer.parseInt(lore.get(0).split(" ")[1].replaceAll("�e", ""));
		int newLevel = (oldLevel + 1) * 10;
		String itemType = lore.get(1).split(" ")[2];
		String type = lore.get(1).split(" ")[3];
		int potency = Integer.parseInt(lore.get(2).split(" ")[1].replaceAll("�e", ""));
		boolean isOverloaded = false;
		int duraLoss = 0;
		if (lore.size() > 3) {
			isOverloaded = true;
			duraLoss = Integer.parseInt(lore.get(3).split(" ")[2].replaceAll("�e", ""));
			type = type.substring(0, type.length() - 1);
		}
		
		if (itemType.equalsIgnoreCase("weapon")) {
			return sItems.getWeaponGem(type, newLevel, isOverloaded, potency, duraLoss);
		}
		else {
			return sItems.getArmorGem(type, newLevel, isOverloaded, potency, duraLoss);
		}
	}
	
	private ItemStack convertCharm(ItemStack item, ArrayList<String> lore) {
		if (item.isSimilar(oldMItems.getDropCharm(false))) {
			return mItems.getDropCharm(false);
		}
		else if (item.isSimilar(oldMItems.getDropCharm(true))) {
			return mItems.getDropCharm(true);
		}
		else if (item.isSimilar(oldMItems.getExpCharm(false))) {
			return mItems.getExpCharm(false);
		}
		else if (item.isSimilar(oldMItems.getExpCharm(true))) {
			return mItems.getExpCharm(true);
		}
		else if (item.isSimilar(oldMItems.getHungerCharm())) {
			return mItems.getHungerCharm();
		}
		else if (item.isSimilar(oldMItems.getLootingCharm(false))) {
			return mItems.getLootingCharm(false);
		}
		else if (item.isSimilar(oldMItems.getLootingCharm(true))) {
			return mItems.getLootingCharm(true);
		}
		else if (item.isSimilar(oldMItems.getQuickEatCharm())) {
			return mItems.getQuickEatCharm();
		}
		else if (item.isSimilar(oldMItems.getRecoveryCharm())) {
			return mItems.getRecoveryCharm();
		}
		else if (item.isSimilar(oldMItems.getSecondChanceCharm())) {
			return mItems.getSecondChanceCharm();
		}
		else if (item.isSimilar(oldMItems.getTravelerCharm())) {
			return mItems.getTravelerCharm();
		}
		return null;
	}
	
	private ItemStack convertGear(ItemStack item, ItemMeta meta, ArrayList<String> lore) {
		// First change durability
		if (util.isWeapon(item)) {
			util.setMaxDurability(item, 1400);
		}
		else if (util.isArmor(item) && lore.get(0).contains("Reinforced")) { 
			util.setMaxDurability(item, 900);
		}
		else if (util.isArmor(item) && lore.get(0).contains("Infused")) { 
			util.setMaxDurability(item, 700);
		}
		
		String oldRarity = ChatColor.stripColor(lore.get(0).split(" ")[1]);
		int newLevel = 10;
		
		switch (oldRarity) {
		case "uncommon":
			newLevel = 15;
			break;
		case "rare":
			newLevel = 20;
			break;
		case "unique":
			newLevel = 25;
			break;
		case "epic":
			newLevel = 30;
			break;
		case "angelic":
			newLevel = 40;
			break;
		case "mythic":
			newLevel = 50;
			break;
		}
		lore.add(1, "�7Level Req: " + newLevel);
		return item;
	}
}
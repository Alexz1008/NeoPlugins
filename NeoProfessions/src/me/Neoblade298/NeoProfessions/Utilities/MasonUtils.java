package me.Neoblade298.NeoProfessions.Utilities;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import io.lumine.xikage.mythicmobs.MythicMobs;
import me.Neoblade298.NeoProfessions.Items.BlacksmithItems;
import me.Neoblade298.NeoProfessions.Items.MasonItems;
import me.Neoblade298.NeoProfessions.Items.StonecutterItems;

public class MasonUtils {

	final int LEVEL_INTERVAL = 5;
	Util util;
	BlacksmithItems bItems;
	StonecutterItems sItems;
	MasonItems mItems;

	public MasonUtils() {
		util = new Util();
		bItems = new BlacksmithItems();
		sItems = new StonecutterItems();
		mItems = new MasonItems();
	}

	public int getAugmentLevel(ItemStack item) {
		return item.getEnchantmentLevel(Enchantment.DURABILITY);
	}

	public void createSlot(ItemStack item, int level) {
		ItemMeta meta = item.getItemMeta();
		ArrayList<String> lore = (ArrayList<String>) meta.getLore();
		boolean hasBonus = false;
		int slotLine = -1;
		for (int i = 0; i < lore.size(); i++) {
			if (lore.get(i).contains("Bonus")) {
				hasBonus = true;
			}
			if (lore.get(i).contains("Durability")) {
				slotLine = i;
			}
		}

		lore.add(slotLine, "�8(Lv " + level + " Slot)");
		if (!hasBonus) {
			lore.add(slotLine, "�9[Bonus Attributes]");
		}
		meta.setLore(lore);
		item.setItemMeta(meta);
	}

	public ItemStack parseUnslot(Player p, int slot) {
		ItemStack item = p.getInventory().getItemInMainHand();
		String line = getSlotLine(item, slot);
		String[] words = line.split(" ");
		int level = util.getItemLevel(item);

		// Parse the line and revert the lore
		// chars 1 3 5 = slotLevel, chars 7 9 11 = slottedLevel, char 13 = slotType,
		// chars 15 17 19 = durability loss
		// slotType: 0 = Durability, 1 = attribute, 2 = overload, 3 = charm
		char color = line.charAt(1);
		int slottedLevel = level;
		int slotType = -1;
		String attr = getSlotLineAttribute(line);
		boolean isArmor = Util.isArmor(item);
		int potency = -1;
		int durabilityLoss = -1;

		if (line.contains("Durability")) {
			slotType = 0;
		}
		else if (StringUtils.isNumeric(words[1].substring(1))) {
			if (color == '9') {
				slotType = 1;
			}
			else {
				slotType = 2;
				if (isArmor) {
					durabilityLoss = 200;
				}
				else {
					durabilityLoss = 400;
				}
			}
		}
		else if (line.contains("Charm")) {
			slotType = 3;
		}
		else if (line.contains("Relic")) {
			slotType = 4;
		}

		switch (slotType) {
		// Max durability
		case 0:
			potency = Integer.parseInt(line.substring(line.indexOf("+") + 1, line.length()));
			if (util.getMaxDurability(item) > potency) {
				util.setMaxDurability(item, util.getMaxDurability(item) - potency);
				if (isArmor) {
					return bItems.getDurabilityItem(slottedLevel, "armor", potency);
				} else {
					return bItems.getDurabilityItem(slottedLevel, "weapon", potency);
				}
			} else {
				Util.sendMessage(p, "&cThis item will break if you unslot this!");
				return null;
			}
			// Normal gem
		case 1:
			potency = Integer.parseInt(line.substring(line.indexOf("+") + 1, line.length()));
			if (isArmor) {
				return sItems.getArmorGem(attr, slottedLevel, false, potency);
			} else {
				return sItems.getWeaponGem(attr, slottedLevel, false, potency);
			}

			// Overload gem
		case 2:
			potency = Integer.parseInt(line.substring(line.indexOf("+") + 1, line.length()));
			util.setMaxDurability(item, util.getMaxDurability(item) + durabilityLoss);
			if (isArmor) {
				return sItems.getArmorGem(attr, slottedLevel, true, potency);
			} else {
				return sItems.getWeaponGem(attr, slottedLevel, true, potency);
			}

			// Charm
		case 3:
			if (line.contains("Advanced")) {
				if (line.contains("Exp")) {
					return mItems.getExpCharm(true);
				} else if (line.contains("Drop")) {
					return mItems.getDropCharm(true);
				} else if (line.contains("Looting")) {
					return mItems.getLootingCharm(true);
				}
			} else {
				if (line.contains("Exp")) {
					return mItems.getExpCharm(false);
				} else if (line.contains("Drop")) {
					return mItems.getDropCharm(false);
				} else if (line.contains("Looting")) {
					return mItems.getLootingCharm(false);
				} else if (line.contains("Recovery")) {
					return mItems.getRecoveryCharm();
				} else if (line.contains("Traveler")) {
					return mItems.getTravelerCharm();
				} else if (line.contains("Second Chance")) {
					return mItems.getSecondChanceCharm();
				} else if (line.contains("Hunger")) {
					return mItems.getHungerCharm();
				} else if (line.contains("Quick Eat")) {
					return mItems.getQuickEatCharm();
				}
			}
			// Relic
		case 4:
			String[] relicStrings = line.split(" ");
			String relic = "Relic" + relicStrings[2];
			for (int i = 3; i < relicStrings.length; i++) {
				relic += relicStrings[i];
			}
			return MythicMobs.inst().getItemManager().getItemStack(relic);
		}
		return null;
	}

	public void breakSecondChance(ItemStack item) {
		if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
			ItemMeta meta = item.getItemMeta();
			ArrayList<String> lore = (ArrayList<String>) item.getItemMeta().getLore();
			for (int i = lore.size() - 1; i > lore.size() - 5 && i >= 0; i--) {
				if (lore.get(i).contains("Second Chance")) {
					int slotLevel = util.getItemLevel(item);
					lore.set(i, "�8(Lv " + slotLevel + " Slot)");
					meta.setLore(lore);
					item.setItemMeta(meta);
				}
			}
		}
	}

	public int countSlots(ItemStack item) {
		ArrayList<String> lore = (ArrayList<String>) item.getItemMeta().getLore();
		int count = 0;
		boolean hasBonus = false;
		for (String line : lore) {
			if (!hasBonus) {
				if (line.contains("Bonus")) {
					hasBonus = true;
				}
			} else {
				if (!line.contains("Durability")) {
					count++;
				}
			}
		}
		return count;
	}
	
	public ArrayList<String> getBonusSlots(ItemStack item) {
		ArrayList<String> slots = new ArrayList<String>();
		ArrayList<String> lore = (ArrayList<String>) item.getItemMeta().getLore();
		boolean hasBonus = false;
		for (String line : lore) {
			if (!hasBonus) {
				if (line.contains("Bonus")) {
					hasBonus = true;
				}
			} else {
				if (!line.contains("Durability")) {
					slots.add(line);
				}
			}
		}
		return slots;
	}

	public boolean isSlotAvailable(ItemStack item, int slot) {
		ArrayList<String> lore = (ArrayList<String>) item.getItemMeta().getLore();
		int count = 0;
		boolean hasBonus = false;
		for (String line : lore) {
			if (!hasBonus) {
				if (line.contains("Bonus")) {
					hasBonus = true;
				}
			} else {
				count++;
				// If the matching slot is empty, return true
				if (slot == count) {
					if (line.contains("Slot")) {
						return true;
					} else {
						return false;
					}
				}
			}
		}
		return false;
	}

	public int getAvailableSlot(ItemStack item) {
		ArrayList<String> lore = (ArrayList<String>) item.getItemMeta().getLore();
		int count = 0;
		boolean hasBonus = false;
		for(String line : lore) {
			if (!hasBonus) {
				if(line.contains("Bonus")) {
					hasBonus = true;
				}
			}
			else {
				count++;
				// If the matching slot is empty, return true
				if (line.contains("Slot")) {
					return count;
				}
			}
		}
		if (hasBonus) {
			return 0;
		}
		return -1;
	}

	public boolean isSlotUsed(ItemStack item, int slot) {
		ArrayList<String> lore = (ArrayList<String>) item.getItemMeta().getLore();
		int count = 0;
		boolean hasBonus = false;
		for (String line : lore) {
			if (!hasBonus) {
				if (line.contains("Bonus")) {
					hasBonus = true;
				}
			} else {
				count++;
				// If the matching slot is empty, return true
				if (slot == count) {
					if (!line.contains("Slot") && !line.contains("/")) {
						return true;
					} else {
						return false;
					}
				}
			}
		}
		return false;
	}

	public int getSlotNum(ItemStack item, int slot) {
		ArrayList<String> lore = (ArrayList<String>) item.getItemMeta().getLore();
		int count = 0;
		int lineNum = 0;
		boolean hasBonus = false;
		for (String line : lore) {
			if (!hasBonus) {
				if (line.contains("Bonus")) {
					hasBonus = true;
				}
			} else {
				count++;
				// If the matching slot is empty, return true
				if (slot == count) {
					return lineNum;
				}
			}
			lineNum++;
		}
		return -1;
	}

	public String getSlotLine(ItemStack item, int slot) {
		ArrayList<String> lore = (ArrayList<String>) item.getItemMeta().getLore();
		int count = 0;
		boolean hasBonus = false;
		for (String line : lore) {
			if (!hasBonus) {
				if (line.contains("Bonus")) {
					hasBonus = true;
				}
			} else {
				count++;
				if (slot == count) {
					return line;
				}
			}
		}
		return null;
	}
	
	public int getSlotLevel(int slot, ItemStack item) {
		ArrayList<String> lore = (ArrayList<String>) item.getItemMeta().getLore();
		int count = 0;
		boolean hasBonus = false;
		for(String line : lore) {
			if (!hasBonus) {
				if(line.contains("Bonus")) {
					hasBonus = true;
				}
			}
			else {
				count++;
				// If the matching slot is empty, return true
				if(slot == count) {
					return Integer.parseInt(line.split(" ")[1]);
				}
			}
		}
		return -1;
	}

	public String getSlotLevel(ItemStack item, int slot) {
		ArrayList<String> lore = (ArrayList<String>) item.getItemMeta().getLore();
		int count = 0;
		boolean hasBonus = false;
		for (String line : lore) {
			if (!hasBonus) {
				if (line.contains("Bonus")) {
					hasBonus = true;
				}
			} else {
				count++;
				if (slot == count) {
					String num = line.split(" ")[1];
					String encode = "";
					for (int i = 0; i < 3 - num.length(); i++) {
						encode += "�0";
					}
					for (int i = 0; i < num.length(); i++) {
						encode += "�" + num.substring(i, i+1);
					}
					return encode;
				}
			}
		}
		return null;
	}

	public String getSlottedLevel(ItemStack item) {
		ArrayList<String> lore = (ArrayList<String>) item.getItemMeta().getLore();
		for (String line : lore) {
			if (line.contains("Level")) {
				String num = line.split(" ")[1];
				String encode = "";
				for (int i = 0; i < 3 - num.length(); i++) {
					encode += "�0";
				}
				for (int i = 0; i < num.length(); i++) {
					encode += "�" + num.charAt(i);
				}
				return encode;
			}
		}
		return null;
	}

	public void removeSlotLine(ItemStack item, int slot) {
		ArrayList<String> lore = (ArrayList<String>) item.getItemMeta().getLore();
		ItemMeta meta = item.getItemMeta();
		int count = 0;
		int lineNum = 0;
		boolean hasBonus = false;
		for (String line : lore) {
			if (!hasBonus) {
				if (line.contains("Bonus")) {
					hasBonus = true;
				}
			} else {
				count++;
				if (slot == count) {
					lore.remove(lineNum);

					// Also remove bonus attributes line if necessary
					if (lineNum + 1 == lore.size() && count == 1) {
						lore.remove(lineNum - 1);
					}
					break;
				}
			}
			lineNum++;
		}
		meta.setLore(lore);
		item.setItemMeta(meta);
	}

	public String getAttributeType(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		ArrayList<String> lore = (ArrayList<String>) meta.getLore();
		for (int i = 0; i < lore.size(); i++) {
			if (lore.get(i).contains("Effect: Increases")) {
				String[] temp = lore.get(i).split(" ");
				if (temp[3].charAt(temp[3].length() - 1) == ',') {
					temp[3] = temp[3].substring(0, temp[3].length() - 1);
				}
				return temp[3].substring(0, 1).toUpperCase() + temp[3].substring(1, temp[3].length());
			}
		}
		return null;
	}

	public String getSlotLineAttribute(String line) {
		if (line.contains("Strength")) {
			return "strength";
		} else if (line.contains("Dexterity")) {
			return "dexterity";
		} else if (line.contains("Intelligence")) {
			return "intelligence";
		} else if (line.contains("Spirit")) {
			return "spirit";
		} else if (line.contains("Perception")) {
			return "perception";
		} else if (line.contains("Endurance")) {
			return "endurance";
		} else if (line.contains("Vitality")) {
			return "vitality";
		} else {
			return null;
		}
	}
	
	// Returns null if can slot, otherwise returns an error
	public String canSlot(ItemStack item, ItemStack augment, int slotLevel, int augmentLevel, String type) {
		if (augmentLevel > slotLevel && 
				(augment.getType().equals(Material.PRISMARINE_CRYSTALS) ||
				augment.getType().equals(Material.QUARTZ))) {
			return "&cThis augment must be same level or below, " + augmentLevel + " is greater than " + slotLevel + "!";
		}
		else if (augmentLevel != slotLevel) {
			return "&cThis augment must be same level as slot, " + augmentLevel + " does not equal " + slotLevel + "!";
		}
		
		if (Util.isArmor(item) && type.contains("weapon")) {
			return "&cThis augment does not work on armor!";
		}
		if (Util.isWeapon(item) && type.contains("armor")) {
			return "&cThis augment does not work on weapons!";
		}
		return null;
	}

	public String slotType(ItemStack item) {
		if (!item.hasItemMeta() || !item.getItemMeta().hasLore()) {
			return null;
		}
		if (item.getItemMeta().getLore().size() < 2) {
			return null;
		}

		String line = item.getItemMeta().getLore().get(1);
		String charmLine = item.getItemMeta().getLore().get(0);
		if (line.contains("max durability")) {
			return "durability";
		} else if (line.contains("Increases weapon")) {
			if (charmLine.contains("Overload")) {
				return "weaponoverload";
			}
			return "weaponattribute";
		} else if (line.contains("Increases armor")) {
			if (charmLine.contains("Overload")) {
				return "armoroverload";
			}
			return "armorattribute";
		} else if (charmLine.contains("Charm")) {
			return "charm";
		} else if (charmLine.contains("Relic")) {
			return "relic";
		}
		return null;
	}

	public boolean parseDurability(ItemStack itemWithSlot, ItemStack itemToSlot, int slot) {
		int potency = -1;
		for (String line : itemToSlot.getItemMeta().getLore()) {
			if (line.contains("Potency")) {
				potency = Integer.parseInt(line.substring(line.indexOf(":") + 4));
			}
		}
		if (potency == -1) {
			return false;
		}
		util.setMaxDurability(itemWithSlot, potency + util.getMaxDurability(itemWithSlot));
		ItemMeta meta = itemWithSlot.getItemMeta();
		String slotLevel = getSlotLevel(itemWithSlot, slot); // Slot
		String slottedLevel = getSlottedLevel(itemToSlot); // Augment
		ArrayList<String> lore = (ArrayList<String>) meta.getLore();
		lore.set(getSlotNum(itemWithSlot, slot),
				slotLevel + slottedLevel + "�0�0�0�0�9Max Durability +" + potency);
		meta.setLore(lore);
		itemWithSlot.setItemMeta(meta);
		return true;
	}

	public boolean parseAttribute(ItemStack itemWithSlot, ItemStack itemToSlot, int slot) {
		int potency = -1;
		for (String line : itemToSlot.getItemMeta().getLore()) {
			if (line.contains("Potency")) {
				potency = Integer.parseInt(line.substring(line.indexOf(":") + 4));
			}
		}
		if (potency == -1) {
			return false;
		}
		ItemMeta meta = itemWithSlot.getItemMeta();
		String slotLevel = getSlotLevel(itemWithSlot, slot);
		String slottedLevel = getSlottedLevel(itemToSlot);
		ArrayList<String> lore = (ArrayList<String>) meta.getLore();
		lore.set(getSlotNum(itemWithSlot, slot),
				slotLevel + slottedLevel + "�1�0�0�0�9" + getAttributeType(itemToSlot) + " +" + potency);
		meta.setLore(lore);
		itemWithSlot.setItemMeta(meta);
		return true;
	}

	public boolean parseOverload(ItemStack itemWithSlot, ItemStack itemToSlot, int slot) {
		int potency = -1;
		int durabilityLoss = -1;
		String durabilityLossString = null;
		for (String line : itemToSlot.getItemMeta().getLore()) {
			if (line.contains("Potency")) {
				potency = Integer.parseInt(line.substring(line.indexOf(":") + 4));
			}
			if (line.contains("Durability Lost")) {
				durabilityLossString = line.substring(line.indexOf(":") + 4);
				durabilityLoss = Integer.parseInt(durabilityLossString);
			}
		}
		if (potency == -1) {
			return false;
		}
		if (util.getMaxDurability(itemWithSlot) - durabilityLoss <= 0) {
			return false;
		}
		util.setMaxDurability(itemWithSlot, util.getMaxDurability(itemWithSlot) - durabilityLoss);
		String encodedDurabilityLoss = durabilityLossString.replaceAll("", "�");
		encodedDurabilityLoss = encodedDurabilityLoss.substring(0, encodedDurabilityLoss.length() - 1);
		ItemMeta meta = itemWithSlot.getItemMeta();
		String slotLevel = getSlotLevel(itemWithSlot, slot);
		String slottedLevel = getSlottedLevel(itemToSlot);
		ArrayList<String> lore = (ArrayList<String>) meta.getLore();

		lore.set(getSlotNum(itemWithSlot, slot), slotLevel + slottedLevel + "�2" + encodedDurabilityLoss
				+ "�c" + getAttributeType(itemToSlot) + " +" + potency);
		meta.setLore(lore);
		itemWithSlot.setItemMeta(meta);
		return true;
	}

	public boolean parseCharm(Player p, ItemStack itemWithSlot, ItemStack itemToSlot, int slot) {
		if (Util.isArmor(itemWithSlot)) {
			Util.sendMessage(p, "&cCharms can only be slotted on weapons!");
			return false;
		}
		ItemMeta meta = itemWithSlot.getItemMeta();
		ArrayList<String> lore = (ArrayList<String>) meta.getLore();

		String[] charmStrings = itemToSlot.getItemMeta().getLore().get(0).split(" ");
		String charm = "";
		for (int i = 2; i < charmStrings.length; i++) {
			charm += charmStrings[i];
			if (i < charmStrings.length - 1) {
				charm += " ";
			}
		}

		lore.set(getSlotNum(itemWithSlot, slot), "�9" + charm);
		meta.setLore(lore);
		itemWithSlot.setItemMeta(meta);
		return true;
	}

	public boolean parseRelic(Player p, ItemStack itemWithSlot, ItemStack itemToSlot, int slot) {
		ItemMeta meta = itemWithSlot.getItemMeta();
		ArrayList<String> lore = (ArrayList<String>) meta.getLore();

		String[] relicStrings = itemToSlot.getItemMeta().getLore().get(0).split(" ");
		String relic = "�b" + relicStrings[2];
		for (int i = 3; i < relicStrings.length; i++) {
			relic += " " + relicStrings[i];
		}

		lore.set(getSlotNum(itemWithSlot, slot), relic);
		meta.setLore(lore);
		itemWithSlot.setItemMeta(meta);
		return true;
	}
	
	public boolean hasRelic(ItemStack item) {
		if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
			ArrayList<String> lore = (ArrayList<String>) item.getItemMeta().getLore();
			
			int count = 1;
			for (int i = lore.size() - 1; i > 0 && count <= 3; i--) {
				if (lore.get(i).contains("Relic")) return true;
				count++;
			}
		}
		return false;
	}
}

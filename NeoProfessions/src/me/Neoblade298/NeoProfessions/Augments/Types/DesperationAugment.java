package me.Neoblade298.NeoProfessions.Augments.Types;

import java.util.List;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.player.PlayerData;

import me.Neoblade298.NeoProfessions.Augments.Augment;
import me.Neoblade298.NeoProfessions.Augments.EventType;

public class DesperationAugment extends ModDamageDealtAugment {
	double manaTaken;
	
	public DesperationAugment() {
		super();
		this.name = "Desperation";
		this.etype = EventType.DAMAGE;
	}

	public DesperationAugment(int level) {
		super(level);
		this.name = "Desperation";
		this.etype = EventType.DAMAGE;
	}

	@Override
	public double getFlatBonus(LivingEntity user) {
		return 0;
	}

	@Override
	public double getMultiplierBonus(LivingEntity user) {
		return 0.03 * (level / 5);
	}

	@Override
	public Augment createNew(int level) {
		return new DesperationAugment(level);
	}

	@Override
	public boolean canUse(Player user, LivingEntity target) {
		PlayerData pdata = SkillAPI.getPlayerData(user);
		return (pdata.getMana() / pdata.getMaxMana()) < 0.2 && pdata.getClass("class").getData().getManaName().endsWith("MP");
	}

	public ItemStack getItem(Player user) {
		ItemStack item = super.getItem(user);
		ItemMeta meta = item.getItemMeta();
		List<String> lore = meta.getLore();
		lore.add("�7Increases damage by �f" + getMultiplierBonus(user) + "% �7when dealing");
		lore.add("�7damage while below 20% mana.");
		lore.add("�cOnly works with mana.");
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}
}

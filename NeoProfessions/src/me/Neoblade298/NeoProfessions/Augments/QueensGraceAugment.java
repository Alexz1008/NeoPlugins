package me.Neoblade298.NeoProfessions.Augments;

import java.util.Arrays;
import java.util.List;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.event.entity.EntityPotionEffectEvent.Action;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.enums.ManaSource;
import com.sucy.skill.api.event.FlagApplyEvent;
import com.sucy.skill.api.util.FlagManager;

public class QueensGraceAugment extends Augment implements ModPotionAugment {
	
	public QueensGraceAugment() {
		super();
		this.name = "Queen's Grace";
		this.etypes = Arrays.asList(new EventType[] {EventType.POTION});
	}

	public QueensGraceAugment(int level) {
		super(level);
		this.name = "Queen's Grace";
		this.etypes = Arrays.asList(new EventType[] {EventType.POTION});
	}

	@Override
	public Augment createNew(int level) {
		return new QueensGraceAugment(level);
	}
	
	@Override
	public void applyPotionEffects(Player user, EntityPotionEffectEvent e) {
		FlagManager.addFlag(user, user, "aug_queensGrace", 100);
		SkillAPI.getPlayerData(user).giveMana(5, ManaSource.SPECIAL);
		user.setHealth(Math.min(user.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(), user.getHealth() + 50));
	}
	
	@Override
	public boolean canUse(Player user, EntityPotionEffectEvent e) {
		return !FlagManager.hasFlag(user, "aug_queensGrace") && e.getAction().equals(Action.ADDED) &&
				SkillAPI.getPlayerData(user).getClass("class").getData().getManaName().endsWith("MP");
	}
	
	@Override
	public boolean isPermanent() {
		return true;
	}

	@Override
	public String getLine() {
		return "�7[�9�o" + name + " Lv " + level + "�7]";
	}

	public ItemStack getItem(Player user) {
		ItemStack item = super.getItem(user);
		ItemMeta meta = item.getItemMeta();
		List<String> lore = meta.getLore();
		lore.add("�7Receiving any potion effect grants");
		lore.add("�f50 �7health and �f5 �7mana. 5s");
		lore.add("�7cooldown.Only works with &9Mana&7.");
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}

}

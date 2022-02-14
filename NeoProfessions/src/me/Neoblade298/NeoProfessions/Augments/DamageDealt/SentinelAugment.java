package me.Neoblade298.NeoProfessions.Augments.DamageDealt;

import java.util.Arrays;
import java.util.List;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.sucy.skill.api.util.FlagManager;

import me.Neoblade298.NeoProfessions.Augments.Augment;
import me.Neoblade298.NeoProfessions.Augments.EventType;

public class SentinelAugment extends Augment implements ModDamageDealtAugment {
	double maxHealthMod;
	
	public SentinelAugment() {
		super();
		this.name = "Sentinel";
		this.etypes = Arrays.asList(new EventType[] {EventType.DAMAGE_DEALT});
		this.maxHealthMod = (this.level / 5) * 0.001;
	}

	public SentinelAugment(int level) {
		super(level);
		this.name = "Sentinel";
		this.etypes = Arrays.asList(new EventType[] {EventType.DAMAGE_DEALT});
		this.maxHealthMod = (this.level / 5) * 0.001;
	}

	@Override
	public void applyDamageDealtEffects(Player user, LivingEntity target, double damage) {
		FlagManager.addFlag(user, user, "aug_sentinel", 20);
	}

	@Override
	public double getDamageDealtFlat(LivingEntity user) {
		return user.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * this.maxHealthMod;
	}

	@Override
	public Augment createNew(int level) {
		return new SentinelAugment(level);
	}

	@Override
	public boolean canUse(Player user, LivingEntity target) {
		double percentage = target.getHealth() / target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
		return percentage > 0.95 && FlagManager.hasFlag(user, "aug_sentinel");
	}

	public ItemStack getItem(Player user) {
		ItemStack item = super.getItem(user);
		ItemMeta meta = item.getItemMeta();
		List<String> lore = meta.getLore();
		lore.add("�7Increases damage by �f" + getDamageDealtFlat(user) + ", �7which");
		lore.add("�7is �f" + formatPercentage(this.maxHealthMod) + "% �7of your max health.");
		lore.add("�f2 �7second cooldown.");
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}
}

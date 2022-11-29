package me.Neoblade298.NeoProfessions.Augments.Builtin;

import java.util.Arrays;
import java.util.List;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.sucy.skill.api.event.PlayerCalculateDamageEvent;

import me.Neoblade298.NeoProfessions.Augments.Augment;
import me.Neoblade298.NeoProfessions.Augments.EventType;
import me.Neoblade298.NeoProfessions.Augments.ModDamageTakenAugment;

public class RedemptionAugment extends Augment implements ModDamageTakenAugment {
	
	public RedemptionAugment() {
		super();
		this.name = "Redemption";
		this.etypes = Arrays.asList(new EventType[] {EventType.DAMAGE_TAKEN});
	}

	public RedemptionAugment(int level) {
		super(level);
		this.name = "Redemption";
		this.etypes = Arrays.asList(new EventType[] {EventType.DAMAGE_TAKEN});
	}

	@Override
	public Augment createNew(int level) {
		return new RedemptionAugment(level);
	}

	@Override
	public boolean canUse(Player user, LivingEntity target, PlayerCalculateDamageEvent e) {
		for (String type : e.getTypes()) {
			if (type.endsWith("corruption")) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void applyDamageTakenEffects(Player user, LivingEntity target, PlayerCalculateDamageEvent e) {
		user.setHealth(Math.min(user.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(), user.getHealth() + e.getDamage()));
		e.setDamage(0);
	}
	
	@Override
	public boolean isPermanent() {
		return true;
	}

	@Override
	public String getLine() {
		return "§7[§4§o" + name + " Lv " + level + "§7]";
	}

	public ItemStack getItem(Player user) {
		ItemStack item = super.getItem(user);
		ItemMeta meta = item.getItemMeta();
		List<String> lore = meta.getLore();
		lore.add("§7Converts all corruption damage to");
		lore.add("§7healing.");
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}

}

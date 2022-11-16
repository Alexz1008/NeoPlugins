package me.Neoblade298.NeoProfessions.Augments.Builtin;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.sucy.skill.api.enums.ManaSource;
import com.sucy.skill.api.event.PlayerCalculateDamageEvent;
import com.sucy.skill.api.player.PlayerData;
import com.sucy.skill.api.util.FlagManager;

import me.Neoblade298.NeoProfessions.Augments.Augment;
import me.Neoblade298.NeoProfessions.Augments.EventType;
import me.Neoblade298.NeoProfessions.Augments.ModDamageTakenAugment;
import me.Neoblade298.NeoProfessions.Augments.ModManaGainAugment;
import me.Neoblade298.NeoProfessions.Managers.AugmentManager;

public class MenacingAugment extends Augment implements ModDamageTakenAugment, ModManaGainAugment {
	private static double damageTakenMult = AugmentManager.getValue("menacing.damage-reduction-multiplier-base");
	private static double damageTakenMultLvl = AugmentManager.getValue("menacing.damage-reduction-multiplier-per-lvl");
	private static double resourceMult = AugmentManager.getValue("menacing.resource-regen-multiplier-base");
	private static double resourceMultLvl = AugmentManager.getValue("menacing.resource-regen-multiplier-per-lvl");
	private static int maxDistance = (int) AugmentManager.getValue("menacing.distance-max");
	private static int maxDistanceSq = (int) Math.pow(AugmentManager.getValue("menacing.distance-max"), 2);
	
	public MenacingAugment() {
		super();
		this.name = "Menacing";
		this.etypes = Arrays.asList(new EventType[] {EventType.DAMAGE_TAKEN, EventType.MANA_GAIN});
	}

	public MenacingAugment(int level) {
		super(level);
		this.name = "Menacing";
		this.etypes = Arrays.asList(new EventType[] {EventType.DAMAGE_TAKEN, EventType.MANA_GAIN});
	}
	
	@Override
	public void applyDamageTakenEffects(Player user, LivingEntity target, PlayerCalculateDamageEvent e) {
		FlagManager.addFlag(user, user, "aug_menacing", 40);
	}

	@Override
	public double getManaGainMult(Player user) {
		return resourceMult + (resourceMultLvl * ((level / 5) - 1));
	}
	
	@Override
	public double getDamageTakenMult(LivingEntity user) {
		return damageTakenMult + (damageTakenMultLvl * ((level / 5) - 1));
	}

	@Override
	public Augment createNew(int level) {
		return new MenacingAugment(level);
	}

	@Override
	public boolean canUse(Player user, LivingEntity target, PlayerCalculateDamageEvent e) {
		Location diff = user.getLocation().subtract(target.getLocation());
		double diffxy = (diff.getX() * diff.getX()) + (diff.getY() * diff.getY());
		return diffxy <= maxDistanceSq;
	}

	public ItemStack getItem(Player user) {
		ItemStack item = super.getItem(user);
		ItemMeta meta = item.getItemMeta();
		List<String> lore = meta.getLore();
		lore.add("§7Decreases damage taken by §f" + formatPercentage(getDamageTakenMult(user)) + "%");
		lore.add("§7when within " + maxDistance + " blocks of an enemy.");
		lore.add("§7Taking damage from this distance");
		lore.add("§7increases resource regen by 10%");
		lore.add("§7for 2 seconds.");
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}

	@Override
	public boolean canUse(PlayerData user, ManaSource src) {
		return FlagManager.hasFlag(user.getPlayer(), "aug_menacing");
	}

}

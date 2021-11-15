package me.Neoblade298.NeoProfessions.Augments;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public abstract class ModDamageDealtAugment extends Augment{
	public abstract double getFlatBonus();
	public abstract double getMultiplierBonus();
	public abstract boolean canUse(Player user, LivingEntity target);
}

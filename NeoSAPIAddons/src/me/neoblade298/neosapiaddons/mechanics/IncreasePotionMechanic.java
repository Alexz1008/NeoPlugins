package me.neoblade298.neosapiaddons.mechanics;

import java.util.List;

import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.google.common.collect.ImmutableList;
import com.sucy.skill.dynamic.ComponentType;
import com.sucy.skill.dynamic.custom.CustomEffectComponent;
import com.sucy.skill.dynamic.custom.EditorOption;

public class IncreasePotionMechanic extends CustomEffectComponent {

	@Override
	public String getDescription() {
		return "Add absorption to the player.";
	}

	@Override
	public List<EditorOption> getOptions() {
        return ImmutableList.of(
        		// key, name, desc, base, scale
                EditorOption.number(
                        "amount",
                        "Amount",
                        "How much to increase the potion by",
                        1,
                        0),
                EditorOption.number(
                        "duration",
                        "Duration",
                        "How long the potion effect should last in seconds",
                        15,
                        0),
                EditorOption.number(
                        "max",
                        "Max",
                        "The upper limit to how much the potion effect should increase, inclusive",
                        4,
                        0),
                // key, name, description, default
        		EditorOption.text("potion", "Potion", "What potion to increase", "FAST_DIGGING"));
	}

	@Override
	public boolean execute(LivingEntity caster, int lvl, List<LivingEntity> targets, double critChance) {
		int amt = (int) parseValues(caster, "amount", lvl, 0, critChance);
		int duration = (int) parseValues(caster, "duration", lvl, 0, critChance) * 20;
		int max = (int) parseValues(caster, "max", lvl, 0, critChance);
		PotionEffectType type = PotionEffectType.getByName(settings.getString("potion"));
		
		for (LivingEntity target : targets) {
			PotionEffect pe = target.getPotionEffect(type);
			if (pe == null) {
				target.addPotionEffect(new PotionEffect(type, duration, amt > max ? max : amt));
			}
			else {
				int newAmt = pe.getAmplifier() + amt > max ? max : pe.getAmplifier() + amt;
				target.addPotionEffect(new PotionEffect(type, duration, newAmt));
			}
		}
		return true;
	}

	@Override
	public String getKey() {
		return "Increase Potion";
	}

	@Override
	public ComponentType getType() {
		return ComponentType.MECHANIC;
	}

}

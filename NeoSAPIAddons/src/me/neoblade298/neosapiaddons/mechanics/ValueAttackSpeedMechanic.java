package me.neoblade298.neosapiaddons.mechanics;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import com.google.common.collect.ImmutableList;
import com.sucy.skill.dynamic.ComponentType;
import com.sucy.skill.dynamic.DynamicSkill;
import com.sucy.skill.dynamic.custom.CustomEffectComponent;
import com.sucy.skill.dynamic.custom.EditorOption;

public class ValueAttackSpeedMechanic extends CustomEffectComponent {

	@Override
	public String getDescription() {
		return "Get the maximum of all available attributes.";
	}

	@Override
	public List<EditorOption> getOptions() {
        return ImmutableList.of(
                EditorOption.text(
                        "key",
                        "Key",
                        "The key to place attack speed into; Divides if it already exists",
                        "value")
        );
	}

	@Override
	public boolean execute(LivingEntity caster, int lvl, List<LivingEntity> targets, double critChance) {
		Collection<AttributeModifier> mods = ((Player) caster).getAttribute(Attribute.GENERIC_ATTACK_SPEED).getModifiers();
		double amount = ((Player) caster).getAttribute(Attribute.GENERIC_ATTACK_SPEED).getBaseValue();
		for (AttributeModifier mod : mods) {
			if (mod.getName().contains("modifier")) {
				// Weapon/tool modifier, we want only this
				amount += mod.getAmount();
			}
			else if (mod.getName().contains("GENERIC_ATTACK")) {
				amount = 10;
			}
		}
		
		String key = settings.getString("key");
		HashMap<String, Object> data = DynamicSkill.getCastData(caster);
		if (data.containsKey(key)) {
			data.put(key, (double) data.get(key) / amount);
		}
		else {
			data.put(key, amount);
		}
		return true;
	}

	@Override
	public String getKey() {
		return "Value Attackspeed";
	}

	@Override
	public ComponentType getType() {
		return ComponentType.MECHANIC;
	}

}

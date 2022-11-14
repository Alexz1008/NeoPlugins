package me.neoblade298.neosapiaddons.conditions;

import java.util.List;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;
import com.sucy.skill.dynamic.ComponentType;
import com.sucy.skill.dynamic.custom.CustomEffectComponent;
import com.sucy.skill.dynamic.custom.EditorOption;

import io.lumine.mythic.bukkit.MythicBukkit;

public class AttackChargeCondition extends CustomEffectComponent {

	@Override
	public String getDescription() {
		return "Check chargeup power of player.";
	}

	@Override
	public List<EditorOption> getOptions() {
        return ImmutableList.of(
                EditorOption.text(
                        "min",
                        "Min",
                        "Minimum chargeup inclusive",
                        "1"),
                EditorOption.text(
                        "max",
                        "Max",
                        "Maximum chargeup inclusive",
                        "1")
        );
	}

	@Override
	public boolean execute(LivingEntity caster, int lvl, List<LivingEntity> targets, double critChance) {
		double min = settings.getDouble("min");
		double max = settings.getDouble("max");
		float charge = MythicBukkit.inst().getVolatileCodeHandler().getItemRecharge((Player) caster);
		return min <= charge && charge <= max && executeChildren(caster, lvl, targets, critChance);
	}

	@Override
	public String getKey() {
		return "Attack Charge";
	}

	@Override
	public ComponentType getType() {
		return ComponentType.CONDITION;
	}

}

package me.neoblade298.neosapiaddons.conditions;

import java.util.List;

import org.bukkit.entity.LivingEntity;
import com.google.common.collect.ImmutableList;
import com.sucy.skill.dynamic.ComponentType;
import com.sucy.skill.dynamic.custom.CustomEffectComponent;
import com.sucy.skill.dynamic.custom.EditorOption;

public class AbsorptionCondition extends CustomEffectComponent {

	@Override
	public String getDescription() {
		return "Check absorption of the player.";
	}

	@Override
	public List<EditorOption> getOptions() {
        return ImmutableList.of(
                EditorOption.text(
                        "min",
                        "Min",
                        "Minimum absorption inclusive",
                        "0"),
                EditorOption.text(
                        "max",
                        "Max",
                        "Maximum absorption inclusive",
                        "1")
        );
	}

	@Override
	public boolean execute(LivingEntity caster, int lvl, List<LivingEntity> targets, double critChance) {
		int min = settings.getInt("min");
		int max = settings.getInt("max");
		return min <= caster.getAbsorptionAmount() && caster.getAbsorptionAmount() <= max && executeChildren(caster, lvl, targets, critChance);
	}

	@Override
	public String getKey() {
		return "Absorption";
	}

	@Override
	public ComponentType getType() {
		return ComponentType.CONDITION;
	}

}

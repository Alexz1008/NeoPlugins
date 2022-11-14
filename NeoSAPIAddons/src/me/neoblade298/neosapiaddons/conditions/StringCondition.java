package me.neoblade298.neosapiaddons.conditions;

import java.util.List;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;
import com.sucy.skill.dynamic.ComponentType;
import com.sucy.skill.dynamic.custom.CustomEffectComponent;
import com.sucy.skill.dynamic.custom.EditorOption;

import io.lumine.mythic.bukkit.MythicBukkit;

public class StringCondition extends CustomEffectComponent {

	@Override
	public String getDescription() {
		return "Compare a value to a string";
	}

	@Override
	public List<EditorOption> getOptions() {
        return ImmutableList.of(
                EditorOption.text(
                        "key",
                        "Key",
                        "Key of value to compare",
                        "value"),
                EditorOption.text(
                        "comp",
                        "Comparator",
                        "Case-insensitive string to compare to",
                        "string")
        );
	}

	@Override
	public boolean execute(LivingEntity caster, int lvl, List<LivingEntity> targets, double critChance) {
		String key = settings.getString("key");
		String comparator = settings.getString("comp");
		return key.equalsIgnoreCase(comparator);
	}

	@Override
	public String getKey() {
		return "string";
	}

	@Override
	public ComponentType getType() {
		return ComponentType.CONDITION;
	}

}

package me.neoblade298.neosapiaddons.conditions;

import java.util.HashMap;
import java.util.List;

import org.bukkit.entity.LivingEntity;
import com.google.common.collect.ImmutableList;
import com.sucy.skill.dynamic.ComponentType;
import com.sucy.skill.dynamic.DynamicSkill;
import com.sucy.skill.dynamic.custom.CustomEffectComponent;
import com.sucy.skill.dynamic.custom.EditorOption;

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
                        "comp key",
                        "Comparator",
                        "Case-insensitive value to compare to",
                        "string")
        );
	}

	@Override
	public boolean execute(LivingEntity caster, int lvl, List<LivingEntity> targets, double critChance) {
		String key = settings.getString("key");
		String comparator = settings.getString("comp");
        HashMap<String, Object> data = DynamicSkill.getCastData(caster);
        String str1 = (String) data.getOrDefault(key, "");
        String str2 = (String) data.getOrDefault(comparator, "");
		return str1.equalsIgnoreCase(str2) && executeChildren(caster, lvl, targets, critChance);
	}

	@Override
	public String getKey() {
		return "String";
	}

	@Override
	public ComponentType getType() {
		return ComponentType.CONDITION;
	}

}

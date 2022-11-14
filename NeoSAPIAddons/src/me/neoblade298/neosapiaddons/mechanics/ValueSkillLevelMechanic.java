package me.neoblade298.neosapiaddons.mechanics;

import java.util.HashMap;
import java.util.List;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import com.google.common.collect.ImmutableList;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.dynamic.ComponentType;
import com.sucy.skill.dynamic.DynamicSkill;
import com.sucy.skill.dynamic.custom.CustomEffectComponent;
import com.sucy.skill.dynamic.custom.EditorOption;

public class ValueSkillLevelMechanic extends CustomEffectComponent {

	@Override
	public String getDescription() {
		return "Get the skill level of another skill";
	}

	@Override
	public List<EditorOption> getOptions() {
        return ImmutableList.of(
                EditorOption.text(
                        "skill",
                        "Skill",
                        "The skill to use",
                        "Martial Versatility"),
                EditorOption.text(
                        "key",
                        "Key",
                        "The key to place the value in",
                        "val"),
                EditorOption.number(
                        "default",
                        "Default",
                        "The number if skill level is 0",
                        0,
                        0),
                EditorOption.number(
                        "base",
                        "Base",
                        "The base number if the skill is level 1",
                        0,
                        0),
                EditorOption.number(
                        "scale",
                        "Scale",
                        "The number to scale by skill level beyond level 1",
                        0,
                        0)
        );
	}

	@Override
	public boolean execute(LivingEntity caster, int lvl, List<LivingEntity> targets, double critChance) {
		int skillLvl = SkillAPI.getPlayerData((Player) caster).getSkill(settings.getString("skill")).getLevel();
		HashMap<String, Object> data = DynamicSkill.getCastData(caster);
		String key = settings.getString("key");
		if (skillLvl == 0) {
			data.put(key, parseValues(caster, "default", lvl, 0, critChance));
		}
		else {
			double base = parseValues(caster, "base", lvl, 0, critChance);
			double scale = parseValues(caster, "scale", lvl, 0, critChance);
			double amt = base + (scale * (skillLvl - 1));
			data.put(key, amt);
		}
		return true;
	}

	@Override
	public String getKey() {
		return "Value Skilllevel";
	}

	@Override
	public ComponentType getType() {
		return ComponentType.MECHANIC;
	}

}

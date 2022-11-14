package me.neoblade298.neosapiaddons.mechanics;

import java.util.List;

import org.bukkit.entity.LivingEntity;
import com.google.common.collect.ImmutableList;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.skills.SkillShot;
import com.sucy.skill.dynamic.ComponentType;
import com.sucy.skill.dynamic.custom.CustomEffectComponent;
import com.sucy.skill.dynamic.custom.EditorOption;

public class SudoSkillMechanic extends CustomEffectComponent {

	@Override
	public String getDescription() {
		return "Force target to cast skill";
	}

	@Override
	public List<EditorOption> getOptions() {
        return ImmutableList.of(
                EditorOption.text(
                        "skill",
                        "Skill",
                        "The skill for the target to sudocast",
                        "Taunt"),
                EditorOption.text(
                        "level",
                        "Level",
                        "The level of the skill cast, defaults to skill's level at -1",
                        "-1")
        );
	}

	@Override
	public boolean execute(LivingEntity caster, int lvl, List<LivingEntity> targets, double critChance) {
		int level = (int) settings.getDouble("level");
		String sname = settings.getString("skill");
		for (LivingEntity target : targets) {
			((SkillShot) SkillAPI.getSkill(sname)).cast(target, level);
		}
		return true;
	}

	@Override
	public String getKey() {
		return "Sudoskill";
	}

	@Override
	public ComponentType getType() {
		return ComponentType.MECHANIC;
	}

}

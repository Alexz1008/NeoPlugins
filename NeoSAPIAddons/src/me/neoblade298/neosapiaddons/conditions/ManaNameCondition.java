package me.neoblade298.neosapiaddons.conditions;

import java.util.List;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.dynamic.ComponentType;
import com.sucy.skill.dynamic.custom.CustomEffectComponent;
import com.sucy.skill.dynamic.custom.EditorOption;

public class ManaNameCondition extends CustomEffectComponent {

	@Override
	public String getDescription() {
		return "Check mana name of the player.";
	}

	@Override
	public List<EditorOption> getOptions() {
        return ImmutableList.of(
                EditorOption.text(
                        "mananame",
                        "Mana Name",
                        "Contains this mana name",
                        "MP")
        );
	}

	@Override
	public boolean execute(LivingEntity caster, int lvl, List<LivingEntity> targets, double critChance) {
		String mananame = settings.getString("mananame");
		if (!(caster instanceof Player)) return false;
		return SkillAPI.getPlayerData((Player) caster).getClass("class").getData().getManaName().endsWith(mananame) && executeChildren(caster, lvl, targets, critChance);
	}

	@Override
	public String getKey() {
		return "Mana Name";
	}

	@Override
	public ComponentType getType() {
		return ComponentType.CONDITION;
	}

}

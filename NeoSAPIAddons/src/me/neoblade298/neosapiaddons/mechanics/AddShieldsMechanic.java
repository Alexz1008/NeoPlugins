package me.neoblade298.neosapiaddons.mechanics;

import java.util.List;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;
import com.sucy.skill.dynamic.ComponentType;
import com.sucy.skill.dynamic.custom.CustomEffectComponent;
import com.sucy.skill.dynamic.custom.EditorOption;

import me.neoblade298.neosapiaddons.Shield;
import me.neoblade298.neosapiaddons.ShieldManager;

public class AddShieldsMechanic extends CustomEffectComponent {

	@Override
	public String getDescription() {
		return "Add shields to the player.";
	}

	@Override
	public List<EditorOption> getOptions() {
		// Amount, time to decay (can be -1), decay delay, decay amount, decay period, decay repetitions
        return ImmutableList.of(
                EditorOption.number(
                        "amount",
                        "Amount",
                        "How much shields to add",
                        100, 10),
                EditorOption.dropdown(
                        "decay-percent",
                        "Decay Is Percent",
                        "Whether the decay is % of shield or not",
                        List.of("true", "false")),
                EditorOption.number(
                        "decay-delay",
                        "Decay delay",
                        "When the shield starts decaying in seconds",
                        3, 0),
                EditorOption.number(
                        "decay-amount",
                        "Decay amount",
                        "How much to decay the shield",
                        20, 0),
                EditorOption.number(
                        "decay-period",
                        "Decay period",
                        "How often in seconds does the decay happen",
                        1, 0),
                EditorOption.number(
                        "decay-repetitions",
                        "Decay repetitions",
                        "How many times does the decay happen",
                        5, 0)
        );
	}

	@Override
	public boolean execute(LivingEntity caster, int lvl, List<LivingEntity> targets, double critChance) {
		int amt = (int) parseValues(caster, "amount", lvl, 0, critChance);
		boolean isPercent = settings.getBool("decay-percent");
		double decayDelay = parseValues(caster, "decay-delay", lvl, 0, critChance);
		double decayAmount = parseValues(caster, "decay-amount", lvl, 0, critChance);
		double decayPeriod = parseValues(caster, "decay-period", lvl, 0, critChance);
		int decayRepetitions = (int) parseValues(caster, "decay-repetitions", lvl, 0, critChance);
		
		for (LivingEntity target : targets) {
			if (target instanceof Player) {
				ShieldManager.addShields((Player) target, new Shield((Player) target, amt, isPercent, decayDelay, decayAmount, decayPeriod, decayRepetitions));
			}
		}
		return true;
	}

	@Override
	public String getKey() {
		return "Add Shields";
	}

	@Override
	public ComponentType getType() {
		return ComponentType.MECHANIC;
	}

}

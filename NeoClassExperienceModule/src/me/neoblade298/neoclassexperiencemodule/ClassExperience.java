package me.neoblade298.neoclassexperiencemodule;

import java.util.Map;

import org.bukkit.entity.Player;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.enums.ExpSource;
import com.sucy.skill.api.player.PlayerData;
import com.sucy.skill.log.Logger;

import me.blackvein.quests.CustomReward;

public class ClassExperience extends CustomReward {
	// Construct the reward
	public ClassExperience() {
		this.setName("Class Exp Reward");
		this.setAuthor("Neoblade298");
		this.setDisplay("Class Exp");
		this.addStringPrompt("Experience", "How much experience to give", 50);
	}

	// Give loot reward to a player
	@Override
	public void giveReward(Player player, Map<String, Object> data) {
		try {
			int experience = Integer.parseInt(data.get("Experience").toString());
			Logger.log("Giving player " + player.getName() + " " + experience + " experience from completing quest.");
			PlayerData playerSkills = SkillAPI.getPlayerData(player);
			playerSkills.giveExp(experience, ExpSource.QUEST);
		} catch (Exception ex) {
			Logger.bug("Something went wrong with Class Exp Reward:");
			ex.printStackTrace();
		}
	}
}
package me.neoblade298.neoquests.quests;

import java.util.ArrayList;

import org.bukkit.entity.Player;

import me.neoblade298.neoquests.actions.RewardAction;
import me.neoblade298.neoquests.objectives.ObjectiveSet;
import me.neoblade298.neoquests.objectives.ObjectiveSetInstance;

public class QuestInstance {
	private Quester q;
	private Quest quest;
	private int stage;
	private ArrayList<ObjectiveSetInstance> sets;
	
	public void completeObjectiveSet(ObjectiveSetInstance set) {
		if (set.getNext() == -1 || set.getNext() == -2) {
			endQuest(set, set.getNext() == -1, stage);
			return;
		}
		else if (set.getNext() == -3) {
			stage = stage + 1 >= quest.getStages().size() ? stage : stage + 1; // Next stage if one exists
		}
		else {
			stage = set.getNext();
		}
		
		for (ObjectiveSetInstance i : sets) {
			i.cleanup();
		}
		
		// Setup new stage
		sets.clear();
		for (ObjectiveSet os : quest.getStages().get(stage).getObjectives()) {
			sets.add(new ObjectiveSetInstance(q.getPlayer(), this, os));
		}
	}
	
	public void endQuest(ObjectiveSetInstance si, boolean success, int stage) {
		Player p = q.getPlayer();
		if (success) {
			ArrayList<RewardAction> rewards = quest.getRewards();
			if (si.getSet().hasAlternateRewards()) {
				rewards = si.getSet().getAlternateRewards();
			}
			p.sendMessage("�7You completed �e" + quest.getName() + "�7!");
			if (rewards.size() > 0) {
				p.sendMessage("�6Rewards:");
				for (RewardAction r : rewards) {
					r.run(p);
					p.sendMessage("�7- " + r.getMessage());
				}
			}
		}
		else {
			p.sendMessage("�cYou failed �e" + quest.getName() + "�c!");
		}
	}
	
	public Quest getQuest() {
		return quest;
	}
	
	public void cleanup() {
		for (ObjectiveSetInstance si : sets) {
			si.cleanup();
		}
	}
}
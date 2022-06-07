package me.neoblade298.neoquests.actions;

import java.util.HashMap;

import org.bukkit.entity.Player;

import me.Neoblade298.NeoProfessions.Managers.StorageManager;
import me.neoblade298.neocore.io.LineConfig;

public class GiveMoneyAction implements RewardAction {
	private static String key = "give-money";
	private int amount;

	public static void register(HashMap<String, Action> actions, HashMap<String, DialogueAction> dialogueActions) {
		actions.put(key, new GiveMoneyAction());
	}
	
	public GiveMoneyAction() {}
	
	public GiveMoneyAction(LineConfig cfg) {
		this.amount = cfg.getInt("amount", 1);
	}

	@Override
	public Action create(LineConfig cfg) {
		return new GiveMoneyAction(cfg);
	}

	@Override
	public String getKey() {
		return key;
	}

	@Override
	public String getDisplay() {
		return "�f" + amount + " �6Gold";
	}

	@Override
	public void run(Player p) {
		StorageManager.givePlayer(p, id, amount);
	}

}

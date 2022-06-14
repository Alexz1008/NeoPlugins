package me.neoblade298.neoquests.actions.builtin;

import java.util.HashMap;

import org.bukkit.entity.Player;

import me.Neoblade298.NeoProfessions.Managers.StorageManager;
import me.neoblade298.neocore.io.LineConfig;
import me.neoblade298.neoquests.actions.Action;
import me.neoblade298.neoquests.actions.DialogueAction;
import me.neoblade298.neoquests.actions.RewardAction;

public class GiveEffect extends RewardAction {
	private static String key = "give-storeditem";
	private String display;
	private int id;
	private int amount;

	public static void register(HashMap<String, Action> actions, HashMap<String, DialogueAction> dialogueActions) {
		actions.put(key, new GiveEffect());
	}
	
	public GiveEffect() {}
	
	public GiveEffect(LineConfig cfg) {
		super(cfg);
		this.id = cfg.getInt("id", 0);
		this.amount = cfg.getInt("amount", 1);
	}

	@Override
	public Action create(LineConfig cfg) {
		return new GiveEffect(cfg);
	}

	@Override
	public String getKey() {
		return key;
	}

	@Override
	public String getDisplay() {
		if (display == null) {
			display = "§f" + amount + " " + StorageManager.getItem(id).getDisplay();
		}
		return display;
	}

	@Override
	public void run(Player p) {
		StorageManager.givePlayer(p, id, amount);
	}

}

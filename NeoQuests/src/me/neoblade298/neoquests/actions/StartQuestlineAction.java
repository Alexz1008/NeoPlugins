package me.neoblade298.neoquests.actions;

import java.util.HashMap;

import org.bukkit.entity.Player;

import me.neoblade298.neocore.io.LineConfig;
import me.neoblade298.neoquests.conversations.ConversationManager;
import me.neoblade298.neoquests.quests.QuestsManager;

public class StartQuestlineAction implements Action {
	private static String key = "start-questline";
	private String ql;

	public static void register(HashMap<String, Action> actions, HashMap<String, DialogueAction> dialogueActions) {
		actions.put(key, new StartQuestlineAction());
	}
	
	public StartQuestlineAction() {}
	
	public StartQuestlineAction(LineConfig cfg) {
		this.ql = cfg.getString("ql", "N/A");
	}

	@Override
	public Action create(LineConfig cfg) {
		return new StartQuestlineAction(cfg);
	}

	@Override
	public String getKey() {
		return key;
	}

	@Override
	public void run(Player p) {
		QuestsManager.getQuester(p).addQuestline(ql);
	}

}

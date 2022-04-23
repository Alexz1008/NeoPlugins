package me.neoblade298.neomythicextension.conditions;

import org.bukkit.Bukkit;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.conditions.IEntityCondition;
import me.neoblade298.neobossinstances.Main;

public class PlayersInBossCondition implements IEntityCondition {
	private int min;
	private int max;
	private String boss;
	protected final me.neoblade298.neobossinstances.Main nbi;
    
    public PlayersInBossCondition(MythicLineConfig mlc) {
        this.boss = mlc.getString(new String[] {"boss", "b"}, "Ratface");
        this.min = mlc.getInteger("min", 0);
        this.max = mlc.getInteger("max", 0);
        this.nbi = (Main) Bukkit.getPluginManager().getPlugin("NeoBossInstances");
    }

    public boolean check(AbstractEntity t) {
    	int numPlayers = 0;
		if (nbi.getActiveFights().containsKey(this.boss)) numPlayers = nbi.getActiveFights().get(this.boss).size();
		return min <= numPlayers && numPlayers <= max;
    }
}

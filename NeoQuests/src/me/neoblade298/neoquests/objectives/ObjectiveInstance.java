package me.neoblade298.neoquests.objectives;

import org.bukkit.entity.Player;

import me.neoblade298.neoquests.listeners.ObjectiveListener;

public class ObjectiveInstance {
	private Player p;
	private Objective obj;
	private ObjectiveSetInstance set;
	private int count;

	public ObjectiveInstance(Player p, Objective obj, ObjectiveSetInstance set) {
		this.p = p;
		this.obj = obj;
		this.set = set;
	}

	public Objective getObjective() {
		return obj;
	}

	public Player getPlayer() {
		return p;
	}

	public int getCount() {
		return count;
	}

	public boolean setCount(int count) {
		this.count = Math.min(obj.getNeeded(), count);
		if (isComplete()) {
			set.checkCompletion();
			return true;
		}
		return false;
	}

	public boolean incrementCount() {
		if (this.count < obj.getNeeded()) {
			this.count++;
		}
		if (isComplete()) {
			set.checkCompletion();
			return true;
		}
		return false;
	}

	public boolean isComplete() {
		updateCount();
		return count >= obj.getNeeded();
	}
	
	public void cleanup() {
		ObjectiveListener.removeObjective(this);
	}
	
	public void finalize(Player p) {
		obj.finalize(p);
	}
	
	public void beginListening(Player p) {
		ObjectiveListener.addObjective(this);
	}

	public void updateCount() {}
}

package me.neoblade298.neocore.bar;

import java.util.ArrayList;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.neoblade298.neocore.NeoCore;
import me.neoblade298.neocore.player.PlayerTags;

public class CoreBar {
	private BossBar bar;
	private String topic = "";
	private boolean enabled;
	private boolean isProgressing = false;
	private ArrayList<Runnable> runAfterProgressing = new ArrayList<Runnable>();
	
	public CoreBar(Player p, BossBar bar, PlayerTags ptags) {
		this.bar = bar;
		bar.setVisible(false);
		bar.addPlayer(p);
		enabled = !ptags.exists("disable-corebar", p.getUniqueId());
	}
	
	public void setTitle(String title) {
		if (!enabled) return;
		
		if (isProgressing) {
			runAfterProgressing.add(new Runnable() {
				public void run() {
					if (!bar.isVisible()) bar.setVisible(true);
					bar.setTitle(title);
				}
			});
		}
		else {
			if (!bar.isVisible()) bar.setVisible(true);
			bar.setTitle(title);
		}
	}
	
	public void setColor(BarColor color) {
		if (isProgressing) {
			runAfterProgressing.add(new Runnable() {
				public void run() {
					bar.setColor(color);
				}
			});
		}
		else {
			bar.setColor(color);
		}
	}
	
	public void setProgress(double progress) {
		// Make other bar changes wait until this completes
		if (isProgressing) {
			runAfterProgressing.add(new Runnable() {
				public void run() {
					bar.setProgress(progress);
				}
			});
		}
		else {
			bar.setProgress(progress);
			isProgressing = true;
			new BukkitRunnable() {
				public void run() {
					if (bar.getProgress() == 1) {
						setVisible(false);
						topic = "";
						bar.setProgress(0);
					}
					isProgressing = false; // Must be before so there's no array comodification
					if (runAfterProgressing.size() > 0) {
						for (Runnable r : runAfterProgressing) {
							r.run();
						}
						runAfterProgressing.clear();
					}
				}
			}.runTaskLater(NeoCore.inst(), 20L);
		}
	}
	
	public void setVisible(boolean visible) {
		if (!enabled && visible) return; // Don't make bar visible if not enabled
		bar.setVisible(visible);
	}
	
	public void setStyle(BarStyle style) {
		if (isProgressing) {
			runAfterProgressing.add(new Runnable() {
				public void run() {
					bar.setStyle(style);
				}
			});
		}
		else {
			bar.setStyle(style);
		}
	}
	
	public void setTopic(String topic) {
		CoreBar cb = this;
		if (isProgressing) {
			runAfterProgressing.add(new Runnable() {
				public void run() {
					cb.topic = topic;
				}
			});
		}
		else {
			this.topic = topic;
		}
	}
	
	public BarColor getColor() {
		return bar.getColor();
	}
	
	public BarStyle getStyle() {
		return bar.getStyle();
	}
	
	public double getProgress() {
		return bar.getProgress();
	}
	
	public boolean isVisible() {
		return bar.isVisible();
	}
	
	public String getTitle() {
		return bar.getTitle();
	}
	
	public String getTopic() {
		return topic;
	}
	
	public boolean toggleEnabled() {
		setEnabled(!enabled);
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		if (!enabled && bar.isVisible()) {
			bar.setVisible(false);
			topic = "";
		}
	}
	
	public boolean isEnabled() {
		return enabled;
	}
}

package me.neoblade298.neoquests.navigation;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import me.Neoblade298.NeoProfessions.Utilities.Util;
import me.neoblade298.neocore.exceptions.NeoIOException;
import me.neoblade298.neoquests.NeoQuests;
import me.neoblade298.neoquests.conditions.Condition;
import me.neoblade298.neoquests.conditions.ConditionManager;

public class Pathway {
	private String key, startDisplay, endDisplay, fileLocation;
	private World w;
	private LinkedList<PathwayPoint> points = new LinkedList<PathwayPoint>();
	private ArrayList<Condition> conditions;
	
	private static final int BLOCKS_PER_PARTICLE = 2;
	private static final int PARTICLES_PER_POINT = 20;
	private static final double PARTICLE_OFFSET = 0.1;
	private static final int PARTICLE_SPEED = 0;
	private static final int END_RADIUS_SQ = 100;
	
	private static final double DISTANCE_SHOWABLE = 5000;
	private static final DustOptions PARTICLE_DATA = new DustOptions(Color.RED, 1.0F);
	
	public Pathway() {}
	
	public Pathway(ConfigurationSection cfg, File file) throws NeoIOException {
		key = cfg.getName().toUpperCase();
		startDisplay = cfg.getString("start");
		endDisplay = cfg.getString("end");
		fileLocation = file.getPath() + "/" + file.getName();
		this.w = Bukkit.getWorld(cfg.getString("world", "Argyll"));
		this.conditions = ConditionManager.parseConditions(cfg.getStringList("conditions"));
		parsePoints(cfg.getStringList("points"));
	}
	
	private void parsePoints(List<String> list) throws NeoIOException {
		if (list.size() <= 1) {
			throw new NeoIOException("Pathway " + this.key + " has <= 1 points, invalid!");
		}
		
		// Redo to parse lin
		for (String line : list) {
			String args[] = line.split(" ");
			double x = Double.parseDouble(args[0]);
			double y = Double.parseDouble(args[1]);
			double z = Double.parseDouble(args[2]);
		}
	}
	
	public PathwayInstance start(Player p) {
		Condition c = ConditionManager.getBlockingCondition(p, conditions);
		if (c != null) {
			Util.sendMessage(p, "�cCould not start navigation from �6" + startDisplay + " to �6" + endDisplay + "�c, " + c.getExplanation(p));
			return null;
		}

		PathwayInstance pwi = new PathwayInstance(p, this);
		BukkitTask task = new BukkitRunnable() {
			public void run() {
				if (p == null) {
					this.cancel();
					return;
				}
				
				// Check if in different world
				if (!p.getWorld().equals(w)) {
					pwi.cancel("no longer in same world.");
					return;
				}
				
				// Check if location reached
				if (p.getLocation().distanceSquared(getEndLocation()) <= END_RADIUS_SQ) {
					pwi.stop();
					return;
				}
				
				show(p);
			}
		}.runTaskTimer(NeoQuests.inst(), 0L, 20L);

		pwi.setTask(task);
		Util.sendMessage(p, "�7Started navigation from �6" + startDisplay + " to �6" + endDisplay + "�7!");
		return pwi;
	}
	
	public void show(Player p) {
		showLines(p, this.points);
	}
	
	public static void showLines(Player p, LinkedList<PathwayPoint> points) {
		ListIterator<PathwayPoint> iter = points.listIterator();
		PathwayPoint l1 = null;
		PathwayPoint l2 = iter.next();
		while (iter.hasNext()) {
			l1 = l2;
			l2 = iter.next();
			l1.spawnParticle(p);
			if (l1.getLocation().distanceSquared(p.getLocation()) < DISTANCE_SHOWABLE) {
				drawLine(p, l1, l2);
			}
			
		}
	}
	
	private static void drawLine(Player p, PathwayPoint l1, PathwayPoint l2) {
		Location start = l1.getLocation();
		Location end = l2.getLocation();
	    
		Vector v = end.subtract(start).toVector();
		int iterations = (int) (v.length() / BLOCKS_PER_PARTICLE);
		for (int i = BLOCKS_PER_PARTICLE; i < iterations; i++) {
		    v.normalize();
		    v.multiply(i * 2);
		    start.add(v);
		    p.spawnParticle(Particle.REDSTONE, start, PARTICLES_PER_POINT, PARTICLE_OFFSET, PARTICLE_OFFSET, PARTICLE_OFFSET, PARTICLE_SPEED, PARTICLE_DATA);
			start.subtract(v);
		}
	}
	
	public Location getEndLocation() {
		return points.get(points.size() - 1).getLocation();
	}
	
	public String getFileLocation() {
		return fileLocation;
	}
	
	public String getStartDisplay() {
		return startDisplay;
	}
	
	public String getEndDisplay() {
		return endDisplay;
	}
}
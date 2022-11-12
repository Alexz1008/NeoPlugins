/*
 * Decompiled with CFR 0.145.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.entity.LivingEntity
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.inventory.CraftItemEvent
 *  org.bukkit.event.inventory.PrepareItemCraftEvent
 *  org.bukkit.event.player.PlayerItemConsumeEvent
 *  org.bukkit.inventory.CraftingInventory
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.Recipe
 *  org.bukkit.inventory.ShapelessRecipe
 *  org.bukkit.potion.PotionEffect
 *  org.bukkit.potion.PotionEffectType
 */
package net.aegistudio.mpp.palette;

import java.awt.Color;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.potion.PotionEffectType;

public class PaintBucketListener
implements Listener {
    private final PaletteManager palette;
    PotionEffectType[] rewards = new PotionEffectType[]{PotionEffectType.BLINDNESS, PotionEffectType.CONFUSION, PotionEffectType.HARM, PotionEffectType.HUNGER, PotionEffectType.POISON, PotionEffectType.WEAKNESS};

    public PaintBucketListener(PaletteManager palette) {
        this.palette = palette;
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onCraftPaintBucket(PrepareItemCraftEvent e) {
    	try {
	        if (e.getRecipe() instanceof ShapelessRecipe) {
	            ItemStack result = e.getInventory().getResult();
	            if (result.getType() != Material.MILK_BUCKET) {
	                return;
	            }
	            int sumCyan = 0;
	            int sumMagenta = 0;
	            int sumYellow = 0;
	            for (ItemStack item : e.getInventory().getMatrix()) {
	                Color color;
	                if (item == null || item.getType() == Material.AIR) continue;
	                if (item.getType() == Material.MILK_BUCKET) {
	                    color = this.palette.paintBucket.getColor(item);
	                    if (color == null) {
	                        continue;
	                    }
	                } else {
	                    if (item.getType() != Material.POTION) {
	                        return;
	                    }
	                    color = this.palette.dye.getColor(item);
	                }
	                sumCyan += color.getRed();
	                sumMagenta += color.getGreen();
	                sumYellow += color.getBlue();
	            }
	            int red = sumCyan > 255 ? 255 : sumCyan;
	            int green = sumMagenta > 255 ? 255 : sumMagenta;
	            int blue = sumYellow > 255 ? 255 : sumYellow;
	            PaletteManager.setColor(result, new Color(red, green, blue));
	            e.getInventory().setResult(result);
	        }
    	} catch (Exception ex) {
            ;
        }
    }

    @EventHandler
    public void postCraft(CraftItemEvent e) {
        if (e.isCancelled()) {
            return;
        }
        if (e.getRecipe() instanceof ShapelessRecipe) {
            ItemStack result = e.getInventory().getResult();
            if (result.getType() != Material.MILK_BUCKET) {
                return;
            }
            ItemStack[] matrix = e.getInventory().getMatrix();
            for (int i = 0; i < matrix.length; ++i) {
                ItemStack item = matrix[i];
                if (item == null || item.getType() == Material.AIR) continue;
                if (item.getType() != Material.MILK_BUCKET && item.getType() != Material.POTION) {
                    return;
                }
                matrix[i] = null;
            }
            e.getInventory().setMatrix(matrix);
            e.getInventory().setResult(result);
        }
    }

    @EventHandler
    public void isPaintBucketTasty(PlayerItemConsumeEvent e) {
        if (e.getItem().getType() != Material.MILK_BUCKET) {
            return;
        }
        if (this.palette.paintBucket.getColor(e.getItem()) == null) {
            return;
        }
        this.rewards[(int)((double)this.rewards.length * Math.random())].createEffect(2000, 1).apply((LivingEntity)e.getPlayer());
        e.getPlayer().sendMessage(this.palette.paintBucket.drinkPaintBucket);
        ItemStack item = new ItemStack(Material.BUCKET, 1);
        e.getPlayer().setItemInHand(item);
        e.setCancelled(true);
    }
}


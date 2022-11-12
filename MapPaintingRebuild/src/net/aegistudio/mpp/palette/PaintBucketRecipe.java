/*
 * Decompiled with CFR 0.145.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.Server
 *  org.bukkit.configuration.ConfigurationSection
 *  org.bukkit.enchantments.Enchantment
 *  org.bukkit.event.Listener
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.Recipe
 *  org.bukkit.inventory.ShapelessRecipe
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.plugin.PluginManager
 */
package net.aegistudio.mpp.palette;

import java.awt.Color;

import net.aegistudio.mpp.MapPainting;
import net.aegistudio.mpp.Module;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class PaintBucketRecipe
implements Module {
    public static final String FILL_BUCKET = "fillBucket";
    public String fillBucket = "@palette.fillBucket";
    public static final String DRINK_PAINT_BUCKET = "drinkPaintBucket";
    public String drinkPaintBucket = "@palette.drinkPaintBucket";
    public MapPainting painting;

    public Color getColor(ItemStack fillBucket) {
        if (fillBucket.getType() != Material.MILK_BUCKET) {
            return null;
        }
        return PaletteManager.getItemColor(fillBucket);
    }

    public void setColor(ItemStack fillBucket, Color color) {
        if (fillBucket.getType() != Material.MILK_BUCKET) {
            return;
        }
        PaletteManager.setColor(fillBucket, color);
    }

    @Override
    public void load(MapPainting painting, ConfigurationSection section) throws Exception {
        this.painting = painting;
        this.fillBucket = painting.getLocale(FILL_BUCKET, this.fillBucket, section);
        this.drinkPaintBucket = painting.getLocale(DRINK_PAINT_BUCKET, this.drinkPaintBucket, section);
        ItemStack paintBucket = new ItemStack(Material.MILK_BUCKET, 1);
        ItemMeta meta = paintBucket.getItemMeta();
        meta.setDisplayName(this.fillBucket);
        meta.addEnchant(Enchantment.DURABILITY,0, false);
        paintBucket.setItemMeta(meta);
        NamespacedKey recipe_buckets = new NamespacedKey(painting, "paintbuckets");
        ShapelessRecipe shapeless = new ShapelessRecipe(recipe_buckets, paintBucket);
        shapeless.addIngredient(1, Material.POTION, -1);
        shapeless.addIngredient(Material.MILK_BUCKET);
        painting.getServer().addRecipe((Recipe)shapeless);
        PaintBucketListener listener = new PaintBucketListener(painting.m_paletteManager);
        painting.getServer().getPluginManager().registerEvents((Listener)listener, (Plugin)painting);
    }

    @Override
    public void save(MapPainting painting, ConfigurationSection section) throws Exception {
    }
}


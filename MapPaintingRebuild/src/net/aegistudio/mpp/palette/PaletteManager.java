package net.aegistudio.mpp.palette;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.aegistudio.mpp.MapPainting;
import net.aegistudio.mpp.Module;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;

public class PaletteManager
implements Module {
    public static final String IDENTIFIER = "identifier";
    public String identifier = "@palette.identifier";
    public static final String CYAN = "cyan";
    public String cyan = "@palette.cyan";
    public static final String MAGENTA = "magenta";
    public String magenta = "@palette.magenta";
    public static final String YELLOW = "yellow";
    public String yellow = "@palette.yellow";
    public NaivePigmentRecipe dye;
    public PaintBucketRecipe paintBucket;

    public Color getColor(ItemStack item) {
        boolean hasIdentifier = false;
        int cyan = 0;
        int magenta = 0;
        int yellow = 0;
        if (item.getItemMeta().hasLore()) {
            for (String lore : item.getItemMeta().getLore()) {
                if (lore.equals(this.identifier)) {
                    hasIdentifier = true;
                }
                if (lore.startsWith(this.cyan)) {
                    cyan = Integer.parseInt(lore.substring(this.cyan.length()));
                }
                if (lore.startsWith(this.magenta)) {
                    magenta = Integer.parseInt(lore.substring(this.magenta.length()));
                }
                if (!lore.startsWith(this.yellow)) continue;
                yellow = Integer.parseInt(lore.substring(this.yellow.length()));
            }
        }
        return hasIdentifier ? new Color(cyan, magenta, yellow) : null;
    }

    public static Color getItemColor(ItemStack item) {
        if (!item.hasItemMeta() || !item.getItemMeta().hasLore()) {
            return null;
        }
        List<String> lore = item.getItemMeta().getLore();
        int red = 0;
        int green = 0;
        int blue = 0;
        for (String s : lore) {
            if (s.contains("Red")) {
                red = Integer.parseInt(ChatColor.stripColor(s).split(": ")[1]);
                continue;
            }
            if (s.contains("Green")) {
                green = Integer.parseInt(ChatColor.stripColor(s).split(": ")[1]);
                continue;
            }
            if (!s.contains("Blue")) continue;
            blue = Integer.parseInt(ChatColor.stripColor(s).split(": ")[1]);
        }
        return new Color(red, green, blue);
    }

    public static ItemStack getColorItem(Color awtColor) {
        ItemStack item = new ItemStack(Material.POTION);
        PaletteManager.setColor(item, awtColor);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        meta.setDisplayName("\u00a7f\u00a7nDye");
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        meta.setColor(org.bukkit.Color.fromRGB(awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue()));
        item.setItemMeta(meta);
//        net.minecraft.server.v1_15_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy((ItemStack)item);
//        NBTTagCompound compound = nmsItem.hasTag() ? nmsItem.getTag() : new NBTTagCompound();
//        compound.set("CustomPotionColor", NBTTagInt.a(org.bukkit.Color.fromRGB((int)color.getRed(), (int)color.getGreen(), (int)color.getBlue()).asRGB()));
//        nmsItem.setTag(compound);
//        return CraftItemStack.asBukkitCopy((net.minecraft.server.v1_15_R1.ItemStack)nmsItem);
        return item;
    }

    public static ItemStack getColorBucket(Color color) {
        ItemStack item = new ItemStack(Material.MILK_BUCKET);
        PaletteManager.setColor(item, color);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("\u00a7f\u00a7nPaint Bucket");
        item.setItemMeta(meta);
        return item;
    }

    public static void setColor(ItemStack item, Color color) {
        ItemMeta meta = item.getItemMeta();
        ArrayList<String> newLore = new ArrayList<String>();
        meta.setDisplayName("\u00a7f\u00a7nDye");
        newLore.add("§7\u00a7lRed: \u00a7f" + color.getRed());
        newLore.add("\u00a7a\u00a7lGreen: \u00a7f" + color.getGreen());
        newLore.add("\u00a79\u00a7lBlue: \u00a7f" + color.getBlue());
        meta.setLore(newLore);
        item.setItemMeta(meta);
    }

    @Override
    public void load(MapPainting painting, ConfigurationSection section) throws Exception {
        this.identifier = painting.getLocale(IDENTIFIER, this.identifier, section);
        this.cyan = painting.getLocale(CYAN, this.cyan, section);
        this.magenta = painting.getLocale(MAGENTA, this.magenta, section);
        this.yellow = painting.getLocale(YELLOW, this.yellow, section);
        this.dye = new NaivePigmentRecipe();
        this.dye.load(painting, section);
        this.paintBucket = new PaintBucketRecipe();
        this.paintBucket.load(painting, section);
    }

    @Override
    public void save(MapPainting painting, ConfigurationSection section) {
    }
}


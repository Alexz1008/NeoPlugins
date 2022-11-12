package net.aegistudio.mpp.palette;

import java.awt.Color;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class NaivePigmentListener implements Listener {
    private final PaletteManager palette;

    public NaivePigmentListener(PaletteManager palette) {
        this.palette = palette;
    }

    @EventHandler
    public void onCraftPigment(PrepareItemCraftEvent e) {
        if (e.getRecipe() instanceof ShapelessRecipe) {
            // Check if it is a naive pigment recipe.
            ItemStack result = e.getInventory().getResult();
            if (result.getType() != Material.POTION) return;

            int sumCyan = 0, sumMagenta = 0, sumYellow = 0;
            int inspected = 0;
            boolean blend = false;

            for (ItemStack item : e.getInventory().getMatrix())
                if (item != null) if (item.getType() != Material.AIR)
                    if (item.getType() == Material.WATER_BUCKET) blend = true;
                    else if (item.getType() != Material.POTION) return;
                    else {
                        Color color = this.palette.dye.getColor(item);
                        sumCyan += color.getRed();
                        sumMagenta += color.getGreen();
                        sumYellow += color.getBlue();
                        inspected += 1;
                    }
            int red, green, blue;

            if (blend) {
                red = sumCyan > 255 ? 255 : sumCyan;
                green = sumMagenta > 255 ? 255 : sumMagenta;
                blue = sumYellow > 255 ? 255 : sumYellow;
            } else {
                red = (sumCyan / inspected) > 255 ? 255 : (sumCyan / inspected);
                green = (sumMagenta / inspected) > 255 ? 255 : (sumMagenta / inspected);
                blue = (sumYellow / inspected) > 255 ? 255 : (sumYellow / inspected);
            }
            this.palette.dye.setColor(result, new Color(red, green, blue));
//            net.minecraft.server.v1_13_R2.ItemStack nmsItem = CraftItemStack.asNMSCopy((ItemStack)result);
//            NBTTagCompound compound = nmsItem.hasTag() ? nmsItem.getTag() : new NBTTagCompound();
//            compound.set("CustomPotionColor", (NBTBase)new NBTTagInt(org.bukkit.Color.fromRGB(red, green, blue).asRGB()));
//            nmsItem.setTag(compound);
//            result = CraftItemStack.asBukkitCopy((net.minecraft.server.v1_13_R2.ItemStack)nmsItem);
            result.setAmount(1);

            PotionMeta potionMeta = (PotionMeta) result.getItemMeta();
            potionMeta.setColor(org.bukkit.Color.fromRGB(red, green, blue));
            potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 0, 0), false);
            result.setItemMeta(potionMeta);

            ItemMeta itemMeta = result.getItemMeta();
            itemMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
            result.setItemMeta(itemMeta);

            e.getInventory().setResult(result);
        }
    }
}
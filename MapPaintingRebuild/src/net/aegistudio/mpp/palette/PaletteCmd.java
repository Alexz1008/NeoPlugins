/*
 * Decompiled with CFR 0.145.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.PlayerInventory
 */
package net.aegistudio.mpp.palette;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.awt.*;

public class PaletteCmd
implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("palette.command")) {
            sender.sendMessage("§7You don't have permission to execute this command.");
            return true;
        }
        if (args.length < 4) {
            sender.sendMessage("§7Usage: /palette <player> <red> <green> <blue> <optional: amount>");
            return true;
        }
        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            sender.sendMessage("§7This player is offline.");
            return true;
        }
        int red;
        int green;
        int blue;
        int amount = 1;
        try {
            red = Integer.parseInt(args[1]);
            green = Integer.parseInt(args[2]);
            blue = Integer.parseInt(args[3]);
        }
        catch (Exception e) {
            sender.sendMessage("§7Invalid number.");
            return true;
        }
        if (args.length >= 5) {
            try {
                amount = Integer.parseInt(args[4]);
            }
            catch (Exception e) {
                sender.sendMessage("§7Invalid number.");
                return true;
            }
        }
        if (!(this.isBetween(red, 0, 255) && this.isBetween(green, 0, 255) && this.isBetween(blue, 0, 255))) {
            sender.sendMessage("§7RGB values must be between 0 to 255.");
            return true;
        }
        if (amount <= 0) {
            sender.sendMessage("§7The amount must be bigger than 1.");
            return true;
        }
        for (int i = 0; i < amount; ++i) {
            player.getInventory().addItem(PaletteManager.getColorItem(new Color(red, green, blue)));
        }
        sender.sendMessage("\u00a7aSuccessfully gave \u00a7e" + player.getName() + "\u00a7a the color.");
        return true;
    }

    private boolean isBetween(int num, int min, int max) {
        return num >= min && num <= max;
    }
}


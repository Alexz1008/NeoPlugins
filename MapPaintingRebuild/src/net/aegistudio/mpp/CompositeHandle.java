/*
 * Decompiled with CFR 0.145.
 * 
 * Could not load the following classes:
 *  org.bukkit.ChatColor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.configuration.ConfigurationSection
 */
package net.aegistudio.mpp;

import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

public class CompositeHandle extends ActualHandle {
    public final ArrayList<String> name = new ArrayList<String>();
    public final ArrayList<CommandHandle> subcommand = new ArrayList<CommandHandle>();
    public static final String DESCRIPTION = "description";
    public static final String SUBCOMMAND = "subcommand";
    
    // lists all the commands that are declared for the plugin
    public void listCommand(MapPainting plugin, String prefix, CommandSender sender, int page) {
        
    	sender.sendMessage(plugin.listing.replace("$prefix", prefix));
        
        int beginIndex = (page - 1) * plugin.commandsPerPage;
        if (beginIndex >= this.subcommand.size() || beginIndex < 0) {
            sender.sendMessage(plugin.lastPage.replace("$prefix", prefix));
        } else {
            int endIndex = Math.min(this.name.size(), beginIndex + plugin.commandsPerPage);
            for (int i = beginIndex; i < endIndex; ++i) {
                StringBuilder builder = new StringBuilder();
                builder.append((Object)ChatColor.YELLOW);
                builder.append(prefix);
                builder.append(' ');
                builder.append((Object)ChatColor.BOLD);
                builder.append(this.name.get(i));
                builder.append((Object)ChatColor.RESET);
                builder.append(": ");
                builder.append(this.subcommand.get(i).description());
                sender.sendMessage(new String(builder));
            }
            if (endIndex == this.name.size()) {
                sender.sendMessage(plugin.lastPage.replace("$prefix", prefix));
            } else {
                sender.sendMessage(plugin.nextPage.replace("$prefix", prefix).replace("$nextPage", Integer.toString(page + 1)));
            }
        }
        
    	/* MLMC STUPIDITY
        sender.sendMessage("§7Type §9/tutorial§7 and visit the painting tutorial for a full guide!");
        sender.sendMessage("§7- §c/paint create §7[painting name] [size 1-128] <BG color> §7(§e1000g§7)");
        sender.sendMessage("§cRecommended size is 64!");
        sender.sendMessage("§7- §c/paint clone §7[painting you want to clone] <quantity 1-64> (§e250g each§7)");
        sender.sendMessage("§7- §c/paint copy §7[name for new painting] [name of painting to copy] (§e500g§7)");
        sender.sendMessage("§7- §c/paint buy §7[color] §7(§e250g§7)");
        sender.sendMessage("§7- §c/paint buy §7[red 0-255] [green 0-255] [blue 0-255] §7(§e1000g§7)");
        sender.sendMessage("§7- §c/paint delete §7[painting name]");
        sender.sendMessage("§7- §c/paint list §7<keyword>");
        sender.sendMessage("§7- §c/paint undo");
        sender.sendMessage("§7- §c/paint redo");
        sender.sendMessage("§7- §c/paint rename §7[old name] [new name] (§e100g§7)");
        */
        
    }

    @Override
    public boolean handle(MapPainting painting, String prefix, CommandSender sender, String[] arguments) {
        if (arguments.length == 0) {
            this.listCommand(painting, prefix, sender, 1);
            return true;
        }
        CommandHandle handle = null;
        for (int i = 0; i < this.name.size(); ++i) {
            if (!this.name.get(i).equalsIgnoreCase(arguments[0])) continue;
            handle = this.subcommand.get(i);
            break;
        }
        if (handle == null) {
            try {
                int page = Integer.parseInt(arguments[0]);
                this.listCommand(painting, prefix, sender, page);
                return true;
            }
            catch (Throwable t) {
                return false;
            }
        }
        String[] passUnder = new String[arguments.length - 1];
        System.arraycopy(arguments, 1, passUnder, 0, arguments.length - 1);
        return handle.handle(painting, prefix.concat(" ").concat(arguments[0]), sender, passUnder);
    }

    @Override
    public void load(MapPainting painting, ConfigurationSection section) throws Exception {
        super.load(painting, section);
        if (!section.contains(SUBCOMMAND)) {
            section.createSection(SUBCOMMAND);
        }
        ConfigurationSection subcommand = section.getConfigurationSection(SUBCOMMAND);
        for (int i = 0; i < this.name.size(); ++i) {
            if (!subcommand.contains(this.name.get(i))) {
                subcommand.createSection(this.name.get(i));
            }
            this.subcommand.get(i).load(painting, subcommand.getConfigurationSection(this.name.get(i)));
        }
    }

    public void add(String name, CommandHandle command) {
        this.name.add(name);
        this.subcommand.add(command);
    }

    @Override
    public void save(MapPainting painting, ConfigurationSection section) throws Exception {
        super.save(painting, section);
        for (CommandHandle handle : this.subcommand) {
            handle.save(painting, section);
        }
    }
}


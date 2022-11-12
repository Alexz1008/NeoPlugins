/*
 * Decompiled with CFR 0.145.
 * 
 * Could not load the following classes:
 *  org.bukkit.Server
 *  org.bukkit.command.CommandSender
 *  org.bukkit.permissions.Permission
 *  org.bukkit.permissions.PermissionAttachment
 *  org.bukkit.permissions.PermissionAttachmentInfo
 *  org.bukkit.plugin.Plugin
 */
package net.aegistudio.mpp.recipe;

import java.util.Set;
import java.util.UUID;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

public class SudoCommandSender
implements CommandSender {
    public final CommandSender sender;

    @Override
    public Spigot spigot() {
        return null;
    }

    public SudoCommandSender(CommandSender wrapped) {
        this.sender = wrapped;
    }

    public PermissionAttachment addAttachment(Plugin arg0) {
        return this.addAttachment(arg0);
    }

    public PermissionAttachment addAttachment(Plugin arg0, int arg1) {
        return this.addAttachment(arg0, arg1);
    }

    public PermissionAttachment addAttachment(Plugin arg0, String arg1, boolean arg2) {
        return this.addAttachment(arg0, arg1, arg2);
    }

    public PermissionAttachment addAttachment(Plugin arg0, String arg1, boolean arg2, int arg3) {
        return this.addAttachment(arg0, arg1, arg2, arg3);
    }

    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return this.sender.getEffectivePermissions();
    }

    public boolean hasPermission(String arg0) {
        return true;
    }

    public boolean hasPermission(Permission arg0) {
        return true;
    }

    public boolean isPermissionSet(String arg0) {
        return this.sender.isPermissionSet(arg0);
    }

    public boolean isPermissionSet(Permission arg0) {
        return this.sender.isPermissionSet(arg0);
    }

    public void recalculatePermissions() {
        this.sender.recalculatePermissions();
    }

    public void removeAttachment(PermissionAttachment arg0) {
        this.sender.removeAttachment(arg0);
    }

    public boolean isOp() {
        return this.sender.isOp();
    }

    public void setOp(boolean arg0) {
    }

    public String getName() {
        return this.sender.getName();
    }

    public Server getServer() {
        return this.sender.getServer();
    }

    public void sendMessage(String arg0) {
        this.sender.sendMessage(arg0);
    }

    public void sendMessage(String[] arg0) {
        this.sender.sendMessage(arg0);
    }

	@Override
	public void sendMessage(UUID arg0, String arg1) {
	}

	@Override
	public void sendMessage(UUID arg0, String[] arg1) {
	}
}


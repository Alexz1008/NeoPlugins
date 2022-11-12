/*
 * Decompiled with CFR 0.145.
 *
 * Could not load the following classes:
 *  com.palmergames.bukkit.towny.object.TownyPermission
 *  com.palmergames.bukkit.towny.object.TownyPermission$ActionType
 *  com.palmergames.bukkit.towny.utils.PlayerCacheUtil
 *  com.sk89q.worldguard.bukkit.WorldGuardPlugin
 *  org.bukkit.GameMode
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.World
 *  org.bukkit.block.Block
 *  org.bukkit.block.BlockFace
 *  org.bukkit.command.CommandSender
 *  org.bukkit.configuration.ConfigurationSection
 *  org.bukkit.enchantments.Enchantment
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.EntityType
 *  org.bukkit.entity.Item
 *  org.bukkit.entity.ItemFrame
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.block.Action
 *  org.bukkit.event.entity.ItemSpawnEvent
 *  org.bukkit.event.player.PlayerInteractEvent
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 */
package net.aegistudio.mpp.canvas;

import com.palmergames.bukkit.towny.event.executors.TownyActionEventExecutor;
//import com.palmergames.bukkit.towny.object.TownyPermission;
//import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import net.aegistudio.mpp.MapPainting;
import net.aegistudio.mpp.Module;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class CanvasScopeListener
        implements Module,
        Listener {
    public MapPainting plugin;
    public static final String IDENTIFIER = "identifier";
    public String identifier = "@canvas.identifier";
    public static final String ID_STRING = "mapIdString";
    public String mapIdString = "@canvas.mapIdString";
    public static final String RENAME_TITLE = "renameTitle";
    public String renameTitle = "@canvas.renameTitle";
    public static final String RENAME_LN3 = "renameLn3";
    public String renameLn3 = "@canvas.renameLn3";
    public static final String RENAME_LN4 = "renameLn4";
    public String renameLn4 = "@canvas.renameLn4";

    public CanvasScopeListener(MapPainting painting) {
        this.plugin = painting;
    }

    public void make(ItemStack item, MapCanvasRegistry registry) {
        item.setType(Material.PAINTING);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(registry.name);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(this.identifier);
        lore.add(this.mapIdString + registry.binding);
        meta.setLore(lore);
        meta.addEnchant(Enchantment.DURABILITY, 0, false);
        item.setItemMeta(meta);
    }

    public int parse(ItemStack item) {
        if (item == null)
            return -1;

        if (item.getType() != Material.PAINTING)
            return -1;

        List<String> lores = item.getItemMeta().getLore();
        if (lores == null)
            return -1;

        if (lores.size() < 2)
            return -1;

        String line = lores.get(0);
        char colorCode = line.charAt(1);
        ChatColor color = ChatColor.getByChar(colorCode);
        if(color != null && color.isFormat())
            line = "\u00A7r" + line;
 
        if (!line.equals(this.identifier))
            return -1;
        return Integer.parseInt(lores.get(1).substring(this.mapIdString.length()));
    }

    private boolean isAllowedToPlaceCanvas(Player player, Location location) {
    	
    	// deny by default
    	boolean result = true;
        
    	/*
    	try {
	    	// if player can't build per Towny perms, reject
	    	if (!PlayerCacheUtil.getCachePermission(player, location, Material.PAINTING, TownyPermission.ActionType.BUILD)) {
	    		player.sendMessage("§c[Paint]§7 Denied based on Towny build permission check.");
	    		return false;
	        }
    	} catch (Exception e) {		
    	}
    	
    	*/
        
        // if player can't build per WorldGuard, reject
        if (!canBuild(player, location)) {
        	player.sendMessage("§c[Paint]§7 Denied based on WorldGuard build permission check.");
        	return false;
        }
        
    	//if player has no permission to place paintings, reject
    	if (!player.hasPermission("mpp.place")) {
    		player.sendMessage("§c[Paint]§7 Denied based on missing mpp.place permission node.");
    		return false;
    	}
        
        return result;
        
        
        // ORIGINAL BADLY WORKING CODE
        // if (player.hasPermission("mpp.place")) return true;
        // if (!PlayerCacheUtil.getCachePermission(player, location, Material.PAINTING, TownyPermission.ActionType.BUILD)) {
        //    return false;
        // }
        // return canBuild(player, location);
        
    }

    public static boolean canBuild(Player p, Location l) {
        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        com.sk89q.worldedit.util.Location loc = BukkitAdapter.adapt(l);
        LocalPlayer pl = WorldGuardPlugin.inst().wrapPlayer(p);
        com.sk89q.worldedit.world.World world = pl.getWorld();
        if (!hasBypass(pl, world)) {
            return query.testState(loc, WorldGuardPlugin.inst().wrapPlayer(p), Flags.BUILD);
        } else {
            return true;
        }
    }


    public static boolean hasBypass(LocalPlayer p, World l) {
        return WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(p, l);
    }
    
    @EventHandler
    private void onDamage(EntityDamageByEntityEvent e) {
    	
    	
    	// IGNORE ALL DAMAGE NOT RELATED TO THE PLUGIN
    	
    	// ignore if damaged entity is not an item frame
        if (!e.getEntityType().equals(EntityType.ITEM_FRAME)) return;
        
        // ignore if damaged frame does not contain a filled map object
        ItemFrame itemFrame = (ItemFrame) e.getEntity();
        if (itemFrame.getItem().getType() != Material.FILLED_MAP) return;
    	
        // check if plugin is replacing the mapID
        int mapId = (int) ((MapMeta) itemFrame.getItem().getItemMeta()).getMapId();
        MapCanvasRegistry registry = JavaPlugin.getPlugin(MapPainting.class).m_canvasManager.idCanvasMap.get(mapId);
        if (registry == null) return;
    	
        
    	// manage player breakage
        if (e.getDamager() instanceof Player) {		
        	
        	Player p = ((Player)e.getDamager());
	        
	        // Worldedit check
	        if (!isAllowedToPlaceCanvas(p, e.getEntity().getLocation())) {
                e.setCancelled(true);
                return;
            }
	        
	        // Towny check
	        if (!TownyActionEventExecutor.canDestroy(p, itemFrame.getLocation(), Material.ITEM_FRAME)) {
                p.sendMessage("§c[Paint]§7 Denied based on Towny build permission check.");
	        	e.setCancelled(true);
	        	return;
	        }
	        
	        // remove the frame
	        e.getEntity().remove();
	        
	        // only give the damager the canvas if it existed in registry
	        plugin.m_canvasManager.give(p, registry, 1);
	        
	        return;
        
        }
        // Creeper explosions, arrows, etc
        else {
        	e.setCancelled(true);
        }
    }


    @SuppressWarnings("incomplete-switch")
	@EventHandler
    public void onItemUse(final PlayerInteractEvent event) {
    	
    	
    	Player p = event.getPlayer();
    	
    	// check if there is a painting in either hand
        
	    	int result = this.parse(event.getItem());
	        
	    	if (result < 0) { 
	        	//p.sendMessage("§7[DEBUG] FAIL - ITEM IS NOT A CANVAS ITEM (PARSE FAIL)");
	        	return; 
	        } // not a canvas item, do nothing immediately
	        
	        MapCanvasRegistry registry = this.plugin.m_canvasManager.idCanvasMap.get((int) result);
	        
	        if (registry == null || registry.removed()) {
	        	
	        	p.sendMessage("§c[Paint]§7 Painting " + result + " has been deleted, useless canvas(es) removed.");
	        	
	        	if (event.getHand() == EquipmentSlot.HAND) {
                		event.getPlayer().getInventory().setItemInMainHand(null);
                }
                
                if (event.getHand() == EquipmentSlot.OFF_HAND) {
                		event.getPlayer().getInventory().setItemInOffHand(null);
                }
	        	
	        	event.setCancelled(true);
	        	
	        	return; 
	        } // a deleted canvas item that is not a managed map

        // check it was a right click event
	        
	        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
	        	//p.sendMessage("§7[DEBUG] FAIL - ACTION WAS NOT A RIGHT CLICK");
	        	// event.setCancelled(true);
	        	// should still be able to break blocks wielding a painting
	        	return;
	        }
	        
	    // check if right click event retrieved an actual block
	        
            Block block = event.getClickedBlock();
            if (block == null) {
            	p.sendMessage("§c[Paint]§7 No surface was found to place the canvas on.");
            	event.setCancelled(true);
                return;
            }
            
        // get the location including face the player has clicked
            
            Location l2 = block.getLocation().clone();
            Location l3 = l2;
            
            switch (event.getBlockFace()) {
            	case NORTH: {
	            	l3 = l2.clone().add(0, 0, -1);
	                break;
	                }
                case SOUTH: {
	                l3 = l2.clone().add(0, 0, 1);
	                break;
	                }
                case EAST: {
	                l3 = l2.clone().add(1, 0, 0);
	                break;
	                }
                case WEST: {
	                l3 = l2.clone().add(-1, 0, 0);
	                break;
	                }
                case UP: {
                	l3 = l2.clone().add(0,1,0);
                	//p.sendMessage("§7[DEBUG] TOP OF BLOCK");
                	break;
                	}
                case DOWN: {
                	l3 = l2.clone().add(0,-1,0);
                	//p.sendMessage("§7[DEBUG] BOTTOM OF BLOCK");
                	break;
                	}
                	
            }
            
            // check location is valid
            if (l3 == null){
            	p.sendMessage("§c[Paint]§7 No surface was found to place the canvas on.");
            	event.setCancelled(true);
            	return;
            }
            
            // check player is allowed to place the item
            if (this.isAllowedToPlaceCanvas(p, l3) == false) {
            	p.sendMessage("§c[Paint]§7 You don't have permission to place this here.");
                event.setCancelled(true);
            	return;
            }
                    
            // PLACE THE FRAME IF EVERYTHING IS OK                    
            this.placeFrame(block.getLocation(), event.getBlockFace(), registry);
                
            // CHECK PLAYER HAND CREATING THE INTERACT EVENT AND REMOVE PAINTING PLACED FROM CORRECT HAND

                if (event.getHand() == EquipmentSlot.HAND) {
                	if (event.getPlayer().getGameMode() == GameMode.SURVIVAL) {
                        int newAmount = event.getPlayer().getInventory().getItemInMainHand().getAmount() - 1;
                        if (newAmount > 0) {
                            event.getPlayer().getInventory().getItemInMainHand().setAmount(newAmount);
                        } else {
                            event.getPlayer().getInventory().setItemInMainHand(null);
                        }
                    }
                }
                
                if (event.getHand() == EquipmentSlot.OFF_HAND) {
                	if (event.getPlayer().getGameMode() == GameMode.SURVIVAL) {
                        int newAmount = event.getPlayer().getInventory().getItemInOffHand().getAmount() - 1;
                        if (newAmount > 0) {
                            event.getPlayer().getInventory().getItemInOffHand().setAmount(newAmount);
                        } else {
                            event.getPlayer().getInventory().setItemInOffHand(null);
                        }
                    }
                }

    }
    

    public void placeFrame(Location blockLocation, BlockFace blockFace, MapCanvasRegistry registry) {
        ItemFrame frame = blockLocation.getWorld().spawn(blockLocation.add(blockFace.getModX(), blockFace.getModY(), blockFace.getModZ()), ItemFrame.class);
        frame.setFacingDirection(blockFace);
        frame.setVisible(false);
        ItemStack mapitem = new ItemStack(Material.FILLED_MAP, 1);
        MapMeta mapMeta = (MapMeta) mapitem.getItemMeta();
        mapMeta.setMapId(registry.binding);
        mapitem.setItemMeta(mapMeta);
        frame.setItem(mapitem);
    }

    @EventHandler
    public void onSpawnItem(ItemSpawnEvent e) {
        ItemStack item = e.getEntity().getItemStack();
        if (item.getType() != Material.FILLED_MAP) {
            return;
        }
        int mapid = (int) ((MapMeta) item.getItemMeta()).getMapId();
        MapCanvasRegistry registry = this.plugin.m_canvasManager.idCanvasMap.get(mapid);
        if (registry != null && !registry.removed()) {
            this.make(item, registry);
            e.getEntity().setItemStack(item);
            this.removeNearby(e.getEntity().getLocation(), mapid);
            registry.canvas.unplace(e.getEntity());
        } else if (this.plugin.m_canvasManager.pool.contains(mapid)) {
            e.getEntity().remove();
            this.removeNearby(e.getEntity().getLocation(), mapid);
        }
    }

    public void removeNearby(Location location, int mapid) {
        location.getWorld().getNearbyEntities(location, 0.5, 0.5, 0.5).forEach(entity -> {
            ItemStack mapitem;
            if (entity.getType() == EntityType.DROPPED_ITEM) {
                if (((Item) entity).getItemStack().getType() == Material.ITEM_FRAME) {
                    entity.remove();
                }
            } else if (entity.getType() == EntityType.ITEM_FRAME && (mapitem = ((ItemFrame) entity).getItem()).getType() == Material.FILLED_MAP && (int) ((MapMeta) mapitem.getItemMeta()).getMapId() == mapid) {
                entity.remove();
            }
        });
    }

    @Override
    public void load(MapPainting painting, ConfigurationSection section) throws Exception {
        this.identifier = painting.getLocale(IDENTIFIER, this.identifier, section);
        this.mapIdString = painting.getLocale(ID_STRING, this.mapIdString, section);
        this.renameTitle = painting.getLocale(RENAME_TITLE, this.renameTitle, section);
        this.renameLn3 = painting.getLocale(RENAME_LN3, this.renameLn3, section);
        this.renameLn4 = painting.getLocale(RENAME_LN4, this.renameLn4, section);
    }

    @Override
    public void save(MapPainting painting, ConfigurationSection section) throws Exception {
        section.set(IDENTIFIER, this.identifier);
        section.set(ID_STRING, this.mapIdString);
    }

}


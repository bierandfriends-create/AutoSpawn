package de.autospawn;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class AutoSpawn extends JavaPlugin implements Listener {

    private static final int RADIUS = 12;
    private static final int MIN_Y = -64;
    private static final int MAX_Y = 319;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    /* -----------------------------
       Spawn-Bereich prüfen
     ----------------------------- */
    private boolean isInProtectedSpawn(Location loc) {
        if (loc == null || loc.getWorld() == null) return false;

        String worldName = loc.getWorld().getName();
        Location center;

        if (worldName.equalsIgnoreCase("farmwelt")) {
            center = new Location(loc.getWorld(), 0, 119, 0);
        } else if (worldName.equalsIgnoreCase("resource_nether")) {
            center = new Location(loc.getWorld(), 0, 59, 0);
        } else {
            return false;
        }

        return Math.abs(loc.getBlockX() - center.getBlockX()) <= RADIUS
                && Math.abs(loc.getBlockZ() - center.getBlockZ()) <= RADIUS
                && loc.getBlockY() >= MIN_Y
                && loc.getBlockY() <= MAX_Y;
    }

    private boolean bypass(Player player) {
        return player.hasPermission("autospawn.bypass");
    }

    /* -----------------------------
       Blockieren: Bauen / Abbauen
     ----------------------------- */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (bypass(event.getPlayer())) return;
        if (isInProtectedSpawn(event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (bypass(event.getPlayer())) return;
        if (isInProtectedSpawn(event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    /* -----------------------------
       Container blockieren
     ----------------------------- */
    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (bypass(player)) return;

        Block block = event.getInventory().getLocation() != null
                ? event.getInventory().getLocation().getBlock()
                : null;

        if (block != null && isInProtectedSpawn(block.getLocation())) {
            event.setCancelled(true);
        }
    }

    /* -----------------------------
       Schaden blockieren
     ----------------------------- */
    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (bypass(player)) return;

        if (isInProtectedSpawn(player.getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPvP(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;

        if (bypass(attacker) || bypass(victim)) return;

        if (isInProtectedSpawn(victim.getLocation())
                || isInProtectedSpawn(attacker.getLocation())) {
            event.setCancelled(true);
        }
    }
}

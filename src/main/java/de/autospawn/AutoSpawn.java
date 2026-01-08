package de.autospawn;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;

public final class AutoSpawn extends JavaPlugin implements Listener {

    private static final int RADIUS = 25;
    private static final int MIN_Y = -64;
    private static final int MAX_Y = 319;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getConsoleSender().sendMessage("§a[AutoSpawn] Spawn-Schutz aktiv");
    }

    private boolean isInProtectedArea(Location loc) {
        if (loc == null || loc.getWorld() == null) return false;

        World w = loc.getWorld();
        String name = w.getName();

        Location center;

        if (name.equalsIgnoreCase("farmwelt")) {
            center = new Location(w, 0, 119, 0);
        } else if (name.equalsIgnoreCase("nether")) {
            center = new Location(w, 0, 59, 0);
        } else {
            return false;
        }

        return Math.abs(loc.getX() - center.getX()) <= RADIUS
                && Math.abs(loc.getZ() - center.getZ()) <= RADIUS
                && loc.getY() >= MIN_Y
                && loc.getY() <= MAX_Y;
    }

    private boolean bypass(org.bukkit.entity.Player p) {
        return p != null && p.hasPermission("autospawn.bypass");
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (isInProtectedArea(e.getBlock().getLocation()) && !bypass(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (isInProtectedArea(e.getBlock().getLocation()) && !bypass(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        if (!(e.getPlayer() instanceof org.bukkit.entity.Player p)) return;
        if (bypass(p)) return;

        InventoryHolder holder = e.getInventory().getHolder();
        if (holder == null) return;

        Location loc = null;

        if (holder instanceof org.bukkit.block.BlockState bs) {
            loc = bs.getLocation();
        }

        if (loc != null && isInProtectedArea(loc)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if (!isInProtectedArea(e.getEntity().getLocation())) return;

        if (e.getCause() == EntityDamageEvent.DamageCause.FALL
                || e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK
                || e.getCause() == EntityDamageEvent.DamageCause.PROJECTILE
                || e.getCause() == EntityDamageEvent.DamageCause.LAVA
                || e.getCause() == EntityDamageEvent.DamageCause.FIRE
                || e.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPvP(EntityDamageByEntityEvent e) {
        if (isInProtectedArea(e.getEntity().getLocation())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onExplosion(EntityExplodeEvent e) {
        if (isInProtectedArea(e.getLocation())) {
            e.blockList().clear();
            e.setCancelled(true);
        }
    }
}

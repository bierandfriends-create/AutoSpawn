package de.autospawn;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class AutoSpawn extends JavaPlugin implements Listener {

    private final String BYPASS = "autospawn.bypass";

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("AutoSpawn aktiviert");
    }

    // =====================
    // WARP COMMAND
    // =====================
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        if (args.length != 1) return true;

        String key = args[0].toLowerCase();
        if (!getConfig().contains("warps." + key)) return true;

        Location l = p.getLocation();
        getConfig().set("warps." + key + ".world", l.getWorld().getName());
        getConfig().set("warps." + key + ".x", l.getX());
        getConfig().set("warps." + key + ".y", l.getY());
        getConfig().set("warps." + key + ".z", l.getZ());
        getConfig().set("warps." + key + ".yaw", l.getYaw());
        getConfig().set("warps." + key + ".pitch", l.getPitch());
        saveConfig();

        p.sendMessage("§aWarp '" + key + "' gesetzt.");
        return true;
    }

    // =====================
    // SPAWN CHECK
    // =====================
    private boolean inSpawn(Location l) {
        if (!getConfig().getBoolean("spawn-protection.enabled")) return false;

        List<String> worlds = getConfig().getStringList("spawn-protection.worlds");
        if (!worlds.contains(l.getWorld().getName())) return false;

        int r = getConfig().getInt("spawn-protection.area.radius-xz");
        int minY = getConfig().getInt("spawn-protection.area.min-y");
        int maxY = getConfig().getInt("spawn-protection.area.max-y");

        return Math.abs(l.getX()) <= r &&
               Math.abs(l.getZ()) <= r &&
               l.getY() >= minY &&
               l.getY() <= maxY;
    }

    // =====================
    // BLOCK EVENTS
    // =====================
    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if (e.getPlayer().hasPermission(BYPASS)) return;
        if (getConfig().getBoolean("spawn-protection.block.break") &&
            inSpawn(e.getBlock().getLocation())) e.setCancelled(true);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if (e.getPlayer().hasPermission(BYPASS)) return;
        if (getConfig().getBoolean("spawn-protection.block.place") &&
            inSpawn(e.getBlock().getLocation())) e.setCancelled(true);
    }

    // =====================
    // INVENTORY
    // =====================
    @EventHandler
    public void onInventory(InventoryOpenEvent e) {
        if (!(e.getPlayer() instanceof Player p)) return;
        if (p.hasPermission(BYPASS)) return;
        if (getConfig().getBoolean("spawn-protection.inventory.open") &&
            inSpawn(p.getLocation())) e.setCancelled(true);
    }

    // =====================
    // DAMAGE
    // =====================
    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        if (!inSpawn(p.getLocation())) return;

        switch (e.getCause()) {
            case FALL -> { if (getConfig().getBoolean("spawn-protection.damage.fall")) e.setCancelled(true); }
            case FIRE, FIRE_TICK -> { if (getConfig().getBoolean("spawn-protection.damage.fire")) e.setCancelled(true); }
            case LAVA -> { if (getConfig().getBoolean("spawn-protection.damage.lava")) e.setCancelled(true); }
            case ENTITY_EXPLOSION, BLOCK_EXPLOSION -> {
                if (getConfig().getBoolean("spawn-protection.damage.explosion")) e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPvP(EntityDamageByEntityEvent e) {
        Entity v = e.getEntity();
        if (!(v instanceof Player p)) return;
        if (!getConfig().getBoolean("spawn-protection.combat.pvp")) return;
        if (inSpawn(p.getLocation())) e.setCancelled(true);
    }
}

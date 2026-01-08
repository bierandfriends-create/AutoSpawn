package de.autospawn;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class AutoSpawn extends JavaPlugin implements Listener {

    private static final int RADIUS = 12; // 25x25
    private static final int MIN_Y = -64;
    private static final int MAX_Y = 319;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("AutoSpawn aktiviert");
    }

    // ======================
    // JOIN → TELEPORT
    // ======================
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        World w = p.getWorld();

        if (w.getName().equalsIgnoreCase("farmwelt")) {
            teleportToWarp(p, "farmwelt");
        } else if (w.getName().equalsIgnoreCase("Nether")) {
            teleportToWarp(p, "nether");
        }
    }

    private void teleportToWarp(Player p, String warp) {
        if (!getConfig().contains("warps." + warp)) return;

        World w = Bukkit.getWorld(getConfig().getString("warps." + warp + ".world"));
        if (w == null) return;

        Location loc = new Location(
                w,
                getConfig().getDouble("warps." + warp + ".x"),
                getConfig().getDouble("warps." + warp + ".y"),
                getConfig().getDouble("warps." + warp + ".z"),
                (float) getConfig().getDouble("warps." + warp + ".yaw"),
                (float) getConfig().getDouble("warps." + warp + ".pitch")
        );

        p.teleport(loc);
    }

    // ======================
    // /SETWARP
    // ======================
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        if (args.length != 1) return false;

        String warp = args[0].toLowerCase();
        if (!warp.equals("farmwelt") && !warp.equals("nether")) return false;

        Location l = p.getLocation();
        getConfig().set("warps." + warp + ".world", l.getWorld().getName());
        getConfig().set("warps." + warp + ".x", l.getX());
        getConfig().set("warps." + warp + ".y", l.getY());
        getConfig().set("warps." + warp + ".z", l.getZ());
        getConfig().set("warps." + warp + ".yaw", l.getYaw());
        getConfig().set("warps." + warp + ".pitch", l.getPitch());

        saveConfig();
        p.sendMessage("§aWarp §e" + warp + " §agesetzt.");
        return true;
    }

    // ======================
    // SPAWN-SCHUTZ
    // ======================
    private boolean inSpawn(Location l) {
        return Math.abs(l.getX()) <= RADIUS &&
               Math.abs(l.getZ()) <= RADIUS &&
               l.getY() >= MIN_Y &&
               l.getY() <= MAX_Y;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if (e.getPlayer().hasPermission("autospawn.bypass")) return;
        if (inSpawn(e.getBlock().getLocation())) e.setCancelled(true);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if (e.getPlayer().hasPermission("autospawn.bypass")) return;
        if (inSpawn(e.getBlock().getLocation())) e.setCancelled(true);
    }

    @EventHandler
    public void onInventory(InventoryOpenEvent e) {
        if (!(e.getPlayer() instanceof Player p)) return;
        if (p.hasPermission("autospawn.bypass")) return;
        if (inSpawn(p.getLocation())) e.setCancelled(true);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player p && inSpawn(p.getLocation())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPvP(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player p && inSpawn(p.getLocation())) {
            e.setCancelled(true);
        }
    }
}

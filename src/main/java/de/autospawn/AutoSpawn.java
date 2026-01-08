package de.autospawn;

import com.fastasyncworldedit.bukkit.BukkitAdapter;
import com.fastasyncworldedit.core.FaweAPI;
import com.fastasyncworldedit.core.session.SessionKey;
import com.fastasyncworldedit.core.session.SessionManager;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.math.BlockVector3;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;

public class AutoSpawn extends JavaPlugin implements Listener {

    private static final int RADIUS = 12;
    private static final int MIN_Y = -64;
    private static final int MAX_Y = 319;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);

        Bukkit.getScheduler().runTaskLater(this, () -> {
            loadWorld("farmwelt");
            loadWorld("Nether");

            pasteSchematic("farmwelt", "SpawnFW", 0, 119, 0);
            pasteSchematic("Nether", "SpawnNE", 0, 59, 0);

        }, 20L * 15); // 15 Sekunden
    }

    /* ---------------- WORLD LOAD ---------------- */

    private void loadWorld(String name) {
        World world = Bukkit.getWorld(name);
        if (world == null) {
            Bukkit.createWorld(new WorldCreator(name));
        }
    }

    /* ---------------- SCHEMATIC PASTE ---------------- */

    private void pasteSchematic(String worldName, String schematic, int x, int y, int z) {
        try {
            File file = new File("plugins/FastAsyncWorldEdit/schematics/" + schematic + ".schem");
            if (!file.exists()) return;

            ClipboardReader reader = ClipboardFormats.findByFile(file).getReader(new FileInputStream(file));
            var clipboard = reader.read();

            World world = Bukkit.getWorld(worldName);
            if (world == null) return;

            EditSession session = WorldEdit.getInstance().newEditSession(new BukkitWorld(world));
            ClipboardHolder holder = new ClipboardHolder(clipboard);

            holder
                .createPaste(session)
                .to(BlockVector3.at(x, y, z))
                .ignoreAirBlocks(false)
                .build();

            session.flushSession();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ---------------- SPAWN CHECK ---------------- */

    private boolean inSpawn(Location loc) {
        if (loc == null || loc.getWorld() == null) return false;

        String w = loc.getWorld().getName();
        Location c;

        if (w.equalsIgnoreCase("farmwelt"))
            c = new Location(loc.getWorld(), 0, 119, 0);
        else if (w.equalsIgnoreCase("Nether"))
            c = new Location(loc.getWorld(), 0, 59, 0);
        else return false;

        return Math.abs(loc.getBlockX() - c.getBlockX()) <= RADIUS &&
               Math.abs(loc.getBlockZ() - c.getBlockZ()) <= RADIUS &&
               loc.getBlockY() >= MIN_Y &&
               loc.getBlockY() <= MAX_Y;
    }

    private boolean bypass(Player p) {
        return p.hasPermission("autospawn.bypass");
    }

    /* ---------------- PROTECTION ---------------- */

    @EventHandler public void place(BlockPlaceEvent e) {
        if (!bypass(e.getPlayer()) && inSpawn(e.getBlock().getLocation())) e.setCancelled(true);
    }

    @EventHandler public void breakB(BlockBreakEvent e) {
        if (!bypass(e.getPlayer()) && inSpawn(e.getBlock().getLocation())) e.setCancelled(true);
    }

    @EventHandler public void inv(InventoryOpenEvent e) {
        if (e.getPlayer() instanceof Player p &&
            !bypass(p) &&
            e.getInventory().getLocation() != null &&
            inSpawn(e.getInventory().getLocation())) e.setCancelled(true);
    }

    @EventHandler public void damage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player p &&
            !bypass(p) &&
            inSpawn(p.getLocation())) e.setCancelled(true);
    }

    @EventHandler public void pvp(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player v &&
            e.getDamager() instanceof Player a &&
            !bypass(v) && !bypass(a) &&
            (inSpawn(v.getLocation()) || inSpawn(a.getLocation())))
            e.setCancelled(true);
    }
}

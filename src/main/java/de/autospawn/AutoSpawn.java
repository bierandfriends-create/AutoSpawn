package de.autospawn;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class AutoSpawn extends JavaPlugin {

    @Override
    public void onEnable() {
        Bukkit.getConsoleSender().sendMessage("§a[AutoSpawn] Plugin gestartet, warte 15 Sekunden...");

        new BukkitRunnable() {
            @Override
            public void run() {
                checkWorld("farmwelt");
                checkWorld("Nether");
            }
        }.runTaskLater(this, 20L * 15); // 15 Sekunden
    }

    private void checkWorld(String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            Bukkit.getConsoleSender().sendMessage("§c[AutoSpawn] Welt NICHT geladen: " + worldName);
        } else {
            Bukkit.getConsoleSender().sendMessage("§a[AutoSpawn] Welt geladen: " + worldName);
        }
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage("§c[AutoSpawn] Plugin deaktiviert.");
    }
}

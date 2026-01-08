package de.autospawn;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class AutoSpawn extends JavaPlugin {

    @Override
    public void onEnable() {
        Bukkit.getConsoleSender().sendMessage("§a[AutoSpawn] Plugin wurde erfolgreich geladen!");
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage("§c[AutoSpawn] Plugin wurde deaktiviert!");
    }
}

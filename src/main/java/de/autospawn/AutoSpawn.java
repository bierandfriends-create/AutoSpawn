import org.bukkit.*;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;

public class AutoSpawn extends JavaPlugin implements Listener {

    private static final int RADIUS = 25;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("AutoSpawn aktiviert");
    }

    private boolean inProtectedSpawn(Location l) {
        if (l == null || l.getWorld() == null) return false;

        String w = l.getWorld().getName();
        if (!w.equals("farmwelt") && !w.equals("Nether")) return false;

        int x = l.getBlockX();
        int z = l.getBlockZ();

        return Math.abs(x) <= RADIUS && Math.abs(z) <= RADIUS;
    }

    /* BLOCKS */
    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if (e.getPlayer().hasPermission("autospawn.bypass")) return;
        if (inProtectedSpawn(e.getBlock().getLocation())) e.setCancelled(true);
    }

    /* DAMAGE */
    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        if (p.hasPermission("autospawn.bypass")) return;
        if (inProtectedSpawn(p.getLocation())) e.setCancelled(true);
    }

    /* PVP */
    @EventHandler
    public void onPvp(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        if (inProtectedSpawn(e.getEntity().getLocation())) e.setCancelled(true);
    }
}

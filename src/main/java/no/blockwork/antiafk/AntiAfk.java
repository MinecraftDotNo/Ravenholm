package no.blockwork.antiafk;

import no.minecraft.BasePlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AntiAfk implements BasePlugin, Listener, Runnable {
    private final JavaPlugin plugin;

    private final Map<Player, Long> ts;
    private final Map<Player, List<Integer>> locations;

    private int checkTaskId;

    public AntiAfk(JavaPlugin pluginInstance) {
        plugin = pluginInstance;

        ts = new HashMap<>();
        locations = new HashMap<>();
    }

    @Override
    public final void onEnable() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        checkTaskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, 200, 200);
    }

    @Override
    public final void onDisable() {
        plugin.getServer().getScheduler().cancelTask(checkTaskId);

        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public final void onPlayerLogin(final PlayerLoginEvent event) {
        final Player player = event.getPlayer();
        final Location currentLocation = player.getLocation();
        final List<Integer> storedLocation = new ArrayList<>();

        storedLocation.add(currentLocation.getBlockX());
        storedLocation.add(currentLocation.getBlockZ());

        locations.put(player, storedLocation);
        ts.put(player, System.currentTimeMillis() / 1000L);
    }

    @EventHandler
    public final void onPlayerQuit(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();

        ts.remove(player);
        locations.remove(player);
    }

    @Override
    public final void run() {
        final int maxTime = plugin.getConfig().getInt("afk.max-time");
        final int distance = plugin.getConfig().getInt("afk.cart-distance");

        for (final Player player : plugin.getServer().getOnlinePlayers()) {
            final Location currentLocation = player.getLocation();
            final List<Integer> storedLocation = locations.get(player);

            final Long now = System.currentTimeMillis() / 1000L;

            if (
                    (
                            !player.isInsideVehicle() ||
                                    (
                                            Math.abs(currentLocation.getBlockX() - storedLocation.get(0)) > distance ||
                                                    Math.abs(currentLocation.getBlockZ() - storedLocation.get(1)) > distance
                                    )
                    ) && (
                            currentLocation.getBlockX() != storedLocation.get(0) ||
                                    currentLocation.getBlockZ() != storedLocation.get(1)
                    )
                    ) {
                storedLocation.set(0, currentLocation.getBlockX());
                storedLocation.set(1, currentLocation.getBlockZ());

                ts.put(player, now);
            }

            if (now - ts.get(player) > maxTime) {
                player.kickPlayer("Du har vært inaktiv for lenge.");
            }
        }
    }
}

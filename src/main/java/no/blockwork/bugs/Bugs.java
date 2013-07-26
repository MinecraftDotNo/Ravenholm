package no.blockwork.bugs;

import no.blockwork.blockwork.Blockwork;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;

public class Bugs implements Listener {
    private final Blockwork plugin;
    private final HashMap<String, Integer> fireTicks;

    public Bugs(Blockwork pluginInstance) {
        this.plugin = pluginInstance;

        fireTicks = new HashMap<>();
    }

    public void onEnable() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void onDisable() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (player.getFireTicks() > 0) {
            plugin.getLogger().info("Player logget out while on fire.");
            fireTicks.put(player.getName(), player.getFireTicks());
        }
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();

        if (fireTicks.containsKey(player.getName())) {
            plugin.getLogger().info("Burning player logged in. Igniting...");
            player.setFireTicks(fireTicks.get(player.getName()));
            fireTicks.remove(player.getName());
        }
    }
}

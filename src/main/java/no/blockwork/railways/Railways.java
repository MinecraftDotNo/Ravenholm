package no.blockwork.railways;

import no.blockwork.blockwork.Blockwork;
import org.bukkit.event.HandlerList;

public final class Railways {
    private final Blockwork plugin;

    private final RailwaysListener railwaysListener;

    public Railways(final Blockwork pluginInstance) {
        plugin = pluginInstance;

        railwaysListener = new RailwaysListener(plugin, this);
    }

    public void onEnable() {
        plugin.getServer().getPluginManager().registerEvents(railwaysListener, plugin);
    }

    public void onDisable() {
        HandlerList.unregisterAll(railwaysListener);
    }
}

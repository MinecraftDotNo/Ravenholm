package no.blockwork.blocklog;

import no.blockwork.blockwork.Blockwork;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public final class BlocklogListener implements Listener {
    private final Blockwork plugin;
    private final Blocklog blocklog;

    public BlocklogListener(Blockwork pluginInstance, Blocklog blocklogInstance) {
        plugin = pluginInstance;
        blocklog = blocklogInstance;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(final BlockPlaceEvent event) {
        blocklog.log(
                event.getBlock().getLocation(),
                event.getPlayer(),
                event,
                event.getBlockPlaced().getType().toString()
        );
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        blocklog.log(
                block.getLocation(),
                event.getPlayer(),
                event,
                block.getType().toString()
        );
    }
}

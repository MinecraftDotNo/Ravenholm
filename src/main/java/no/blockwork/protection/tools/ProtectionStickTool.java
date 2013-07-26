package no.blockwork.protection.tools;

import no.blockwork.blockwork.Blockwork;
import no.blockwork.protection.Protection;
import no.blockwork.tools.Tool;
import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerInteractEvent;

public final class ProtectionStickTool extends Tool {
    private final Blockwork plugin;
    private final Protection protection;

    public ProtectionStickTool(final Blockwork pluginInstance, final Protection protectionInstance) {
        plugin = pluginInstance;
        protection = protectionInstance;
    }

    protected void leftClickAir(final PlayerInteractEvent event) {

    }

    protected void leftClickBlock(final PlayerInteractEvent event) {
        event.setCancelled(true);

        final Block block = event.getClickedBlock();

        protection.freeBlock(block);

        plugin.getBlocklog().log(block.getLocation(), event.getPlayer(), event, "protection.stick");

        block.breakNaturally();
    }

    protected void rightClickAir(final PlayerInteractEvent event) {

    }

    protected void rightClickBlock(final PlayerInteractEvent event) {

    }
}

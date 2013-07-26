package no.blockwork.tools.tools;

import no.blockwork.blockwork.Blockwork;
import no.blockwork.tools.Tool;
import no.blockwork.tools.Tools;
import org.bukkit.event.player.PlayerInteractEvent;

public final class ToolsTeleportTool extends Tool {
    private final Blockwork plugin;
    private final Tools tools;

    public ToolsTeleportTool(final Blockwork pluginInstance, final Tools toolsInstance) {
        plugin = pluginInstance;
        tools = toolsInstance;
    }

    @Override
    public void leftClickAir(final PlayerInteractEvent event) {

    }

    @Override
    public void leftClickBlock(final PlayerInteractEvent event) {

    }

    @Override
    public void rightClickAir(final PlayerInteractEvent event) {

    }

    @Override
    public void rightClickBlock(final PlayerInteractEvent event) {

    }
}

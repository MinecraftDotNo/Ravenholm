package no.blockwork.tools;

import no.blockwork.blockwork.Blockwork;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;

final class ToolsListener implements Listener {
    private final Blockwork plugin;
    private final Tools tools;

    public ToolsListener(final Blockwork pluginInstance, final Tools toolsInstance) {
        plugin = pluginInstance;
        tools = toolsInstance;
    }

    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL) {
            return;
        }

        final Player player = event.getPlayer();
        final Material material = player.getItemInHand().getType();

        Map<Material, Tool> map = tools.getTools();

        if (map.containsKey(material) && plugin.getMyBb().isPowerUser(player)) {
            map.get(material).trigger(event);
        }
    }

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent event) {
        tools.getToolsToolsCommand().disableTools(event.getPlayer());
    }
}

package no.blockwork.protection.tools;

import no.blockwork.blockwork.Blockwork;
import no.blockwork.protection.Protection;
import no.blockwork.tools.Tool;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

public final class ProtectionFreeTool extends Tool {
    private final Blockwork plugin;
    private final Protection protection;

    public ProtectionFreeTool(final Blockwork pluginInstance, final Protection protectionInstance) {
        plugin = pluginInstance;
        protection = protectionInstance;
    }

    @Override
    public void leftClickAir(final PlayerInteractEvent event) {

    }

    @Override
    public void leftClickBlock(final PlayerInteractEvent event) {
        event.setCancelled(true);

        final Block block = event.getClickedBlock();
        final Player player = event.getPlayer();

        if (protection.getBlockOwner(block) == 0) {
            player.sendMessage(ChatColor.GRAY + "Kan ikke fjerne beskyttelse fra ubeskyttet blokk.");
            return;
        }

        protection.freeBlock(block);

        plugin.getBlocklog().log(block.getLocation(), player, event, "protection.freeBlock");

        player.sendMessage(ChatColor.GRAY + "Beskyttelse fjernet.");
    }

    @Override
    public void rightClickAir(final PlayerInteractEvent event) {

    }

    @Override
    public void rightClickBlock(final PlayerInteractEvent event) {
        event.setCancelled(true);

        final Block block = event.getClickedBlock();
        final Player player = event.getPlayer();

        final int owner = protection.getBlockOwner(block);

        if (owner == 0) {
            player.sendMessage(ChatColor.GRAY + "Ingen eier.");
            return;
        }

        player.sendMessage(ChatColor.GRAY + "Blokken eies av \"" + plugin.getMyBb().getUserName(owner) + "\".");
    }
}

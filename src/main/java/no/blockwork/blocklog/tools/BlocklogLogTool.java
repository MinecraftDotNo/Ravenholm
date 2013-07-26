package no.blockwork.blocklog.tools;

import no.blockwork.blocklog.Blocklog;
import no.blockwork.blockwork.Blockwork;
import no.blockwork.tools.Tool;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Collection;

public final class BlocklogLogTool extends Tool {
    private final Blockwork plugin;
    private final Blocklog blocklog;

    public BlocklogLogTool(final Blockwork pluginInstance, final Blocklog blocklogInstance) {
        plugin = pluginInstance;
        blocklog = blocklogInstance;
    }

    @Override
    protected void leftClickAir(final PlayerInteractEvent event) {

    }

    @Override
    protected void leftClickBlock(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final Material item = player.getItemInHand().getType();

        if (item != Material.BOOK) {
            return;
        }

        event.setCancelled(true);

        final Block block = event.getClickedBlock();

        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                final Collection<String> log = blocklog.getLog(block);

                if (log.isEmpty()) {
                    player.sendMessage(ChatColor.GRAY + "Denne blokken har ingen historie.");
                } else {
                    player.sendMessage(ChatColor.GRAY + "Logg for " + block.getX() + " " + block.getY() + " " + block.getZ() + ":");

                    for (String string : log) {
                        player.sendMessage(ChatColor.GRAY + string + ".");
                    }
                }
            }
        });
    }

    @Override
    protected void rightClickAir(final PlayerInteractEvent event) {

    }

    @Override
    protected void rightClickBlock(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final Material item = player.getItemInHand().getType();

        if (item != Material.LOCKED_CHEST) {
            return;
        }

        event.setCancelled(true);

        final Block block = event.getClickedBlock().getRelative(event.getBlockFace());

        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                final Collection<String> log = blocklog.getLog(block);

                if (log.isEmpty()) {
                    player.sendMessage(ChatColor.GRAY + "Denne blokken har ingen historie.");
                } else {
                    player.sendMessage(ChatColor.GRAY + "Logg for " + block.getX() + " " + block.getY() + " " + block.getZ() + ":");

                    for (String string : log) {
                        player.sendMessage(ChatColor.GRAY + string + ".");
                    }
                }
            }
        });
    }
}

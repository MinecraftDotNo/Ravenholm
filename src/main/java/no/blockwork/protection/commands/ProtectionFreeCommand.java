package no.blockwork.protection.commands;

import no.blockwork.blockwork.Blockwork;
import no.blockwork.protection.Protection;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;

public final class ProtectionFreeCommand implements CommandExecutor {
    private final Blockwork plugin;
    private final Protection protection;

    public ProtectionFreeCommand(final Blockwork pluginInstance, final Protection protectionInstance) {
        plugin = pluginInstance;
        protection = protectionInstance;
    }

    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }

        final Player player = (Player) sender;

        if (!plugin.getMyBb().isPowerUser(player)) {
            return true;
        }

        final Collection<Block> blocks = plugin.getTools().getToolsSelectionTool().getBlocks(player);

        if (blocks.size() == 0) {
            player.sendMessage(ChatColor.GRAY + "Ingen blokker markert.");
            return true;
        }

        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                player.sendMessage(ChatColor.GRAY + "Frigjør område (" + blocks.size() + " blokker)...");

                for (final Block block : blocks) {
                    protection.freeBlock(block);
                }

                player.sendMessage(ChatColor.GRAY + "Området er frigjort.");
            }
        });

        return true;
    }
}

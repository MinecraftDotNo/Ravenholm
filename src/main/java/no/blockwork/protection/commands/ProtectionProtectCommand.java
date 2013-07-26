package no.blockwork.protection.commands;

import no.blockwork.blockwork.Blockwork;
import no.blockwork.mybb.MyBb;
import no.blockwork.protection.Protection;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;

public final class ProtectionProtectCommand implements CommandExecutor {
    private final Blockwork plugin;
    private final Protection protection;

    public ProtectionProtectCommand(final Blockwork pluginInstance, final Protection protectionInstance) {
        plugin = pluginInstance;
        protection = protectionInstance;
    }

    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }

        final Player player = (Player) sender;

        final MyBb mybb = plugin.getMyBb();

        if (!mybb.isPowerUser(player)) {
            return true;
        }

        if (args.length < 1) {
            return false;
        }

        final int uid = mybb.getUserId("%" + args[0] + "%");

        if (uid == 0) {
            player.sendMessage(ChatColor.GRAY + "Fant ikke spilleren.");
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
                player.sendMessage(ChatColor.GRAY + "Beskytter område (" + blocks.size() + " blokker)...");

                for (final Block block : blocks) {
                    if (block.getType() != Material.AIR) {
                        protection.setBlockOwner(block, uid);
                        plugin.getBlocklog().log(block.getLocation(), player, null, "protection.setBlockOwner");
                    }
                }

                player.sendMessage(ChatColor.GRAY + "Området eies nå av \"" + mybb.getUserName(uid) + "\".");
            }
        });

        return true;
    }
}

package no.blockwork.tools.commands;

import no.blockwork.blockwork.Blockwork;
import no.blockwork.tools.Tools;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class ToolsSetSpawnCommand implements CommandExecutor {
    private final Blockwork plugin;
    private final Tools tools;

    public ToolsSetSpawnCommand(final Blockwork pluginInstance, final Tools toolsInstance) {
        plugin = pluginInstance;
        tools = toolsInstance;
    }

    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }

        final Player player = (Player) sender;

        if (!plugin.getMyBb().isPowerUser(player)) {
            return true;
        }

        final Location location = player.getLocation();
        player.getWorld().setSpawnLocation(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        player.sendMessage(ChatColor.GRAY + "Spawn endret.");

        return true;
    }
}

package no.blockwork.tools.commands;

import no.blockwork.blockwork.Blockwork;
import no.blockwork.tools.Tools;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public final class ToolsTpCommand implements CommandExecutor {
    private final Blockwork plugin;
    private final Tools tools;

    public final Map<Player, Location> original;

    public ToolsTpCommand(final Blockwork pluginInstance, final Tools toolsInstance) {
        plugin = pluginInstance;
        tools = toolsInstance;

        original = new HashMap<>();
    }

    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }

        final Player player = (Player) sender;

        if (!plugin.getMyBb().isPowerUser(player)) {
            return true;
        }

        if (args.length < 1) {
            return false;
        }

        final Player target = plugin.getServer().getPlayer(args[0]);

        if (target == null) {
            player.sendMessage(ChatColor.GRAY + "Fant ikke spilleren.");
            return true;
        }

        original.put(player, player.getLocation());

        player.teleport(target.getLocation());

        return true;
    }
}

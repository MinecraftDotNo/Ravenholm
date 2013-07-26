package no.blockwork.tools.commands;

import no.blockwork.blockwork.Blockwork;
import no.blockwork.tools.Tools;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public final class ToolsGotoCommand implements CommandExecutor {
    private final Blockwork plugin;
    private final Tools tools;

    public final Map<Player, Location> original;

    public ToolsGotoCommand(final Blockwork pluginInstance, final Tools toolsInstance) {
        plugin = pluginInstance;
        tools = toolsInstance;

        original = new HashMap<>();
    }

    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }

        if (args.length != 3) {
            return false;
        }

        final Player player = (Player) sender;

        if (!plugin.getMyBb().isPowerUser(player)) {
            return true;
        }

        final Location current = player.getLocation();
        player.teleport(new Location(
                player.getWorld(),
                Double.parseDouble(args[0]),
                Double.parseDouble(args[1]),
                Double.parseDouble(args[2])
        ));
        tools.getToolsTpCommand().original.put(player, current);

        return true;
    }
}

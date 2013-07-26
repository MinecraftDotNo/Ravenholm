package no.blockwork.tools.commands;

import no.blockwork.blockwork.Blockwork;
import no.blockwork.tools.Tools;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class ToolsFlyCommand implements CommandExecutor {
    private final Blockwork plugin;
    private final Tools tools;

    public ToolsFlyCommand(final Blockwork pluginInstance, final Tools toolsInstance) {
        plugin = pluginInstance;
        tools = toolsInstance;
    }

    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Denne kommandoen kan kun brukes in-game.");
            return true;
        }

        final Player player = (Player) sender;

        if (!plugin.getMyBb().isPowerUser(player)) {
            return true;
        }

        player.setAllowFlight(!player.getAllowFlight());

        player.sendMessage(ChatColor.GRAY + (player.getAllowFlight() ? "Flyving er nå aktivert." : "Flyving er nå deaktivert."));

        return true;
    }
}

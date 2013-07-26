package no.blockwork.tools.commands;

import no.blockwork.blockwork.Blockwork;
import no.blockwork.tools.Tools;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public final class ToolsButcherCommand implements CommandExecutor {
    private final Blockwork plugin;
    private final Tools tools;

    public ToolsButcherCommand(final Blockwork pluginInstance, final Tools toolsInstance) {
        plugin = pluginInstance;
        tools = toolsInstance;
    }

    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used in-game.");
            return true;
        }

        final Player player = (Player) sender;

        if (!plugin.getMyBb().isPowerUser(player)) {
            return true;
        }

        int r = 100;
        if (args.length == 1) {
            r = Integer.parseInt(args[0]);
        }

        int n = 0;

        for (final Entity entity : player.getNearbyEntities(r, r, r)) {
            if (entity instanceof LivingEntity && !(entity instanceof Player)) {
                entity.remove();
                n++;
            }
        }

        player.sendMessage(ChatColor.GRAY + "Drepte " + n + " skapninger innenfor " + r + " blokker.");

        return true;
    }
}

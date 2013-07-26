package no.blockwork.tools.commands;

import no.blockwork.blockwork.Blockwork;
import no.blockwork.tools.Tools;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public final class ToolsMobCommand implements CommandExecutor {
    private final Blockwork plugin;
    private final Tools tools;

    public ToolsMobCommand(final Blockwork pluginInstance, final Tools toolsInstance) {
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

        if (args.length < 1 || args.length > 2) {
            return false;
        }

        final EntityType type = EntityType.fromName(args[0]);

        if (type == null) {
            player.sendMessage(ChatColor.GRAY + "Ukjent monstertype.");
            return true;
        }

        int n = 1;
        if (args.length == 2) {
            n = Integer.parseInt(args[1]);
        }

        player.sendMessage(ChatColor.GRAY + "Spawner " + n + " monstere av typen " + type.toString() + "...");

        Location l = player.getLocation();
        final Block target = player.getTargetBlock(null, 100);
        if (target != null) {
            l = target.getRelative(BlockFace.UP).getLocation();
        }

        for (int i = 1; i <= n; i++) {
            player.getWorld().spawnEntity(l, type);
        }

        return true;
    }
}

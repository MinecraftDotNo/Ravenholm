package no.blockwork.mybb.commands;

import no.blockwork.blockwork.Blockwork;
import no.blockwork.mybb.MyBb;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MyBbBanCommand implements CommandExecutor {
    private Blockwork plugin;
    private MyBb mybb;

    public MyBbBanCommand(Blockwork pluginInstance, MyBb mybbInstance) {
        plugin = pluginInstance;
        mybb = mybbInstance;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used in-game.");
            return true;
        }

        Player player = (Player) sender;

        if (!mybb.isPowerUser(player)) {
            return true;
        }

        if (args.length < 1) {
            return false;
        }

        int uid = mybb.getUserId("%" + args[0] + "%");

        if (uid == 0) {
            mybb.setUserGroup(args[0], MyBb.Group.AWAITING_ACTIVATION);
            uid = mybb.getUserId(args[0]);
        }

        String name = mybb.getUserName(uid);

        System.arraycopy(args, 1, args, 0, args.length - 1);
        String reason = args.length > 0 ? StringUtils.join(args, " ") : "Ingen grunn oppgitt.";

        if (!mybb.banUser(name, player, reason)) {
            player.sendMessage(ChatColor.RED + "Noe gikk galt.");
        } else {
            player.sendMessage(ChatColor.GRAY + name + " er nå utestengt.");

            try {
                plugin.getServer().getPlayer(name).kickPlayer("Du er ikke lenger velkommen her. Grunn: " + reason);
            } catch (NullPointerException exception) {
                // The user wasn't online. Everything is okay.
            }
        }

        return true;
    }
}

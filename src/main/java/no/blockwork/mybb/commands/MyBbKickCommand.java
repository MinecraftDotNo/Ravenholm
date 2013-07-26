package no.blockwork.mybb.commands;

import no.blockwork.blockwork.Blockwork;
import no.blockwork.mybb.MyBb;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MyBbKickCommand implements CommandExecutor {
    private Blockwork plugin;
    private MyBb mybb;

    public MyBbKickCommand(Blockwork pluginInstance, MyBb mybbInstance) {
        plugin = pluginInstance;
        mybb = mybbInstance;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player && !mybb.isPowerUser((Player) sender)) {
            return true;
        }

        if (!mybb.isPowerUser((Player) sender)) {
            return true;
        }

        if (args.length < 1 || args.length > 2) {
            return false;
        }

        String reason = args.length == 2 ? args[1] : ""; // Todo: Default message?

        Player victim = plugin.getServer().getPlayer(args[0]);

        if (victim == null) {
            sender.sendMessage(ChatColor.GRAY + "Fant ikke spilleren.");
            return true;
        }

        victim.getWorld().strikeLightningEffect(victim.getLocation());
        victim.kickPlayer("Du ble sparket ut. " + reason);
        plugin.getServer().broadcastMessage(victim.getName() + " ble sparket ut. " + reason);

        return true;
    }
}

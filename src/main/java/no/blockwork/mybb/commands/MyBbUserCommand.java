package no.blockwork.mybb.commands;

import no.blockwork.blockwork.Blockwork;
import no.blockwork.blockwork.BlockworkListener;
import no.blockwork.mybb.MyBb;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.RegisteredListener;

public class MyBbUserCommand implements CommandExecutor {
    private final Blockwork plugin;
    private final MyBb mybb;

    public MyBbUserCommand(Blockwork pluginInstance, MyBb mybbInstance) {
        plugin = pluginInstance;
        mybb = mybbInstance;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player && !mybb.isPowerUser((Player) sender)) {
            return true;
        }

        if (args.length != 1) {
            return false;
        }

        int uid = mybb.getUserId(args[0]);

        if (uid == 0) {
            sender.sendMessage(ChatColor.RED + "Fant ikke spilleren.");
            return true;
        }

        String name = mybb.getUserName(uid);

        if (!mybb.setUserGroup(name, MyBb.Group.REGISTERED)) {
            sender.sendMessage(ChatColor.RED + "Noe gikk galt.");
            return true;
        }

        sender.sendMessage(ChatColor.GREEN + name + " er nå registrert.");

        Player player = plugin.getServer().getPlayerExact(name);
        if (player != null) {
            for (RegisteredListener eventListener : HandlerList.getRegisteredListeners(plugin)) {
                Listener listener = eventListener.getListener();

                if (listener instanceof BlockworkListener) {
                    ((BlockworkListener) listener).onPlayerJoin(new PlayerJoinEvent(player, null));
                    break;
                }
            }

            player.sendMessage(ChatColor.GREEN + "Du er nå registrert.");
        }

        return true;
    }
}

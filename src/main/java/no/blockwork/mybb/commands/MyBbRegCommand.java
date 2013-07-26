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

public class MyBbRegCommand implements CommandExecutor {
    private Blockwork plugin;
    private MyBb mybb;

    public MyBbRegCommand(Blockwork pluginInstance, MyBb mybbInstance) {
        plugin = pluginInstance;
        mybb = mybbInstance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used in-game.");
            return true;
        }

        Player player = (Player) sender;

        if (mybb.getUserGroup(player) != MyBb.Group.AWAITING_ACTIVATION) {
            player.sendMessage(ChatColor.RED + "Du er alt registrert!");
            return true;
        }

        if (args.length != 1 || !args[0].equalsIgnoreCase("godtatt")) {
            player.sendMessage(ChatColor.RED + "Du har tydeligvis ikke lest instruksene for registrering. Prøv igjen senere!");
            return true;
        }

        mybb.setUserGroup(player, MyBb.Group.REGISTERED);

        player.sendMessage(ChatColor.GREEN + "Du er nå registrert!");

        for (RegisteredListener eventListener : HandlerList.getRegisteredListeners(plugin)) {
            Listener listener = eventListener.getListener();

            if (listener instanceof BlockworkListener) {
                ((BlockworkListener) listener).onPlayerJoin(new PlayerJoinEvent(player, null));
                break;
            }
        }

        return true;
    }
}

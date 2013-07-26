package no.blockwork.mybb.commands;

import no.blockwork.blockwork.Blockwork;
import no.blockwork.mybb.MyBb;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class MyBbFriendCommand implements CommandExecutor {
    private Blockwork plugin;
    private MyBb mybb;

    public MyBbFriendCommand(Blockwork pluginInstance, MyBb mybbInstance) {
        plugin = pluginInstance;
        mybb = mybbInstance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used in-game.");
            return true;
        }

        if (args.length == 0) {
            return false;
        }

        Player player = (Player) sender;

        if (mybb.isUserGuest(player)) {
            return true;
        }

        int user_id = mybb.getUserId(player);

        if (args[0].equalsIgnoreCase("add") && args.length == 2) {
            int friend_id = mybb.getUserId(args[1]);

            if (friend_id == 0) {
                player.sendMessage(ChatColor.RED + "Ukjent spiller.");
                return true;
            }

            if (!mybb.addFriend(user_id, friend_id)) {
                player.sendMessage(ChatColor.RED + "Kunne ikke legge til venn.");
            } else {
                player.sendMessage(ChatColor.GREEN + "Ny venn lagt til.");
            }

            return true;
        } else if (args[0].equalsIgnoreCase("remove") && args.length == 2) {
            int friend_id = mybb.getUserId(args[1]);

            if (friend_id == 0) {
                player.sendMessage(ChatColor.RED + "Ukjent spiller.");
                return true;
            }

            if (!mybb.removeFriend(user_id, friend_id)) {
                player.sendMessage(ChatColor.RED + "Kunne ikke fjerne venn.");
            } else {
                player.sendMessage(ChatColor.GREEN + "Venn fjernet.");
            }

            return true;
        } else if (args[0].equalsIgnoreCase("list") && args.length == 1) {
            List<String> friends = new ArrayList<String>();

            for (int friend_id : mybb.getFriends(user_id)) {
                ChatColor color = ChatColor.DARK_GREEN;

                if (mybb.hasFriend(friend_id, user_id)) {
                    color = ChatColor.GREEN;
                }

                friends.add(color + mybb.getUserName(friend_id));
            }

            player.sendMessage(ChatColor.GRAY + "Venneliste: " + StringUtils.join(friends, ChatColor.GRAY + ", "));

            return true;
        }

        return false;
    }
}

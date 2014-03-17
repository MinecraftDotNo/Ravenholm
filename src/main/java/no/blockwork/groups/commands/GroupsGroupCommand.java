package no.blockwork.groups.commands;

import no.blockwork.blockwork.Blockwork;
import no.blockwork.groups.Group;
import no.blockwork.groups.Groups;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class GroupsGroupCommand implements CommandExecutor {
	private final Blockwork plugin;
	private final Groups groups;

	public GroupsGroupCommand(Blockwork pluginInstance,Groups groupsInstance) {
		plugin = pluginInstance;
		groups = groupsInstance;
	}

	public boolean onCommand(CommandSender sender,Command command,String label,String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("This command can only be used in-game.");
			return true;
		}

		if (args.length < 1) {
			return false;
		}

		Player player = (Player)sender;

		if (args[0].equalsIgnoreCase("invite")) {
			if (args.length < 2) {
				return false;
			}

			Player victim = plugin.getServer().getPlayer(args[1]);

			if (victim == null) {
				player.sendMessage(ChatColor.GRAY + "Fant ikke spilleren.");
				return true;
			}

			if (victim == player) {
				player.sendMessage(ChatColor.GRAY + "Du kan ikke invitere deg selv.");
				return true;
			}

			Group vGroup = groups.getGroup(victim);

			if ((vGroup != null && !vGroup.getName().equals(victim.getName())) || groups.getInvite(victim) != null) {
				player.sendMessage(victim.getDisplayName() + ChatColor.GRAY + " er alt i, eller invitert til, en gruppe.");
				return true;
			}

			Group group = groups.getGroup(player);
			if (group == null) {
				group = groups.createGroup(player);
			}

			if (!group.getName().equals(player.getName())) {
				player.sendMessage(ChatColor.GRAY + "Kun " + group.getName() + " kan invitere spillere til " + group.getName() + "-gruppen.");
				return true;
			}

			victim.sendMessage(player.getDisplayName() + ChatColor.GRAY + " inviterer deg til " + group.getName() + "-gruppen.");
			group.broadcastMessage(victim.getDisplayName() + ChatColor.GRAY + " ble invitert til " + group.getName() + "-gruppen.");
			group.addInvite(victim);

			return true;
		}

		if (args[0].equalsIgnoreCase("accept")) {
			Group group = groups.getInvite(player);

			if (group == null) {
				player.sendMessage(ChatColor.GRAY + "Du har ingen invitasjoner å godta.");
				return true;
			}

			group.addMember(player);
			group.removeInvite(player);
			group.broadcastMessage(player.getDisplayName() + ChatColor.GRAY + " ble medlem i " + group.getName() + "-gruppen.");

			return true;
		}

		if (args[0].equalsIgnoreCase("reject")) {
			Group group = groups.getInvite(player);

			if (group == null) {
				player.sendMessage(ChatColor.GRAY + "Du har ingen invitasjoner å avslå.");
				return true;
			}

			group.broadcastMessage(player.getDisplayName() + ChatColor.GRAY + " avslo invitasjonen til " + group.getName() + "-gruppen.");
			group.removeInvite(player);

			player.sendMessage(ChatColor.GRAY + "Du avslo invitasjonen til " + group.getName() + "-gruppen.");

			return true;
		}

		if (args[0].equalsIgnoreCase("list")) {
			Group group = groups.getGroup(player);

			if (group == null || group.getMembers().size() == 0) {
				player.sendMessage(ChatColor.GRAY + "Du er ikke medlem i noen gruppe.");
				return true;
			}

			List<String> list = new ArrayList<>();
			for (Player p : group.getMembers()) {
				list.add(p.getDisplayName());
			}

			player.sendMessage(ChatColor.GRAY + "Medlemmer i " + group.getName() + "-gruppen: " + StringUtils.join(list,ChatColor.GRAY + ", "));

			return true;
		}

		if (args[0].equalsIgnoreCase("leave")) {
			Group group = groups.getGroup(player);

			if (group == null || group.getName().equals(player.getName())) {
				player.sendMessage(ChatColor.GRAY + "Du er ikke medlem i noen gruppe.");
				return true;
			}

			group.broadcastMessage(player.getDisplayName() + ChatColor.GRAY + " forlot " + group.getName() + "-gruppen.");
			group.removeMember(player);

			return true;
		}

		if (args[0].equalsIgnoreCase("kick")) {
			if (args.length < 2) {
				return false;
			}

			Player victim = plugin.getServer().getPlayer(args[1]);

			if (victim == null) {
				player.sendMessage(ChatColor.GRAY + "Fant ikke spilleren.");
				return true;
			}

			if (victim == player) {
				player.sendMessage(ChatColor.GRAY + "Du kan ikke sparke ut deg selv.");
				return true;
			}

			Group group = groups.getGroup(player);

			if (group == null) {
				player.sendMessage(ChatColor.GRAY + "Du er ikke medlem av noen gruppe.");
				return true;
			}

			if (!group.getName().equals(player.getName())) {
				player.sendMessage(ChatColor.GRAY + "Kun " + group.getName() + " kan sparke spillere ut fra " + group.getName() + "-gruppen.");
				return true;
			}

			group.broadcastMessage(victim.getDisplayName() + ChatColor.GRAY + " ble sparket fra " + group.getName() + "-gruppen.");
			group.removeMember(victim);

			return true;
		}

		if (args[0].equalsIgnoreCase("join")) {
			if (args.length != 2) {
				return false;
			}

			int user_id = plugin.getMyBb().getUserId(player);
			int friend_id = plugin.getMyBb().getUserId(args[1]);

			if (friend_id == 0) {
				player.sendMessage(ChatColor.RED + "Ukjent spiller.");
				return true;
			}

			Group group = groups.getOwnGroup(friend_id);

			if (!plugin.getMyBb().hasFriend(friend_id,user_id)) {
				player.sendMessage(ChatColor.RED + "Du trenger invitasjon for å bli medlem av " + group.getName() + "-gruppen.");
				return true;
			}

			Group invitedTo = groups.getInvite(player);
			if (invitedTo != null) {
				invitedTo.removeInvite(player);
			}

			Group isIn = groups.getGroup(player);
			if (isIn != null) {
				if (isIn.hasMember(player)) {
					player.sendMessage(ChatColor.RED + "Du er alt i " + isIn.getName() + "-gruppen.");
					return true;
				}

				isIn.broadcastMessage(player.getDisplayName() + ChatColor.GRAY + " forlot " + isIn.getName() + "-gruppen.");
				isIn.removeMember(player);
			}

			group.addMember(player);
			group.broadcastMessage(player.getDisplayName() + ChatColor.GRAY + " ble medlem i " + group.getName() + "-gruppen.");

			return true;
		}

        if (args[0].equalsIgnoreCase("chat")) {
            this.groups.setChat(player,!this.groups.getChat(player));

            player.sendMessage(ChatColor.GRAY + "Gruppechat " + (this.groups.getChat(player) ? ChatColor.GREEN + "på" : ChatColor.RED + "av") +  ChatColor.GRAY + ".");

            return true;
        }

		return false;
	}
}

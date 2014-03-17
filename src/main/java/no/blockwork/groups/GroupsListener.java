package no.blockwork.groups;

import me.desht.dhutils.ExperienceManager;
import no.blockwork.blockwork.Blockwork;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

final class GroupsListener implements Listener {
	private final Blockwork plugin;
	private final Groups groups;

	public GroupsListener(final Blockwork pluginInstance,final Groups groupsInstance) {
		plugin = pluginInstance;
		groups = groupsInstance;
	}

	@EventHandler
	public void onPlayerQuit(final PlayerQuitEvent event) {
		final Player player = event.getPlayer();

        this.groups.setChat(player,false);

		final Group memberOf = groups.getGroup(player);
		if (memberOf != null) {
			memberOf.removeMember(player);

			if (memberOf.getMembers().isEmpty() && memberOf.getInvites().isEmpty()) {
				groups.removeGroup(memberOf);
			}
		}

		final Group invitedTo = groups.getInvite(player);
		if (invitedTo != null) {
			invitedTo.removeInvite(player);

			if (invitedTo.getMembers().isEmpty() && invitedTo.getInvites().isEmpty()) {
				groups.removeGroup(invitedTo);
			}
		}
	}

	@EventHandler
	public void onPlayerExpChange(final PlayerExpChangeEvent event) {
		final Player player = event.getPlayer();
		final Group group = groups.getGroup(player);

		if (group == null) {
			return;
		}

		final Location location = player.getLocation();

		final List<Player> members = group.getMembers();

		final int each = event.getAmount() / (members.size() + 1);
		final int radius = plugin.getConfig().getInt("groups.exp-share-radius");

		for (final Player member : members) {
			if (member.getWorld() == player.getWorld() && member.getLocation().distanceSquared(location) < Math.pow(radius,2)) {
				new ExperienceManager(member).changeExp(each);
			}
		}

		event.setAmount(each);
	}
}

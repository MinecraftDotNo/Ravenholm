package no.blockwork.chat;

import no.blockwork.blockwork.Blockwork;
import no.blockwork.groups.Group;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

class ChatListener implements Listener {
	private final Blockwork plugin;
	private final Chat chat;

	public ChatListener(Blockwork pluginInstance,Chat chatInstance) {
		plugin = pluginInstance;
		chat = chatInstance;
	}

	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		if (event.getEntity() instanceof Player) {
			((PlayerDeathEvent) event).setDeathMessage(null);
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		event.setJoinMessage(null);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		event.setQuitMessage(null);

		chat.getLocations().remove(event.getPlayer().getName());
	}

	@EventHandler
	public void onPlayerKick(PlayerKickEvent event) {
		event.setLeaveMessage(null);
	}

	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		chat.getLocations().put(event.getPlayer().getName(),event.getTo());
	}

	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		String message = event.getMessage().replaceAll(ChatColor.COLOR_CHAR + ".","");

		Player pmRecipient = chat.getPrivate(message);
		if (pmRecipient != null) {
			// Private message.
			event.setCancelled(true);
			event.setFormat(ChatColor.AQUA + "[Privat]" + ChatColor.RESET + " %s: %s");

			String display = String.format(
                event.getFormat(),
                player.getDisplayName() + " -> " + pmRecipient.getDisplayName(),
                message.split(": ?",2)[1]
            );

			player.sendMessage(display);
			pmRecipient.sendMessage(display);

			return;
		}

		if (message.endsWith("!")) {
			// Shout.
			event.setCancelled(true);
			event.setFormat(ChatColor.YELLOW + "[Global]" + ChatColor.RESET + " %s: %s");

			Iterator<Player> i = event.getRecipients().iterator();
			while (i.hasNext()) {
				i.next().sendMessage(String.format(event.getFormat(),player.getDisplayName(),event.getMessage()));
			}

			return;
		}

        Group group = this.plugin.getGroups().getGroup(player);
        ArrayList<Player> groupMembers = (ArrayList<Player>) ((ArrayList<Player>) group.getMembers()).clone();
        if (group.getOwner() != null) {
            groupMembers.add(group.getOwner());
        }
        if (groupMembers.size() > 1 && this.plugin.getGroups().getChat(player)) {
            // Group chatter.
            event.setCancelled(true);
            event.setFormat(ChatColor.DARK_GREEN + "[Gruppe]" + ChatColor.RESET + " %s: %s");

            for (Player member : groupMembers) {
                member.sendMessage(String.format(event.getFormat(),player.getDisplayName(),event.getMessage()));
            }

            return;
        }

		// Local chatter.
		event.setFormat(ChatColor.GRAY + "[Lokal]" + ChatColor.RESET +" %s: %s");

		Iterator<Player> i = event.getRecipients().iterator();

		Map<String,Location> locations = chat.getLocations();

		Location playerLocation = locations.get(player.getName());
		if (playerLocation == null || !player.isOnline()) {
			event.setCancelled(true);

			while (i.hasNext()) {
				i.next().sendMessage(String.format(event.getFormat(),player.getName(),event.getMessage()));
			}

			return;
		}

		FileConfiguration config = plugin.getConfig();

		while (i.hasNext()) {
			Player recipient = i.next();
			Location recipientLocation = locations.get(recipient.getName());
			if (
				recipientLocation == null ||
				!playerLocation.getWorld().getUID().equals(recipientLocation.getWorld().getUID()) ||
				playerLocation.distanceSquared(recipientLocation) > Math.pow(config.getInt("chat.radius"),2)
			) {
				i.remove();
			}
		}
	}
}

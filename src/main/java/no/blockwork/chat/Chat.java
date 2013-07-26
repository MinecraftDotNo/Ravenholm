package no.blockwork.chat;

import no.blockwork.blockwork.Blockwork;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Chat {
    private final Blockwork plugin;

    private final ConcurrentHashMap<String, Location> locations;

    private final ChatListener chatListener;

    public Chat(Blockwork pluginInstance) {
        plugin = pluginInstance;

        locations = new ConcurrentHashMap<>();

        chatListener = new ChatListener(plugin, this);
    }

    public void onEnable() {
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    locations.put(player.getName(), player.getLocation());
                }
            }
        }, 0, 40);

        plugin.getServer().getPluginManager().registerEvents(chatListener, plugin);
    }

    public void onDisable() {
        HandlerList.unregisterAll(chatListener);
    }

    public Map<String, Location> getLocations() {
        return locations;
    }

    public void tagPlayer(Player player) {
        ChatColor color = ChatColor.GRAY;

        switch (plugin.getMyBb().getUserGroup(player)) {
            case REGISTERED:
                color = ChatColor.WHITE;
                break;
            case SUPER_MODERATOR:
            case MODERATOR:
                color = ChatColor.BLUE;
                break;
            case ADMINISTRATOR:
                color = ChatColor.GOLD;
                break;
        }

        player.setDisplayName(color + player.getName() + ChatColor.RESET);

        if (color == ChatColor.WHITE) {
            // Update it, in case it was something else before.
            player.setPlayerListName(player.getName());
        } else {
            player.setPlayerListName(
                    player.getDisplayName().length() > 16 ?
                            player.getDisplayName().substring(0, 11) + "..." + ChatColor.RESET :
                            player.getDisplayName()
            );
        }
    }

    public Player getPrivate(String message) {
        if (message.contains(":")) {
            String[] parts = message.split(":", 2);

            if (!parts[0].isEmpty() && !parts[0].contains(" ")) {
                Player recipient = plugin.getServer().getPlayer(parts[0]);

                if (recipient != null) {
                    return recipient;
                }
            }
        }

        return null;
    }
}

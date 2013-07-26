package no.blockwork.irc;

import no.blockwork.blockwork.Blockwork;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

class IrcListener implements Listener {
    private final Blockwork plugin;
    private final Irc irc;

    public IrcListener(Blockwork pluginInstance, Irc ircInstance) {
        plugin = pluginInstance;
        irc = ircInstance;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        if (!player.isOnline()) {
            return;
        }

        Player recipient = plugin.getChat().getPrivate(message);

        if (recipient == null) {
            irc.getMainChannel().send(irc.getDisplayName(player) + ": " + message);
        } else {
            irc.getPmChannel().send(irc.getDisplayName(player) + " -> " + irc.getDisplayName(recipient) + ": " + message.split(": ?", 2)[1]);
        }
    }
}

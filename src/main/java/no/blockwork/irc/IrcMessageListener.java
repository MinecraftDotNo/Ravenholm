package no.blockwork.irc;

import com.sorcix.sirc.Channel;
import com.sorcix.sirc.IrcConnection;
import com.sorcix.sirc.MessageListener;
import com.sorcix.sirc.User;
import no.blockwork.blockwork.Blockwork;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashSet;

class IrcMessageListener implements MessageListener {
    private final Blockwork plugin;
    private final Irc irc;

    public IrcMessageListener(Blockwork pluginInstance, Irc ircInstance) {
        plugin = pluginInstance;
        irc = ircInstance;
    }

    public void onAction(IrcConnection irc, User sender, Channel target, String action) {

    }

    public void onAction(IrcConnection irc, User sender, String action) {

    }

    public void onCtcpReply(IrcConnection irc, User sender, String command, String message) {

    }

    public void onMessage(IrcConnection irc, User sender, Channel target, String message) {
        if (message.substring(0, 1).equals("!")) {
            String data[] = message.substring(1).split(" ");
            this.irc.commandHandler(target, data);
        } else if (target.getName().equalsIgnoreCase(this.irc.getMainChannel().getName())) {
            plugin.getServer().getPluginManager().callEvent(new AsyncPlayerChatEvent(
                    false,
                    new IrcPlayer(plugin.getServer(), sender.getNick()),
                    message,
                    new HashSet<>(plugin.getServer().getOnlinePlayers())
            ));
        }
    }

    public void onNotice(IrcConnection irc, User sender, Channel target, String message) {

    }

    public void onNotice(IrcConnection irc, User sender, String message) {

    }

    public void onPrivateMessage(IrcConnection irc, User sender, String message) {

    }
}

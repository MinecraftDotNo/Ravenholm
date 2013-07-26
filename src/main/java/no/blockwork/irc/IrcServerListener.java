package no.blockwork.irc;

import com.sorcix.sirc.Channel;
import com.sorcix.sirc.IrcConnection;
import com.sorcix.sirc.ServerListener;
import com.sorcix.sirc.User;
import no.blockwork.blockwork.Blockwork;

class IrcServerListener implements ServerListener {
    private final Blockwork plugin;
    private final Irc irc;

    public IrcServerListener(Blockwork pluginInstance, Irc ircInstance) {
        plugin = pluginInstance;
        irc = ircInstance;
    }

    public void onConnect(IrcConnection irc) {
        irc.createUser("NickServ").send("IDENTIFY " + plugin.getConfig().getString("irc.password"));

        this.irc.getMainChannel().join();
        this.irc.getPmChannel().join();
    }

    public void onDisconnect(IrcConnection irc) {

    }

    public void onInvite(IrcConnection irc, User sender, User user, Channel channel) {

    }

    public void onJoin(IrcConnection irc, Channel channel, User user) {

    }

    public void onKick(IrcConnection irc, Channel channel, User sender, User user, String msg) {

    }

    public void onMode(IrcConnection irc, Channel channel, User sender, String mode) {

    }

    public void onMotd(IrcConnection irc, String motd) {

    }

    public void onNick(IrcConnection irc, User oldUser, User newUser) {

    }

    public void onPart(IrcConnection irc, Channel channel, User user, String message) {

    }

    public void onQuit(IrcConnection irc, User user, String message) {

    }

    public void onTopic(IrcConnection irc, Channel channel, User sender, String topic) {

    }
}

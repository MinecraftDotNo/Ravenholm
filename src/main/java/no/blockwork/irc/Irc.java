package no.blockwork.irc;

import com.sorcix.sirc.*;
import no.blockwork.blockwork.Blockwork;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Irc {
    private final Blockwork plugin;

    private final Map<ChatColor, String> colorsMcToIrc;

    private IrcConnection irc;
    private int connectionTaskId;
    private final IrcServerListener ircServerListener;
    private final IrcMessageListener ircMessageListener;

    private Channel mainChannel;
    private Channel pmChannel;

    private IrcListener ircListener;

    public Irc(Blockwork pluginInstance) {
        plugin = pluginInstance;

        colorsMcToIrc = new HashMap<>();
        colorsMcToIrc.put(ChatColor.WHITE, "");
        colorsMcToIrc.put(ChatColor.BLACK, "");
        colorsMcToIrc.put(ChatColor.GOLD, IrcColors.COLOR + IrcColors.ORANGE);
        colorsMcToIrc.put(ChatColor.BLUE, IrcColors.COLOR + IrcColors.BLUE);

        ircListener = new IrcListener(plugin, this);
        ircServerListener = new IrcServerListener(plugin, this);
        ircMessageListener = new IrcMessageListener(plugin, this);
    }

    public void onEnable() {
        FileConfiguration config = plugin.getConfig();

        irc = new IrcConnection(
                config.getString("irc.host"),
                config.getInt("irc.port")
        );

        irc.setCharset(Charset.forName(config.getString("irc.charset")));
        irc.setNick(config.getString("irc.nick"));

        irc.addServerListener(ircServerListener);
        irc.addMessageListener(ircMessageListener);

        mainChannel = irc.createChannel(config.getString("irc.channel"));
        pmChannel = irc.createChannel(config.getString("irc.pm-channel"));

        connectionTaskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            public void run() {
                if (!irc.isConnected()) {
                    onDisable();
                    onEnable();
                }
            }
        }, 0, 600);

        plugin.getServer().getPluginManager().registerEvents(ircListener, plugin);

        connect();
    }

    public void onDisable() {
        HandlerList.unregisterAll(ircListener);

        plugin.getServer().getScheduler().cancelTask(connectionTaskId);

        irc.removeMessageListener(ircMessageListener);
        irc.removeServerListener(ircServerListener);

        irc.disconnect();
    }

    public Channel getMainChannel() {
        return mainChannel;
    }

    public Channel getPmChannel() {
        return pmChannel;
    }

    private void connect() {
        try {
            irc.connect();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NickNameException e) {
            irc.setNick(irc.getState().getClient().getNick() + "_");
        } catch (PasswordException e) {

        }
    }

    public void commandHandler(Channel channel, String[] data) {
        if (data[0].equals("list")) {
            Collection<String> players = new ArrayList<>();
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                players.add(getDisplayName(player));
            }

            channel.sendNotice("Tilkoblede spillere (" + players.size() + "): " + StringUtils.join(players, ", "));
        }
    }

    public String getDisplayName(Player player) {
        String name = player.getDisplayName();

        // Replace the ones we know how to.
        for (ChatColor mcColor : colorsMcToIrc.keySet()) {
            name = name.replaceAll(mcColor.toString(), colorsMcToIrc.get(mcColor));
        }

        // Remove the rest.
        name = name.replaceAll(ChatColor.COLOR_CHAR + ".", "");

        return name + IrcColors.RESET;
    }
}

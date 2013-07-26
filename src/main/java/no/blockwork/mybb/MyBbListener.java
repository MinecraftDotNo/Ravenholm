package no.blockwork.mybb;

import no.blockwork.blockwork.Blockwork;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

class MyBbListener implements Listener {
    private final Blockwork plugin;
    private final MyBb mybb;

    public MyBbListener(Blockwork pluginInstance, MyBb mybbInstance) {
        plugin = pluginInstance;
        mybb = mybbInstance;
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();

        if (!mybb.isUserRegistered(player)) {
            mybb.setUserGroup(player, MyBb.Group.AWAITING_ACTIVATION);
        }

        if (mybb.isUserBanned(player)) {
            // TODO: Implement support for timed bans.
            MyBbBan data = mybb.getBanData(player);

            event.setResult(PlayerLoginEvent.Result.KICK_BANNED);
            event.setKickMessage("Du er ikke lenger velkommen her." + (data != null ? " Grunnlag: " + data.getReason() : ""));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        mybb.getUidCache().remove(event.getPlayer());
        mybb.getGidCache().remove(event.getPlayer());
    }
}

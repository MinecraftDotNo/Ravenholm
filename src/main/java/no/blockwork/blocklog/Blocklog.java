package no.blockwork.blocklog;

import no.blockwork.blocklog.tools.BlocklogLogTool;
import no.blockwork.blockwork.Blockwork;
import no.blockwork.tools.Tools;
import no.minecraft.BasePlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.sql.*;
import java.sql.Date;
import java.util.*;

public final class Blocklog implements BasePlugin {
    private final Blockwork plugin;

    private final HashMap<String, String> mapWhat;

    private final BlocklogListener blocklogListener;
    private final BlocklogLogTool blocklogLogTool;

    private PreparedStatement queryLog;
    private PreparedStatement queryGetLog;

    public Blocklog(final Blockwork pluginInstance) {
        plugin = pluginInstance;

        mapWhat = new HashMap<>();

        mapWhat.put("BLOCK_PLACE", "plasserte");
        mapWhat.put("BlockPlaceEvent", "plasserte");
        mapWhat.put("BLOCK_BREAK", "fjernet");
        mapWhat.put("BlockBreakEvent", "fjernet");

        mapWhat.put("protection.freeBlock", "fjernet beskyttelse");
        mapWhat.put("protection.setBlockOwner", "endret eier");
        mapWhat.put("protection.stick", "brukte pinnen");

        blocklogListener = new BlocklogListener(plugin, this);
        blocklogLogTool = new BlocklogLogTool(plugin, this);
    }

    @Override
    public void onEnable() {
        plugin.getServer().getPluginManager().registerEvents(blocklogListener, plugin);

        final Tools tools = plugin.getTools();
        tools.registerTool(Material.BOOK, blocklogLogTool);
        tools.registerTool(Material.LOCKED_CHEST, blocklogLogTool);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(blocklogListener);
    }

    public void prepareStatements(final Connection db) {
        try {
            queryLog = db.prepareStatement("INSERT INTO blocklog (time,world,x,y,z,who,what,extra) VALUES (?,?,?,?,?,?,?,?)");
            queryGetLog = db.prepareStatement("SELECT time,who,what,extra FROM blocklog WHERE world=? AND x=? AND y=? AND z=? ORDER BY time DESC LIMIT 5");
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void log(final Location location, final String who, final Event event, final String extra) {
        plugin.getDb();

        try {
            queryLog.setLong(1, System.currentTimeMillis() / 1000L);

            queryLog.setString(2, location.getWorld().getName());
            queryLog.setInt(3, (int) location.getX());
            queryLog.setInt(4, (int) location.getY());
            queryLog.setInt(5, (int) location.getZ());

            queryLog.setString(6, who);
            queryLog.setString(7, event != null ? event.getEventName() : "");

            queryLog.setString(8, extra.isEmpty() ? "" : extra);

            queryLog.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void log(final Location location, final String who, final Event event) {
        log(location, who, event, "");
    }

    public void log(final Location location, final Player player, final Event event, final String extra) {
        log(location, player.getName(), event, extra);
    }

    public void log(final Location location, final Player player, final Event event) {
        log(location, player, event, "");
    }

    public Collection<String> getLog(final Location location) {
        plugin.getDb();

        final List<String> log = new ArrayList<>();

        try {
            queryGetLog.setString(1, location.getWorld().getName());
            queryGetLog.setInt(2, (int) location.getX());
            queryGetLog.setInt(3, (int) location.getY());
            queryGetLog.setInt(4, (int) location.getZ());

            final ResultSet result = queryGetLog.executeQuery();

            while (result.next()) {
                final Date date = new Date(result.getLong(1) * 1000L);

                String what = result.getString(3);
                while (what.contains(".")) { // TODO: This is a bit ugly.
                    what = what.substring(what.indexOf(".") + 1);
                }
                if (mapWhat.containsKey(what)) {
                    what = mapWhat.get(what);
                }

                String extra = result.getString(4);
                if (mapWhat.containsKey(extra)) {
                    what = mapWhat.get(extra);
                    extra = "";
                } else {
                    extra = extra.toLowerCase().replace("_", " ");
                }

                log.add(date + ": " + result.getString(2) + " " + what + (extra.isEmpty() ? "" : " " + extra));
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        Collections.reverse(log);

        return log;
    }

    public Collection<String> getLog(Block block) {
        return getLog(block.getLocation());
    }
}

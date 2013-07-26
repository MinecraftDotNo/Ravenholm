package no.blockwork.protection;

import no.blockwork.blockwork.Blockwork;
import no.blockwork.groups.Group;
import no.blockwork.mybb.MyBb;
import no.blockwork.protection.commands.ProtectionChownCommand;
import no.blockwork.protection.commands.ProtectionFreeCommand;
import no.blockwork.protection.commands.ProtectionProtectCommand;
import no.blockwork.protection.tools.ProtectionFreeTool;
import no.blockwork.protection.tools.ProtectionStickTool;
import no.blockwork.tools.Tools;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Protection {
    private final Blockwork plugin;

    private final Map<Chunk, Map<Location, Integer>> cache;

    private PreparedStatement queryGetBlockOwner;
    private PreparedStatement querySetBlockOwner;
    private PreparedStatement queryFreeBlock;

    private final ProtectionListener protectionListener;

    private final ProtectionFreeTool protectionFreeTool;
    private final ProtectionStickTool protectionStickTool;

    private final ProtectionChownCommand protectionChownCommand;
    private final ProtectionFreeCommand protectionFreeCommand;
    private final ProtectionProtectCommand protectionProtectCommand;

    public Protection(final Blockwork pluginInstance) {
        plugin = pluginInstance;

        cache = new HashMap<>();

        protectionListener = new ProtectionListener(plugin, this);

        protectionFreeTool = new ProtectionFreeTool(plugin, this);
        protectionStickTool = new ProtectionStickTool(plugin, this);

        protectionChownCommand = new ProtectionChownCommand(plugin, this);
        protectionFreeCommand = new ProtectionFreeCommand(plugin, this);
        protectionProtectCommand = new ProtectionProtectCommand(plugin, this);
    }

    public void onEnable() {
        plugin.getServer().getPluginManager().registerEvents(protectionListener, plugin);

        plugin.getCommand("chown").setExecutor(protectionChownCommand);
        plugin.getCommand("free").setExecutor(protectionFreeCommand);
        plugin.getCommand("protect").setExecutor(protectionProtectCommand);

        final Tools tools = plugin.getTools();
        tools.registerTool(Material.PAPER, protectionFreeTool);
        tools.registerTool(Material.STICK, protectionStickTool);
    }

    public void onDisable() {
        plugin.getCommand("protect").setExecutor(null);
        plugin.getCommand("free").setExecutor(null);
        plugin.getCommand("chown").setExecutor(null);

        HandlerList.unregisterAll(protectionListener);
    }

    public void prepareStatements(final Connection db) {
        try {
            queryGetBlockOwner = db.prepareStatement("SELECT uid FROM protection WHERE world=? AND x=? AND y=? AND z=?");
            querySetBlockOwner = db.prepareStatement("REPLACE INTO protection(world,x,y,z,uid) VALUES (?,?,?,?,?)");
            queryFreeBlock = db.prepareStatement("DELETE FROM protection WHERE world=? AND x=? AND y=? AND z=?");
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public Map<Chunk, Map<Location, Integer>> getCache() {
        return cache;
    }

    public int getBlockOwner(final Location location) {
        if (location.getBlock().getType() == Material.AIR) {
            return 0;
        }

        final Chunk chunk = location.getChunk();

        if (cache.containsKey(chunk)) {
            final Map<Location, Integer> cacheChunk = cache.get(chunk);

            if (cacheChunk.containsKey(location)) {
                return cacheChunk.get(location);
            }
        }

        plugin.getDb();

        int owner = 0;

        try {
            queryGetBlockOwner.setString(1, location.getWorld().getName());
            queryGetBlockOwner.setInt(2, location.getBlockX());
            queryGetBlockOwner.setInt(3, location.getBlockY());
            queryGetBlockOwner.setInt(4, location.getBlockZ());

            ResultSet result = queryGetBlockOwner.executeQuery();

            if (result.next()) {
                owner = result.getInt("uid");
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        if (!cache.containsKey(chunk)) {
            cache.put(chunk, new HashMap<Location, Integer>());
        }
        cache.get(chunk).put(location, owner);

        return owner;
    }

    public int getBlockOwner(final Block block) {
        return getBlockOwner(block.getLocation());
    }

    public void setBlockOwner(final Block block, final int owner) {
        plugin.getDb();

        try {
            querySetBlockOwner.setString(1, block.getWorld().getName());
            querySetBlockOwner.setInt(2, block.getX());
            querySetBlockOwner.setInt(3, block.getY());
            querySetBlockOwner.setInt(4, block.getZ());
            querySetBlockOwner.setInt(5, owner);

            querySetBlockOwner.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        final Chunk chunk = block.getChunk();
        if (!cache.containsKey(chunk)) {
            cache.put(chunk, new HashMap<Location, Integer>());
        }
        cache.get(chunk).put(block.getLocation(), owner);
    }

    public void setBlockOwner(final Block block, final Player player) {
        setBlockOwner(block, plugin.getMyBb().getUserId(player));
    }

    public void freeBlock(final Location location) {
        plugin.getDb();

        try {
            queryFreeBlock.setString(1, location.getWorld().getName());
            queryFreeBlock.setInt(2, location.getBlockX());
            queryFreeBlock.setInt(3, location.getBlockY());
            queryFreeBlock.setInt(4, location.getBlockZ());

            queryFreeBlock.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        final Chunk chunk = location.getChunk();
        if (cache.containsKey(chunk)) {
            final Map<Location, Integer> cacheChunk = cache.get(chunk);

            cacheChunk.remove(location);

            if (cacheChunk.isEmpty()) {
                cache.remove(chunk);
            }
        }
    }

    public void freeBlock(final Block block) {
        freeBlock(block.getLocation());
    }

    public boolean canUse(final Block block, final Player player) {
        final MyBb mybb = plugin.getMyBb();

        if (mybb.isPowerUser(player)) {
            return true;
        }

        final int owner = getBlockOwner(block);

        if (owner == 0 || mybb.getUserId(player) == owner) {
            return true;
        }

        final Group group = plugin.getGroups().getGroup(player);

        return group != null && group.getUid() == owner;
    }
}

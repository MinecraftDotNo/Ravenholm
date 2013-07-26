package no.blockwork.mybb;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import no.blockwork.blockwork.Blockwork;
import no.blockwork.mybb.commands.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyBb {
    public enum Group {
        AWAITING_ACTIVATION(5),
        REGISTERED(2),
        MODERATOR(6),
        SUPER_MODERATOR(3),
        ADMINISTRATOR(4),
        BANNED(7);

        private int gid;

        private Group(int gid) {
            this.gid = gid;
        }

        public int getId() {
            return gid;
        }

        public static Group getById(int gid) {
            for (Group group : Group.values()) {
                if (group.getId() == gid) {
                    return group;
                }
            }

            return null;
        }
    }

    private final Blockwork plugin;

    private final MysqlDataSource dataSource;
    private Connection db;
    private Integer dbTaskId;

    private final HashMap<Player, Integer> uidCache;
    private final HashMap<Player, Integer> gidCache;

    private PreparedStatement queryInsertUser;
    private PreparedStatement querySetUserGroup;
    private PreparedStatement queryGetUserId;
    private PreparedStatement queryGetUserName;
    private PreparedStatement queryGetUserGroup;
    private PreparedStatement querySetBanData;
    private PreparedStatement queryGetBanData;
    private PreparedStatement queryAddFriend;
    private PreparedStatement queryRemoveFriend;
    private PreparedStatement queryHasFriend;
    private PreparedStatement queryGetFriends;

    private final MyBbListener myBbListener;

    private final MyBbKickCommand myBbKickCommand;
    private final MyBbBanCommand myBbBanCommand;
    private final MyBbGuestCommand myBbGuestCommand;
    private final MyBbUserCommand myBbUserCommand;
    private final MyBbModCommand myBbModCommand;
    private final MyBbAdminCommand myBbAdminCommand;
    private final MyBbRegCommand myBbRegCommand;
    private final MyBbFriendCommand myBbFriendCommand;

    public MyBb(Blockwork pluginInstance) {
        plugin = pluginInstance;

        dataSource = new MysqlDataSource();

        uidCache = new HashMap<>();
        gidCache = new HashMap<>();

        myBbListener = new MyBbListener(plugin, this);

        myBbKickCommand = new MyBbKickCommand(plugin, this);
        myBbBanCommand = new MyBbBanCommand(plugin, this);
        myBbGuestCommand = new MyBbGuestCommand(plugin, this);
        myBbUserCommand = new MyBbUserCommand(plugin, this);
        myBbModCommand = new MyBbModCommand(plugin, this);
        myBbAdminCommand = new MyBbAdminCommand(plugin, this);
        myBbRegCommand = new MyBbRegCommand(plugin, this);
        myBbFriendCommand = new MyBbFriendCommand(plugin, this);
    }

    public void onEnable() {
        FileConfiguration config = plugin.getConfig();

        dataSource.setServerName(config.getString("mybb.host"));
        dataSource.setPort(config.getInt("mybb.port"));
        dataSource.setDatabaseName(config.getString("mybb.schema"));
        dataSource.setUser(config.getString("mybb.user"));
        dataSource.setPassword(config.getString("mybb.password"));

        dbTaskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            public void run() {
                try {
                    getDb().prepareStatement("SELECT 1").execute();
                } catch (SQLException exception) {
                    plugin.getLogger().info("MyBB database disconnected.");
                }
            }
        }, 200, 200);

        plugin.getServer().getPluginManager().registerEvents(myBbListener, plugin);

        plugin.getCommand("kick").setExecutor(myBbKickCommand);
        plugin.getCommand("ban").setExecutor(myBbBanCommand);
        plugin.getCommand("guest").setExecutor(myBbGuestCommand);
        plugin.getCommand("user").setExecutor(myBbUserCommand);
        plugin.getCommand("mod").setExecutor(myBbModCommand);
        plugin.getCommand("admin").setExecutor(myBbAdminCommand);
        plugin.getCommand("reg").setExecutor(myBbRegCommand);
        plugin.getCommand("friend").setExecutor(myBbFriendCommand);
    }

    public void onDisable() {
        plugin.getServer().getScheduler().cancelTask(dbTaskId);

        plugin.getCommand("friend").setExecutor(null);
        plugin.getCommand("reg").setExecutor(null);
        plugin.getCommand("admin").setExecutor(null);
        plugin.getCommand("mod").setExecutor(null);
        plugin.getCommand("user").setExecutor(null);
        plugin.getCommand("guest").setExecutor(null);
        plugin.getCommand("ban").setExecutor(null);
        plugin.getCommand("kick").setExecutor(null);

        HandlerList.unregisterAll(myBbListener);

        try {
            if (db != null && !db.isClosed()) {
                db.close();
                db = null;
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public Connection getDb() {
        try {
            if (db == null || db.isClosed()) {
                db = dataSource.getConnection();

                prepareStatements();
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return db;
    }

    private void prepareStatements() {
        try {
            queryInsertUser = db.prepareStatement("INSERT INTO mybb_users (username,usergroup) VALUES (?,?)");
            querySetUserGroup = db.prepareStatement("UPDATE mybb_users SET usergroup=? WHERE username=?");
            queryGetUserId = db.prepareStatement("SELECT uid FROM mybb_users WHERE username LIKE ?");
            queryGetUserName = db.prepareStatement("SELECT username FROM mybb_users WHERE uid=?");
            queryGetUserGroup = db.prepareStatement("SELECT usergroup FROM mybb_users WHERE uid=?");
            querySetBanData = db.prepareStatement("INSERT INTO mybb_banned (uid,gid,oldgroup,admin,dateline,reason) VALUES (?,?,?,?,?,?)");
            queryGetBanData = db.prepareStatement("SELECT admin,dateline,bantime,lifted,reason FROM mybb_banned WHERE uid=? ORDER BY dateline DESC");
            queryAddFriend = db.prepareStatement("INSERT INTO mybb_friends (user_id,friend_id) VALUES (?,?)");
            queryRemoveFriend = db.prepareStatement("DELETE FROM mybb_friends WHERE user_id=? AND friend_id=?");
            queryHasFriend = db.prepareStatement("SELECT 1 FROM mybb_friends WHERE user_id=? AND friend_id=?");
            queryGetFriends = db.prepareStatement("SELECT friend_id FROM mybb_friends WHERE user_id=?");
        } catch (SQLException exception) {
            exception.printStackTrace();
        } catch (NullPointerException exception) {
            plugin.getLogger().info("Could not connect to MyBB database!");
            plugin.getServer().shutdown();
        }
    }

    public Map<Player, Integer> getUidCache() {
        return uidCache;
    }

    public Map<Player, Integer> getGidCache() {
        return gidCache;
    }

    public boolean setUserGroup(String username, Group group) {
        Player player = plugin.getServer().getPlayer(username);
        if (player != null) {
            gidCache.remove(player);
        }

        getDb();

        if (getUserId(username) == 0) {
            try {
                queryInsertUser.setString(1, username);
                queryInsertUser.setInt(2, group.getId());

                if (queryInsertUser.executeUpdate() == 1) {
                    return true;
                }
            } catch (SQLException exception) {
                exception.printStackTrace();
                return false;
            }
        } else {
            try {
                querySetUserGroup.setInt(1, group.getId());
                querySetUserGroup.setString(2, username);

                if (querySetUserGroup.executeUpdate() == 1) {
                    return true;
                }
            } catch (SQLException exception) {
                exception.printStackTrace();
                return false;
            }
        }

        return false;
    }

    public boolean setUserGroup(Player player, Group group) {
        return setUserGroup(player.getName(), group);
    }

    public int getUserId(String name) {
        getDb();

        try {
            queryGetUserId.setString(1, name);

            ResultSet result = queryGetUserId.executeQuery();

            if (result.next()) {
                return result.getInt("uid");
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return 0;
    }

    public int getUserId(Player player) {
        if (uidCache.containsKey(player)) {
            return uidCache.get(player);
        }

        int uid = getUserId(player.getName());

        if (uid != 0) {
            uidCache.put(player, uid);
        }

        return uid;
    }

    public Group getUserGroup(int uid) {
        getDb();

        try {
            queryGetUserGroup.setInt(1, uid);

            ResultSet result = queryGetUserGroup.executeQuery();

            if (result.next()) {
                return Group.getById(result.getInt("usergroup"));
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return null;
    }

    public Group getUserGroup(Player player) {
        if (gidCache.containsKey(player)) {
            return Group.getById(gidCache.get(player));
        }

        Group group = getUserGroup(getUserId(player));

        if (isActivatedGroup(group)) {
            gidCache.put(player, group.getId());
        }

        return group;
    }

    public String getUserName(int uid) {
        getDb();

        try {
            queryGetUserName.setInt(1, uid);

            ResultSet result = queryGetUserName.executeQuery();

            if (result.next()) {
                return result.getString("username");
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return null;
    }

    public Player getPlayer(int uid) {
        try {
            return plugin.getServer().getPlayer(getUserName(uid));
        } catch (NullPointerException exception) {
            // Oh well...
        }

        return null;
    }

    public boolean isActivatedGroup(Group group) {
        switch (group) {
            case REGISTERED:
            case SUPER_MODERATOR:
            case ADMINISTRATOR:
            case MODERATOR:
                return true;
        }

        return false;
    }

    public boolean isPowerGroup(Group group) {
        switch (group) {
            case SUPER_MODERATOR:
            case ADMINISTRATOR:
            case MODERATOR:
                return true;
        }

        return false;
    }

    public boolean isUserGuest(Player player) {
        return !isUserRegistered(player) || getUserGroup(player) == Group.AWAITING_ACTIVATION;
    }

    public boolean isUserRegistered(Player player) {
        return getUserId(player) != 0;
    }

    public boolean isUserActivated(Player player) {
        return isActivatedGroup(getUserGroup(player));
    }

    public boolean isPowerUser(Player player) {
        return player.isOp() || isPowerGroup(getUserGroup(player));
    }

    public MyBbBan getBanData(Player player) {
        int uid = getUserId(player);

        if (uid == 0) {
            return null;
        }

        try {
            queryGetBanData.setInt(1, uid);

            ResultSet result = queryGetBanData.executeQuery();

            if (result.next()) {
                return new MyBbBan(
                        uid,
                        result.getInt("admin"),
                        result.getInt("dateline"),
                        result.getString("bantime"),
                        result.getInt("lifted") == 1,
                        result.getString("reason")
                );
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return null;
    }

    public boolean banUser(String name, Player admin, String reason) {
        int uid = getUserId("%" + name + "%");

        if (uid == 0) {
            return false;
        }

        Group group = getUserGroup(uid);
        setUserGroup(name, Group.BANNED);

        try {
            querySetBanData.setInt(1, uid);
            querySetBanData.setInt(2, Group.BANNED.getId());
            querySetBanData.setInt(3, group.getId());
            querySetBanData.setInt(4, getUserId(admin));
            querySetBanData.setInt(5, (int) (System.currentTimeMillis() / 1000L));
            querySetBanData.setString(6, reason);

            if (querySetBanData.executeUpdate() == 1) {
                return true;
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return false;
    }

    public boolean isUserBanned(Player player) {
        return getUserGroup(player) == Group.BANNED;
    }

    public boolean addFriend(int user, int friend) {
        if (hasFriend(user, friend)) {
            return false;
        }

        try {
            queryAddFriend.setInt(1, user);
            queryAddFriend.setInt(2, friend);

            if (queryAddFriend.executeUpdate() == 1) {
                return true;
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return false;
    }

    public boolean removeFriend(int user, int friend) {
        try {
            queryRemoveFriend.setInt(1, user);
            queryRemoveFriend.setInt(2, friend);

            if (queryRemoveFriend.executeUpdate() == 1) {
                return true;
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return false;
    }

    public boolean hasFriend(int user, int friend) {
        try {
            queryHasFriend.setInt(1, user);
            queryHasFriend.setInt(2, friend);

            if (queryHasFriend.executeQuery().next()) {
                return true;
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return false;
    }

    public List<Integer> getFriends(int user) {
        List<Integer> friends = new ArrayList();

        try {
            queryGetFriends.setInt(1, user);

            ResultSet result = queryGetFriends.executeQuery();

            while (result.next()) {
                friends.add(result.getInt("friend_id"));
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return friends;
    }
}

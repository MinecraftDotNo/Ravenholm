package no.blockwork.blockwork;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import no.blockwork.antiafk.AntiAfk;
import no.blockwork.blocklog.Blocklog;
import no.blockwork.bugs.Bugs;
import no.blockwork.chat.Chat;
import no.blockwork.groups.Groups;
import no.blockwork.irc.Irc;
import no.blockwork.lumberjack.Lumberjack;
import no.blockwork.mybb.MyBb;
import no.blockwork.portals.Portals;
import no.blockwork.protection.Protection;
import no.blockwork.pvp.Pvp;
import no.blockwork.railways.Railways;
import no.blockwork.tools.Tools;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class Blockwork extends JavaPlugin {
    private MysqlDataSource dataSource;
    private Connection db;
    private Integer dbTaskId;

    private Tools tools;
    private MyBb mybb;
    private Groups groups;
    private Blocklog blocklog;
    private Protection protection;
    private Irc irc;
    private Chat chat;
    private Railways railways;
    private Lumberjack lumberjack;
    private Bugs bugs;
    private Pvp pvp;
    private Portals portals;
    private AntiAfk antiAfk;

    private WorldEditPlugin worldEdit;

    private BlockworkListener blockworkListener;

    public Blockwork() {

    }

    public void onEnable() {
        saveDefaultConfig();
        FileConfiguration config = getConfig();
        config.options().copyDefaults(true);
        saveConfig();
        reloadConfig();

        dataSource = new MysqlDataSource();

        dataSource.setServerName(config.getString("mysql.host"));
        dataSource.setPort(config.getInt("mysql.port"));
        dataSource.setDatabaseName(config.getString("mysql.schema"));
        dataSource.setUser(config.getString("mysql.user"));
        dataSource.setPassword(config.getString("mysql.password"));

        tools = config.getBoolean("tools.enabled") ? new Tools(this) : null;
        mybb = config.getBoolean("mybb.enabled") ? new MyBb(this) : null;
        groups = config.getBoolean("groups.enabled") ? new Groups(this) : null;
        blocklog = config.getBoolean("blocklog.enabled") ? new Blocklog(this) : null;
        protection = config.getBoolean("protection.enabled") ? new Protection(this) : null;
        irc = config.getBoolean("irc.enabled") ? new Irc(this) : null;
        chat = config.getBoolean("chat.enabled") ? new Chat(this) : null;
        railways = config.getBoolean("railways.enabled") ? new Railways(this) : null;
        lumberjack = config.getBoolean("lumberjack.enabled") ? new Lumberjack(this) : null;
        bugs = config.getBoolean("bugs.enabled") ? new Bugs(this) : null;
        pvp = config.getBoolean("pvp.enabled") ? new Pvp(this) : null;
        portals = config.getBoolean("portals.enabled") ? new Portals(this) : null;
        antiAfk = config.getBoolean("antiafk.enabled") ? new AntiAfk(this) : null;

        getDb();

        dbTaskId = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                try {
                    getDb().prepareStatement("SELECT 1").execute();
                } catch (SQLException exception) {
                    getLogger().info("Server database disconnected.");
                }
            }
        }, 200, 200);

        blockworkListener = new BlockworkListener(this);

        worldEdit = (WorldEditPlugin) getServer().getPluginManager().getPlugin("WorldEdit");

        if (tools != null) {
            tools.onEnable();
        }
        if (mybb != null) {
            mybb.onEnable();
        }
        if (groups != null) {
            groups.onEnable();
        }
        if (blocklog != null) {
            blocklog.onEnable();
        }
        if (protection != null) {
            protection.onEnable();
        }
        if (irc != null) {
            irc.onEnable();
        }
        if (chat != null) {
            chat.onEnable();
        }
        if (railways != null) {
            railways.onEnable();
        }
        if (lumberjack != null) {
            lumberjack.onEnable();
        }
        if (bugs != null) {
            bugs.onEnable();
        }
        if (pvp != null) {
            pvp.onEnable();
        }
        if (portals != null) {
            portals.onEnable();
        }
        if (antiAfk != null) {
            antiAfk.onEnable();
        }

        getServer().getPluginManager().registerEvents(blockworkListener, this);
    }

    public void onDisable() {
        getServer().getScheduler().cancelTask(dbTaskId);

        HandlerList.unregisterAll(blockworkListener);

        if (antiAfk != null) {
            antiAfk.onDisable();
        }
        if (portals != null) {
            portals.onDisable();
        }
        if (pvp != null) {
            pvp.onDisable();
        }
        if (bugs != null) {
            bugs.onDisable();
        }
        if (lumberjack != null) {
            lumberjack.onDisable();
        }
        if (railways != null) {
            railways.onDisable();
        }
        if (chat != null) {
            chat.onDisable();
        }
        if (irc != null) {
            irc.onDisable();
        }
        if (protection != null) {
            protection.onDisable();
        }
        if (blocklog != null) {
            blocklog.onDisable();
        }
        if (groups != null) {
            groups.onDisable();
        }
        if (mybb != null) {
            mybb.onDisable();
        }
        if (tools != null) {
            tools.onDisable();
        }

        List<RegisteredListener> listeners = HandlerList.getRegisteredListeners(this);
        if (listeners.size() != 0) {
            for (RegisteredListener listener : listeners) {
                getLogger().info(listener.getListener() + " was not unregistered by own package!");
            }
            HandlerList.unregisterAll(this);
        }

        List<BukkitTask> tasks = getServer().getScheduler().getPendingTasks();
        if (tasks.size() != 0) {
            for (BukkitTask task : tasks) {
                getLogger().info(task + " was not cancelled by own package!");
            }
            getServer().getScheduler().cancelTasks(this);
        }

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

                blocklog.prepareStatements(db);
                protection.prepareStatements(db);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        } catch (NullPointerException exception) {

        }

        return db;
    }

    public Tools getTools() {
        return tools;
    }

    public MyBb getMyBb() {
        return mybb;
    }

    public Groups getGroups() {
        return groups;
    }

    public Blocklog getBlocklog() {
        return blocklog;
    }

    public Protection getProtection() {
        return protection;
    }

    public Irc getIrc() {
        return irc;
    }

    public Chat getChat() {
        return chat;
    }

    public Railways getRailways() {
        return railways;
    }

    public Lumberjack getLumberjack() {
        return lumberjack;
    }

    public Bugs getBugs() {
        return bugs;
    }

    public Pvp getPvp() {
        return pvp;
    }

    public Portals getPortals() {
        return portals;
    }

    public AntiAfk getAntiAfk() {
        return antiAfk;
    }

    public WorldEditPlugin getWorldEdit() {
        return worldEdit;
    }
}

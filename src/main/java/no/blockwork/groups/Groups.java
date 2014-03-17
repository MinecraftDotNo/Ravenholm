package no.blockwork.groups;

import no.blockwork.blockwork.Blockwork;
import no.blockwork.groups.commands.GroupsGroupCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.util.HashMap;
import java.util.Map;

public class Groups {
	private final Blockwork plugin;

	private final Map<Integer,Group> groups;
    private final Map<Player,Boolean> chat;

	private final GroupsListener groupsListener;
	private final GroupsGroupCommand groupsGroupCommand;

	public Groups(Blockwork pluginInstance) {
		plugin = pluginInstance;

		groups = new HashMap<>();
        chat = new HashMap<>();

		groupsListener = new GroupsListener(plugin,this);
		groupsGroupCommand = new GroupsGroupCommand(plugin,this);
	}

	public void onEnable() {
		plugin.getServer().getPluginManager().registerEvents(groupsListener,plugin);

		plugin.getCommand("group").setExecutor(groupsGroupCommand);
	}

	public void onDisable() {
		plugin.getCommand("group").setExecutor(null);

		HandlerList.unregisterAll(groupsListener);
	}

	public Group createGroup(int uid) {
		Group group = new Group(uid);

		groups.put(uid,group);

		return group;
	}

	public Group createGroup(Player player) {
		return createGroup(plugin.getMyBb().getUserId(player));
	}

	public void removeGroup(Group group) {
		groups.remove(group.getUid());
	}

	public Group getOwnGroup(int uid) {
		if (groups.containsKey(uid)) {
			return groups.get(uid);
		}

		return createGroup(uid);
	}

	public Group getGroup(Player player) {
		// TODO: Cache member -> group relations for faster lookups?
		for (Group group : groups.values()) {
			if (group.hasMember(player)) {
				return group;
			}
		}

		return getOwnGroup(plugin.getMyBb().getUserId(player));
	}

	public Group getInvite(Player player) {
		for (Group group : groups.values()) {
			if (group.hasInvite(player)) {
				return group;
			}
		}

		return null;
	}

    public void setChat(Player player,boolean status) {
        if (status) {
            this.chat.put(player,true);
        } else {
            this.chat.remove(player);
        }
    }

    public boolean getChat(Player player) {
        return this.chat.containsKey(player) ? true : false;
    }
}

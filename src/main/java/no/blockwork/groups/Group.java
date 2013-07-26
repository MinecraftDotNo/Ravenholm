package no.blockwork.groups;

import no.blockwork.blockwork.Blockwork;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Group {
    private final Blockwork plugin;
    private final int uid;
    private final List<Player> members;
    private final List<Player> invites;

    public Group(int _uid) {
        plugin = (Blockwork) Bukkit.getServer().getPluginManager().getPlugin("Blockwork");

        uid = _uid;

        members = new ArrayList<>();
        invites = new ArrayList<>();
    }

    public int getUid() {
        return uid;
    }

    public String getName() {
        return plugin.getMyBb().getUserName(uid);
    }

    public Player getOwner() {
        return plugin.getMyBb().getPlayer(uid);
    }

    public void addMember(Player player) {
        members.add(player);
    }

    public void removeMember(Player player) {
        members.remove(player);
    }

    public boolean hasMember(Player player) {
        return members.contains(player);
    }

    public List<Player> getMembers() {
        return members;
    }

    public void addInvite(Player player) {
        invites.add(player);
    }

    public void removeInvite(Player player) {
        invites.remove(player);
    }

    public boolean hasInvite(Player player) {
        return invites.contains(player);
    }

    public Collection<Player> getInvites() {
        return invites;
    }

    public void broadcastMessage(String message) {
        Player owner = getOwner();
        if (owner != null) {
            owner.sendMessage(message);
        }

        for (Player player : members) {
            player.sendMessage(message);
        }
    }
}

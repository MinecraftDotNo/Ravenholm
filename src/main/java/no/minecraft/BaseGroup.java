package no.minecraft;

import java.util.Collection;

public interface BaseGroup {
    public void setOwner();

    public BasePlayer getOwner();

    public void addPlayer(BasePlayer player);

    public void removePlayer(BasePlayer player);

    public Collection<BasePlayer> getMembers();

    public void invitePlayer(BasePlayer player);

    public void removeInvite(BasePlayer player);

    public Collection<BasePlayer> getInvites();
}

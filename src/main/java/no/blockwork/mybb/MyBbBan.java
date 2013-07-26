package no.blockwork.mybb;

public class MyBbBan {
    private final int uid;
    private final int admin;
    private final int dateline;
    private final String bantime;
    private final boolean lifted;
    private final String reason;

    public MyBbBan(int uid, int admin, int dateline, String bantime, boolean lifted, String reason) {
        this.uid = uid;
        this.admin = admin;
        this.dateline = dateline;
        this.bantime = bantime;
        this.lifted = lifted;
        this.reason = reason;
    }

    public int getUid() {
        return uid;
    }

    public int getAdmin() {
        return admin;
    }

    public int getDateline() {
        return dateline;
    }

    public String getBantime() {
        return bantime;
    }

    public boolean getLifted() {
        return lifted;
    }

    public String getReason() {
        return reason;
    }
}

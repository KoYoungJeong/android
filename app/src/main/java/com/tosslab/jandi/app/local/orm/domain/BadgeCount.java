package com.tosslab.jandi.app.local.orm.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by tonyjs on 15. 9. 16..
 */
@DatabaseTable(tableName = "badge_count")
public class BadgeCount {
    @DatabaseField(id = true)
    private long teamId;
    @DatabaseField
    private int badgeCount;

    public BadgeCount() {
    }

    public BadgeCount(long teamId, int badgeCount) {
        this.teamId = teamId;
        this.badgeCount = badgeCount;
    }

    public long getTeamId() {
        return teamId;
    }

    public void setTeamId(long teamId) {
        this.teamId = teamId;
    }

    public int getBadgeCount() {
        return badgeCount;
    }

    public void setBadgeCount(int badgeCount) {
        this.badgeCount = badgeCount;
    }

    @Override
    public String toString() {
        return "BadgeCount{" +
                "teamId=" + teamId +
                ", badgeCount=" + badgeCount +
                '}';
    }
}

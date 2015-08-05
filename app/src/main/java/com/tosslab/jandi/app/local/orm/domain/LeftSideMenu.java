package com.tosslab.jandi.app.local.orm.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by Steve SeongUg Jung on 15. 7. 21..
 */
@DatabaseTable(tableName = "left_raw")
public class LeftSideMenu {

    @DatabaseField(id = true)
    private int teamId;
    @DatabaseField
    private String rawLeftSideMenu;

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int team) {
        this.teamId = team;
    }

    public String getRawLeftSideMenu() {
        return rawLeftSideMenu;
    }

    public void setRawLeftSideMenu(String rawLeftSideMenu) {
        this.rawLeftSideMenu = rawLeftSideMenu;
    }

}

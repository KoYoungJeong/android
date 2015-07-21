package com.tosslab.jandi.app.local.orm.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.tosslab.jandi.app.network.models.ResAccountInfo;

/**
 * Created by Steve SeongUg Jung on 15. 7. 21..
 */
@DatabaseTable(tableName = "left_raw")
public class LeftSideMenu {

    @DatabaseField(generatedId = true)
    private long _id;

    @DatabaseField(foreign = true, unique = true)
    private ResAccountInfo.UserTeam team;
    @DatabaseField
    private String rawLeftSideMenu;

    public ResAccountInfo.UserTeam getTeam() {
        return team;
    }

    public void setTeam(ResAccountInfo.UserTeam team) {
        this.team = team;
    }

    public String getRawLeftSideMenu() {
        return rawLeftSideMenu;
    }

    public void setRawLeftSideMenu(String rawLeftSideMenu) {
        this.rawLeftSideMenu = rawLeftSideMenu;
    }

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }
}

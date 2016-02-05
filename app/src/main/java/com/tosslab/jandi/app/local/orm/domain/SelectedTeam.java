package com.tosslab.jandi.app.local.orm.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by Steve SeongUg Jung on 15. 7. 20..
 */
@DatabaseTable(tableName = "selected_team")
public class SelectedTeam {
    public static final long DEFAULT_ID = 1;
    @DatabaseField(id = true)
    private long _id = DEFAULT_ID;

    @DatabaseField
    private long selectedTeamId = -1;

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public long getSelectedTeamId() {
        return selectedTeamId;
    }

    public void setSelectedTeamId(long selectedTeamId) {
        this.selectedTeamId = selectedTeamId;
    }
}

package com.tosslab.jandi.app.network.models.start;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.tosslab.jandi.app.local.orm.persister.DateConverter;
import com.vimeo.stag.GsonAdapterKey;

import java.util.Date;

/**
 * Created by tee on 2017. 5. 31..
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@DatabaseTable(tableName = "initial_absence_info")
public class Absence {

    @DatabaseField(id = true)
    long id = 0;

    @DatabaseField
    @GsonAdapterKey
    String status;

    @DatabaseField
    @GsonAdapterKey
    String applyStatus;

    @DatabaseField
    @GsonAdapterKey
    boolean disablePush;

    @DatabaseField
    @GsonAdapterKey
    String message;

    @DatabaseField(persisterClass = DateConverter.class)
    @GsonAdapterKey
    Date startAt;

    @DatabaseField(persisterClass = DateConverter.class)
    @GsonAdapterKey
    Date endAt;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getApplyStatus() {
        return applyStatus;
    }

    public void setApplyStatus(String applyStatus) {
        this.applyStatus = applyStatus;
    }

    public boolean isDisablePush() {
        return disablePush;
    }

    public void setDisablePush(boolean disablePush) {
        this.disablePush = disablePush;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getStartAt() {
        return startAt;
    }

    public void setStartAt(Date startAt) {
        this.startAt = startAt;
    }

    public Date getEndAt() {
        return endAt;
    }

    public void setEndAt(Date endAt) {
        this.endAt = endAt;
    }
}

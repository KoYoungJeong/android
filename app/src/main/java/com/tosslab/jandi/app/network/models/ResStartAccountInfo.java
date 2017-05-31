package com.tosslab.jandi.app.network.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.tosslab.jandi.app.local.orm.persister.DateConverter;

import java.util.Date;

/**
 * Created by tee on 2017. 5. 29..
 */

// /start-api/account 응답 값
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ResStartAccountInfo {

    private Absence absence;

    public Absence getAbsence() {
        return absence;
    }

    public void setAbsence(Absence absence) {
        this.absence = absence;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @DatabaseTable(tableName = "initial_absence_info")
    public static class Absence {
        @DatabaseField(id = true)
        private long id = 0;
        @DatabaseField
        private String status;
        @DatabaseField
        private String applyStatus;
        @DatabaseField
        private boolean disablePush;
        @DatabaseField
        private String message;
        @DatabaseField(persisterClass = DateConverter.class)
        private Date startAt;
        @DatabaseField(persisterClass = DateConverter.class)
        private Date endAt;

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
}

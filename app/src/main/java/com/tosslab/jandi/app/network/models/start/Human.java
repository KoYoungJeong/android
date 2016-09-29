package com.tosslab.jandi.app.network.models.start;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.tosslab.jandi.app.local.orm.dao.HumanDaoImpl;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@DatabaseTable(tableName = "initial_info_human", daoClass = HumanDaoImpl.class)
public class Human {
    @DatabaseField(id = true)
    private long id;
    @DatabaseField
    private long teamId;
    @DatabaseField
    private String name;
    @DatabaseField
    private String type;
    @DatabaseField
    private String photoUrl;
    @DatabaseField
    private String role;
    @DatabaseField
    private String accountId;
    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private Profile profile;
    @DatabaseField
    private String status;
    @DatabaseField
    private boolean isStarred;
    @JsonIgnore
    @DatabaseField(foreign = true)
    private InitialInfo initialInfo;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTeamId() {
        return teamId;
    }

    public void setTeamId(long teamId) {
        this.teamId = teamId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public InitialInfo getInitialInfo() {
        return initialInfo;
    }

    public void setInitialInfo(InitialInfo initialInfo) {
        this.initialInfo = initialInfo;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isStarred() {
        return isStarred;
    }

    public void setIsStarred(boolean starred) {
        isStarred = starred;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @DatabaseTable(tableName = "initial_info_human_profile")
    public static class Profile {
        @JsonIgnore
        @DatabaseField(id = true)
        private long _id;
        @DatabaseField
        private String email;
        @DatabaseField
        private String position;
        @DatabaseField
        private String department;
        @DatabaseField
        private String phoneNumber;
        @DatabaseField
        private String statusMessage;
        @DatabaseField
        private boolean isUpdated;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }

        public String getDepartment() {
            return department;
        }

        public void setDepartment(String department) {
            this.department = department;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getStatusMessage() {
            return statusMessage;
        }

        public void setStatusMessage(String statusMessage) {
            this.statusMessage = statusMessage;
        }

        public boolean isUpdated() {
            return isUpdated;
        }

        public void setIsUpdated(boolean updated) {
            isUpdated = updated;
        }

        public long get_id() {
            return _id;
        }

        public void set_id(long _id) {
            this._id = _id;
        }
    }
}

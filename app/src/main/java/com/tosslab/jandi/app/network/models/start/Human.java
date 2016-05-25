package com.tosslab.jandi.app.network.models.start;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@DatabaseTable(tableName = "initial_info_human")
public class Human {
    @DatabaseField(id = true)
    private long id;
    @DatabaseField
    private long teamId;
    @DatabaseField
    private String name;
    @DatabaseField
    private String photoUrl;
    @DatabaseField
    private String role;
    @DatabaseField
    private String accountId;
    @DatabaseField(foreign = true)
    private Profile profile;
    @DatabaseField
    private String status;
    @JsonIgnore
    @DatabaseField(foreign = true)
    private InitializeInfo initialInfo;


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

    public InitializeInfo getInitialInfo() {
        return initialInfo;
    }

    public void setInitialInfo(InitializeInfo initialInfo) {
        this.initialInfo = initialInfo;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @DatabaseTable(tableName = "initial_info_human_profile")
    public static class Profile {
        @JsonIgnore
        @DatabaseField(generatedId = true)
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
    }
}

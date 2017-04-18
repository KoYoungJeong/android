package com.tosslab.jandi.app.network.models;

import java.util.List;

/**
 * Created by tee on 2017. 4. 17..
 */

public class ResOnlineStatus {

    private List<Record> records;

    public List<Record> getRecords() {
        return records;
    }

    public void setRecords(List<Record> records) {
        this.records = records;
    }

    public static class Record {
        private long memberId;
        private String presence;

        public long getMemberId() {
            return memberId;
        }

        public void setMemberId(long memberId) {
            this.memberId = memberId;
        }

        public String getPresence() {
            return presence;
        }

        public void setPresence(String presence) {
            this.presence = presence;
        }
    }
}

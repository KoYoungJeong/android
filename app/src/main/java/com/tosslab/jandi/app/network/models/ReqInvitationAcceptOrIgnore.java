package com.tosslab.jandi.app.network.models;

/**
 * Created by Bill Minwook Heo on 15. 4. 27..
 */
public class ReqInvitationAcceptOrIgnore {

    private final String type;

    public ReqInvitationAcceptOrIgnore(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public enum Type {
        ACCEPT("accept"), DECLINE("decline");
        private final String type;

        private Type(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }
}

package com.tosslab.jandi.app.network.models;

/**
 * Created by Steve SeongUg Jung on 14. 12. 11..
 */
public class ReqInvitationConfirm {

    public enum Type {
        ACCEPT("accept"), DECLINE("decline"), CANCEL("cancel");
        private final String type;

        private Type(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }

    private String token;
    private String type;
    private String memberName;
    private String memberEmail;

    public ReqInvitationConfirm(String token, String type, String memberName, String memberEmail) {
        this.token = token;
        this.type = type;
        this.memberName = memberName;
        this.memberEmail = memberEmail;
    }

    public String getToken() {
        return token;
    }

    public String getType() {
        return type;
    }

    public String getMemberName() {
        return memberName;
    }

    public String getMemberEmail() {
        return memberEmail;
    }
}

package com.tosslab.jandi.lib.sprinkler.tracker.keys;

/**
 * Created by Steve SeongUg Jung on 15. 7. 2..
 */
public enum ScreenViewProperty {

    LoginPage("500"),
    AccountHome("501"),
    Topics("502"),
    Messages("504"),
    Files("505"),
    More("506"),
    Profile("507"),
    TeamMember("508"),
    Settings("509"),
    Help("510"),
    FileSearch("511"),
    MessageSearch("512"),
    ChatRoom("513"),
    Invite("514"),
    Participants("515");

    private final String propertyName;

    ScreenViewProperty(String propertyName) {

        this.propertyName = propertyName;
    }

    public String propertyName() {
        return propertyName;
    }
}

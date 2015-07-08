package com.tosslab.jandi.lib.sprinkler.domain.property.value;

/**
 * Created by tonyjs on 15. 7. 8..
 */
public enum ScreenViewValue implements PropertyValue {
    LogInPage("500"),
    AccountHome("501"),
    Topics("502"),
    Messages("504"),
    Files("505"),
    More("506"),
    MoreProfile("507"),
    MoreTeamMember("508"), MoreSettings("509"), MoreHelp("510"),
    FileSearch("511"),
    MessageSearch("512"),
    ChatRoom("513"), ChatRoomInvite("514"), ChatRoomParticipants("515");

    private final String propertyName;

    ScreenViewValue(String propertyName) {
        this.propertyName = propertyName;
    }

    @Override
    public String getValue() {
        return propertyName;
    }
}

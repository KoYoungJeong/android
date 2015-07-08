package com.tosslab.jandi.lib.sprinkler.domain;

/**
 * <a href="http://wiki.tosslab.com/display/SPRK/Android">Android#Event</a>
 */
public enum Event {
    SignIn("e6"),
    SignOut("e12"),
    AccountNameChange("e16"),
    AccountEmailChange("e18"),
    ccountEmailAdd("e19"),
    TopicCreate("e31"),
    TopicStarred("e33"),
    TopicUnstarred("e34"),
    TopicLeave("e36"),
    FileUpload("e38"),
    FileDownload("e39"),
    FileShare("e40"),
    FileUnshare("e41"),
    FileDelete("e42"),
    SearchFile("e43"),
    ChatSend("e44"),
    SearchChat("e45"),
    ChatDelete("e46"),
    ChatInvite("e47"),
    TopicJoin("e49"),

    ScreenView("e48");

    private final String eventName;

    Event(String eventName) {
        this.eventName = eventName;
    }

    public String eventName() {
        return eventName;
    }
}

package com.tosslab.jandi.lib.sprinkler.constant.property;

/**
 * Created by tonyjs on 15. 7. 22..
 */
public enum PropertyKey {

    ResponseSuccess("p13"),
    ErrorCode("p14"),
    AutoSignIn("p25"),
    Email("p19"),
    TeamId("p20"),
    MemberId("p21"),
    TopicId("p22"),
    FileId("p23"),
    SearchKeyword("p24"),
    MessageId("p26"),
    ScreenView("p27"),

    // Default Property
    AppVersion("p29"),
    Brand("p30"),
    Manufacturer("p32"),
    Model("p33"),
    OS("p34"),
    OSVersion("p35"),
    ScreenDPI("p36"),
    ScreenHeight("p37"),
    ScreenWidth("p38"),
    MemberCount("p39"),
    Carrier("p40"),
    Wifi("p41"),
    GooglePlayServices("p42"),
    PollId("p46"),
    PollCommentId("p46"),
    PollItemId("p47");

    String name;

    PropertyKey(String keyName) {
        name = keyName;
    }

    public String getName() {
        return name;
    }
}

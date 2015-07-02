package com.tosslab.jandi.lib.sprinkler.tracker.keys;

/**
 * Created by Steve SeongUg Jung on 15. 7. 2..
 */
public enum Property {
    AppVersion("p24"),
    Brand("p25"),
    Manufacture("p27"),
    Model("p28"),
    OSName("p29"),
    OSVersion("p30"),
    ScreenDPI("p31"),
    ScreenHeight("p32"),
    ScreenWidth("p33");

    private final String propertyName;

    Property(String propertyName) {

        this.propertyName = propertyName;
    }

    public String propertyName() {
        return propertyName;
    }
}

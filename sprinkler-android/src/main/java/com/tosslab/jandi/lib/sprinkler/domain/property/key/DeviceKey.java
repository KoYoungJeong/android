package com.tosslab.jandi.lib.sprinkler.domain.property.key;

/**
 * Created by tonyjs on 15. 7. 8..
 */
public enum DeviceKey implements PropertyKey {
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

    DeviceKey(String propertyName) {
        this.propertyName = propertyName;
    }

    @Override
    public String getName() {
        return propertyName;
    }
}

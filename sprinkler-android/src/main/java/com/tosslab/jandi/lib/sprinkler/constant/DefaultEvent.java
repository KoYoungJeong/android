package com.tosslab.jandi.lib.sprinkler.constant;

import com.tosslab.jandi.lib.sprinkler.io.domain.event.Event;

/**
 * Created by tonyjs on 2016. 7. 26..
 */
public class DefaultEvent {

    public static final Event AppOpen = Event.create("SystemRelatedEvent",
            "e50",
            new String[]{
                    DefaultPropertyKey.AppVersion,
                    DefaultPropertyKey.Brand,
                    DefaultPropertyKey.Manufacturer,
                    DefaultPropertyKey.Model,
                    DefaultPropertyKey.OS,
                    DefaultPropertyKey.OSVersion,
                    DefaultPropertyKey.ScreenDPI,
                    DefaultPropertyKey.ScreenHeight,
                    DefaultPropertyKey.ScreenWidth,
                    DefaultPropertyKey.Carrier,
                    DefaultPropertyKey.Wifi,
                    DefaultPropertyKey.GooglePlayServices});

    public static final Event AppClose = Event.create("SystemRelatedEvent",
            "e51",
            new String[]{});
}

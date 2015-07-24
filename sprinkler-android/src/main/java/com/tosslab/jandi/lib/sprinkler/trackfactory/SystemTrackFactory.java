package com.tosslab.jandi.lib.sprinkler.trackfactory;

import com.tosslab.jandi.lib.sprinkler.DefaultProperties;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

/**
 * Created by tonyjs on 15. 7. 22..
 */
public class SystemTrackFactory {

    public static FutureTrack getAppOpenTrack(DefaultProperties defaultProperties) {
        return new FutureTrack.Builder()
                .event(Event.AppOpen)
                .property(PropertyKey.AppVersion, defaultProperties.getAppVersion())
                .property(PropertyKey.Brand, defaultProperties.getDeviceBrand())
                .property(PropertyKey.Manufacturer, defaultProperties.getDeviceManufacturer())
                .property(PropertyKey.Model, defaultProperties.getDeviceModel())
                .property(PropertyKey.OS, defaultProperties.getOs())
                .property(PropertyKey.OSVersion, defaultProperties.getOsVersion())
                .property(PropertyKey.ScreenDPI, defaultProperties.getScreenDpi())
                .property(PropertyKey.ScreenHeight, defaultProperties.getScreenHeight())
                .property(PropertyKey.ScreenWidth, defaultProperties.getScreenWidth())
                .property(PropertyKey.Carrier, defaultProperties.getDeviceCarrier())
                .property(PropertyKey.Wifi, defaultProperties.isWifiEnabled())
                .property(PropertyKey.GooglePlayServices, defaultProperties.getGooglePlayAvailable())
                .build();
    }

    public static FutureTrack getAppCloseTrack() {
        return new FutureTrack.Builder()
                .event(Event.AppClose)
                .build();
    }

}

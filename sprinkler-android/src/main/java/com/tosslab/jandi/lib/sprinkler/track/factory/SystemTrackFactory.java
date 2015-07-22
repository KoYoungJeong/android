package com.tosslab.jandi.lib.sprinkler.track.factory;

import com.tosslab.jandi.lib.sprinkler.Config;
import com.tosslab.jandi.lib.sprinkler.model.event.Event;
import com.tosslab.jandi.lib.sprinkler.model.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.track.FutureTrack;

/**
 * Created by tonyjs on 15. 7. 22..
 */
public class SystemTrackFactory {

    public static FutureTrack getAppOpenTrack(Config config) {
        return new FutureTrack.Builder()
                .event(Event.AppOpen)
                .property(PropertyKey.AppVersion, config.getAppVersion())
                .property(PropertyKey.Brand, config.getDeviceBrand())
                .property(PropertyKey.Manufacturer, config.getDeviceManufacturer())
                .property(PropertyKey.Model, config.getDeviceModel())
                .property(PropertyKey.OS, config.getOs())
                .property(PropertyKey.OSVersion, config.getOsVersion())
                .property(PropertyKey.ScreenDPI, config.getScreenDpi())
                .property(PropertyKey.ScreenHeight, config.getScreenHeight())
                .property(PropertyKey.ScreenWidth, config.getScreenWidth())
                .property(PropertyKey.Carrier, config.getDeviceCarrier())
                .property(PropertyKey.Wifi, config.isWifiEnabled())
                .property(PropertyKey.GooglePlayServices, config.getGooglePlayAvailable())
                .build();
    }

    public static FutureTrack getAppCloseTrack() {
        return new FutureTrack.Builder()
                .event(Event.AppClose)
                .build();
    }

}

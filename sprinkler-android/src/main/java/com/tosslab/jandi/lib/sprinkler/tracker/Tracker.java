package com.tosslab.jandi.lib.sprinkler.tracker;

import android.content.Context;

import com.tosslab.jandi.lib.sprinkler.domain.EventProperty;
import com.tosslab.jandi.lib.sprinkler.tracker.device.DeviceCollector;
import com.tosslab.jandi.lib.sprinkler.tracker.device.DeviceCollectorImpl;
import com.tosslab.jandi.lib.sprinkler.tracker.identify.IdentifyCollector;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 7. 2..
 */
public class Tracker {

    private IdentifyCollector identifyCollector;
    private DeviceCollector deviceCollector;
    private Context context;

    public Tracker(Context context) {

        this.context = context;
        this.deviceCollector = new DeviceCollectorImpl();
    }

    public void setIdentifyCollector(IdentifyCollector identifyCollector) {
        this.identifyCollector = identifyCollector;
    }


    private List<EventProperty> getDeviceProperties(Context context) {
        if (deviceCollector != null) {
            return deviceCollector.getDeviceProperties(context);
        } else {
            return new ArrayList<EventProperty>();
        }
    }

}

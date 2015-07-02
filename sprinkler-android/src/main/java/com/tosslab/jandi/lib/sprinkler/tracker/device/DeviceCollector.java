package com.tosslab.jandi.lib.sprinkler.tracker.device;

import android.content.Context;

import com.tosslab.jandi.lib.sprinkler.domain.EventProperty;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 7. 2..
 */
public interface DeviceCollector {

    List<EventProperty> getDeviceProperties(Context context);

}

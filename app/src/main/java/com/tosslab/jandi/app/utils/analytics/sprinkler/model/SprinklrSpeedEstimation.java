package com.tosslab.jandi.app.utils.analytics.sprinkler.model;

import com.tosslab.jandi.app.utils.analytics.sprinkler.PropertyKey;
import com.tosslab.jandi.app.utils.analytics.sprinkler.SprinklerEvents;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.Properties.IPerformanceTarget;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.Properties.IStartFinishFlag;

/**
 * Created by tee on 2017. 3. 8..
 */

public class SprinklrSpeedEstimation extends MainSprinklrModel
        implements IPerformanceTarget, IStartFinishFlag {

    public SprinklrSpeedEstimation() {
        super(SprinklerEvents.SpeedEstimation, true, true);
    }

    public static void sendFailLog(int errorCode) {
        new SprinklrSpeedEstimation()
                .sendFail(errorCode);
    }

    public static void sendLog(String performanceTarget, int startFinishFlag) {
        new SprinklrSpeedEstimation()
                .setPerformanceTarget(performanceTarget)
                .setStartFinishFlag(startFinishFlag)
                .sendSuccess();
    }

    @Override
    public SprinklrSpeedEstimation setPerformanceTarget(String performanceTarget) {
        setProperty(PropertyKey.PerformanceTarget, performanceTarget);
        return this;
    }

    @Override
    public SprinklrSpeedEstimation setStartFinishFlag(int flag) {
        setProperty(PropertyKey.StartFinishFlag, flag);
        return this;
    }

}

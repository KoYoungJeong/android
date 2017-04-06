package com.tosslab.jandi.app.utils;

import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrSpeedEstimation;
import com.tosslab.jandi.app.utils.logger.LogUtil;

/**
 * Created by tee on 2017. 3. 8..
 */

public class SpeedEstimationUtil {

    private static boolean flagTopicEntered = false;
    private static boolean flagExecutionApp = false;
    private static boolean flagPushEntered = false;
    private static boolean flagMessageSending = false;

    public static void sendAnalyticsTopicEnteredStart() {
        if (TeamInfoLoader.getInstance() != null
                && TeamInfoLoader.getInstance().getTeamId() == 279) {
            flagTopicEntered = true;
            SprinklrSpeedEstimation.sendLog("enter_topic_1", 0);
            LogUtil.e("topic_enter_start");
        }
    }

    public static void sendAnalyticsTopicEnteredEndIfStarted() {
        if (flagTopicEntered) {
            LogUtil.e("topic_enter_end");
            SprinklrSpeedEstimation.sendLog("enter_topic_1", 1);
            flagTopicEntered = false;
        }
    }

    public static void sendAnalyticsExecutionAppStart() {
        if (TeamInfoLoader.getInstance() != null
                && TeamInfoLoader.getInstance().getTeamId() == 279) {
            flagExecutionApp = true;
            SprinklrSpeedEstimation.sendLog("init_jandi_1", 0);
            LogUtil.e("execution_app_start");
        }
    }

    public static void sendAnalyticsExecutionAppEndIfStarted() {
        if (flagExecutionApp) {
            LogUtil.e("execution_app_end");
            SprinklrSpeedEstimation.sendLog("init_jandi_1", 1);
            flagExecutionApp = false;
        }
    }

    public static void sendAnalyticsPushEnteredStart() {
        if (TeamInfoLoader.getInstance() != null
                && TeamInfoLoader.getInstance().getTeamId() == 279) {
            flagPushEntered = true;
            SprinklrSpeedEstimation.sendLog("enter_push_1", 0);
            LogUtil.e("push_enter_start");
        }
    }

    public static void sendAnalyticsPushEnteredEndIfStarted() {
        if (flagPushEntered) {
            LogUtil.e("push_enter_end");
            SprinklrSpeedEstimation.sendLog("enter_push_1", 1);
            flagPushEntered = false;
        }
    }

    public static void sendAnalyticsMessageSendingStart() {
        if (TeamInfoLoader.getInstance() != null
                && TeamInfoLoader.getInstance().getTeamId() == 279) {
            flagMessageSending = true;
            SprinklrSpeedEstimation.sendLog("send_message_1", 0);
            LogUtil.e("message_sending_start");
        }
    }

    public static void sendAnalyticsMessageSendingEndIfStarted() {
        if (flagMessageSending) {
            LogUtil.e("message_sending_end");
            SprinklrSpeedEstimation.sendLog("send_message_1", 1);
            flagMessageSending = false;
        }
    }

}

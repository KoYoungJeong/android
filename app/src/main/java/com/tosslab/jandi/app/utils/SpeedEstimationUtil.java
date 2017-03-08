package com.tosslab.jandi.app.utils;

/**
 * Created by tee on 2017. 3. 8..
 */

public class SpeedEstimationUtil {

    private static boolean flagTopicEntered = false;
    private static boolean flagExecutionApp = false;
    private static boolean flagPushEntered = false;
    private static boolean flagMessageSending = false;

    private static long startTopicEnteredTime = 0;
    private static long endTopicEnteredTime = 0;
    private static long startExecutionAppTime = 0;
    private static long endExecutionAppTime = 0;
    private static long startPushEnteredAppTime = 0;
    private static long endPushEnteredAppTime = 0;
    private static long startMessageSendingTime = 0;
    private static long endMessageSendingTime = 0;

    public static void setFlagTopicEnteredOn() {
        flagTopicEntered = true;
    }

    public static void setFlagExecutionAppOn() {
        flagExecutionApp = true;
    }

    public static void setFlagPushEnteredOn() {
        flagPushEntered = true;
    }

    public static void setFlagMessageSendingOn() {
        flagMessageSending = true;
    }

    public static void sendStartTopicEnteredTime(long startTopicEnteredTime) {
        SpeedEstimationUtil.startTopicEnteredTime = startTopicEnteredTime;
    }

    public static void setEndTopicEnteredTime(long endTopicEnteredTime) {
        SpeedEstimationUtil.endTopicEnteredTime = endTopicEnteredTime;
    }

    public static void setStartExecutionAppTime(long startExecutionAppTime) {
        SpeedEstimationUtil.startExecutionAppTime = startExecutionAppTime;
    }

    public static void setEndExecutionAppTime(long endExecutionAppTime) {
        SpeedEstimationUtil.endExecutionAppTime = endExecutionAppTime;
    }

    public static void setStartPushEnteredAppTime(long startPushEnteredAppTime) {
        SpeedEstimationUtil.startPushEnteredAppTime = startPushEnteredAppTime;
    }

    public static void setEndPushEnteredAppTime(long endPushEnteredAppTime) {
        SpeedEstimationUtil.endPushEnteredAppTime = endPushEnteredAppTime;
    }

    public static void setStartMessageSendingTime(long startMessageSendingTime) {
        SpeedEstimationUtil.startMessageSendingTime = startMessageSendingTime;
    }

    public static void setEndMessageSendingTime(long endMessageSendingTime) {
        SpeedEstimationUtil.endMessageSendingTime = endMessageSendingTime;
    }

    public static void sendTopicEnteredLogIfFlagOn() {
        if (flagTopicEntered) {

            flagTopicEntered = false;
        }

    }

    public static void sendExecutionAppLogIfFlagOn() {
        if (flagExecutionApp) {

            flagExecutionApp = false;
        }
    }

    public static void sendPushEndteredLogIfFlagOn() {
        if (flagPushEntered) {

            flagPushEntered = false;
        }

    }

    public static void sendMessageSendingLogIfFlagOn() {
        if (flagMessageSending) {

            flagMessageSending = false;
        }

    }
}

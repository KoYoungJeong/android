package com.tosslab.toss.app.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by justinygchoi on 2014. 5. 27..
 */
public class DateTransformator {
    public static String getTimeDifference(Date date) {
        DateFormat mCreateDateFormat = new SimpleDateFormat("yyyy/MM/dd, HH:mm");
        return mCreateDateFormat.format(date);
    }

//    public static String getTimeDifference(Date date) {
//        DateFormat mCreateDateFormat = new SimpleDateFormat("yyyy/MM/dd, HH:mm");
//
//        final int MIN = 60000;
//        final int HOUR = 60 * MIN;
//        final int DAY = 24 * HOUR;
//        final int WEEK = DAY * 7;
//
//        DateFormat hourFormat = new SimpleDateFormat("a K:mm");
//        String hour = hourFormat.format(date);
//
//        Date current = new Date();
//        long millsTimeDifference = current.getTime() - date.getTime();
//        // TODO 한글 전부 바꿔
//        if ((millsTimeDifference / WEEK) >= 1) {
//            String time = mCreateDateFormat.format(date);
//            return time;
//        } else if ((millsTimeDifference / DAY) >= 3) {
//            return (millsTimeDifference / DAY) + "일 전, " + hour;
//        } else if ((millsTimeDifference / DAY) >= 2) {
//            return "그저께, " + hour;
//        } else if ((millsTimeDifference / DAY) >= 1) {
//            return "어제, " + hour;
//        } else if ((millsTimeDifference / HOUR) >= 1) {
//            return (millsTimeDifference / HOUR) + "시간 전";
//        } else if ((millsTimeDifference / MIN) >= 1) {
//            return (millsTimeDifference / MIN) + "분 전";
//        }
//
//        return "방금";
//    }

    public static String getTimeString(Date date) {
        DateFormat mCreateDateFormat = new SimpleDateFormat("yyyy/MM/dd, HH:mm");
        return mCreateDateFormat.format(date);
    }
}

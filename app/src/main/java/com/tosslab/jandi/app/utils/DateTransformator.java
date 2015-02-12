package com.tosslab.jandi.app.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by justinygchoi on 2014. 5. 27..
 */
public class DateTransformator {
    public static String getTimeDifference(Date date) {
        DateFormat mCreateDateFormat = new SimpleDateFormat("MM/dd/yyyy, hh:mm a");
        return mCreateDateFormat.format(date);
    }

    public static String getTimeString(Date date) {
        DateFormat mCreateDateFormat = new SimpleDateFormat("MM/dd/yyyy, hh:mm a");
        return mCreateDateFormat.format(date);
    }

    public static String getTimeStringForDivider(Date date) {
        DateFormat mCreateDateFormat = new SimpleDateFormat("MM/dd/yyyy, EEE");
        return mCreateDateFormat.format(date);
    }

    public static String getTimeStringForDivider(long dateTime) {
        DateFormat mCreateDateFormat = new SimpleDateFormat("MM/dd/yyyy, EEE");
        return mCreateDateFormat.format(dateTime);
    }

    public static String getTimeStringForSimple(Date date) {
        DateFormat mCreateDateFormat = new SimpleDateFormat("a h:mm");
        return mCreateDateFormat.format(date);
    }
}

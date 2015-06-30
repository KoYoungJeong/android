package com.tosslab.jandi.app.utils;

import org.codehaus.jackson.map.util.ISO8601DateFormat;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by justinygchoi on 2014. 5. 27..
 */
public class DateTransformator {
    public static final String FORMAT_DEFAULT = "MM/dd/yyyy, hh:mm a";
    public static final String FORMAT_YYYYMMDD_HHMM_A = "yyyy/MM/dd hh:mm a";

//    public static String getTimeDifference(Date date) {
//        return getTimeString(date, FORMAT_DEFAULT);
//    }

    public static String getTimeString(Date date) {
        return getTimeString(date, FORMAT_DEFAULT);
    }

    public static String getTimeString(Date date, String format) {
        return new SimpleDateFormat(format).format(date);
    }

    public static String getTimeStringFromISO(String date, String format) {
        try {
//            Date formatDate = ISO_DATE_FORMAT.parse(date);
            ISO8601DateFormat isoFormat = new ISO8601DateFormat();
            Date formatDate = isoFormat.parse(date);
            date = getTimeString(formatDate, format);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String getTimeStringFromISO(String date) {
        return getTimeStringFromISO(date, "MM/dd/yyyy, hh:mm a");
    }

    public static String getTimeStringForDivider(Date date) {
        return getTimeString(date, "MM/dd/yyyy, EEE");
    }

    public static String getTimeStringForDivider(long dateTime) {
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy, EEE");
        return dateFormat.format(dateTime);
    }

    public static String getTimeStringForSimple(Date date) {
        return getTimeString(date, "a h:mm");
    }
}

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
    public static final long PARSE_FAIL = -1;
    public static final String FORMAT_DEFAULT = "MM/dd/yyyy, hh:mm a";
    public static final String FORMAT_YYYYMMDD_HHMM_A = "yyyy/MM/dd hh:mm a";

    public static String getTimeString(Date date) {
        return getTimeString(date, FORMAT_DEFAULT);
    }

    public static String getTimeString(Date date, String format) {
        return new SimpleDateFormat(format).format(date);
    }

    public static long getTimeFromISO(String date) {
        ISO8601DateFormat isoFormat = new ISO8601DateFormat();
        Date formatDate = null;
        try {
            formatDate = isoFormat.parse(date);
            return formatDate.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return PARSE_FAIL;
    }

    public static String getTimeStringFromISO(String date, String format) {
        try {
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

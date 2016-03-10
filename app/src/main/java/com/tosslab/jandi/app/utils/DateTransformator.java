package com.tosslab.jandi.app.utils;

import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;

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

    public static String getTimeString(Date date) {
        Locale locale = JandiApplication.getContext().getResources().getConfiguration().locale;
        switch (locale.getLanguage()) {
            case "ko":
            case "zh":
                return getTimeString(date, "yyyy/MM/dd a hh:mm");
            case "ja":
            default:
                return getTimeString(date, "MM/dd/yyyy hh:mm a");
        }
    }

    public static String getTimeString(Date date, String format) {
        return new SimpleDateFormat(format).format(date);
    }

    public static long getTimeFromISO(String date) {
        if (TextUtils.isEmpty(date)) {
            return PARSE_FAIL;
        }

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

    public static String getTimeStringForDivider(long dateTime) {
        Locale locale = JandiApplication.getContext().getResources().getConfiguration().locale;
        DateFormat dateFormat = null;
        switch (locale.getLanguage()) {
            case "ko":
                dateFormat = new SimpleDateFormat("yyyy년 MM월 dd일 (EEE)");
                break;
            case "zh":
                dateFormat = new SimpleDateFormat("yyyy年 MM月 dd日 EEE");
                break;
            case "ja":
                dateFormat = new SimpleDateFormat("yyyy年 MM月 dd日 (EEE)");
                break;
            default:
                dateFormat = new SimpleDateFormat("EEE MMM dd, yyyy");
                break;
        }
        return dateFormat.format(dateTime);
    }

    public static String getTimeStringForSimple(Date date) {
        Locale locale = JandiApplication.getContext().getResources().getConfiguration().locale;
        switch (locale.getLanguage()) {
            case "ko":
            case "zh":
                return getTimeString(date, "a h:mm");
            case "ja":
            default:
                return getTimeString(date, "h:mm a");
        }
    }

}

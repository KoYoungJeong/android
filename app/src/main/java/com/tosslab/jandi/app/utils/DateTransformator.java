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
    public static final String FORMAT_DEFAULT = "MM/dd/yyyy_hh:mm_a";
    public static final String FORMAT_YYYYMMDD_HHMM_A = "yyyy/MM/dd_a_hh:mm";

    public static String getTimeString(Date date) {
        return getTimeString(date, FORMAT_DEFAULT);
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

    public static String getTimeStringFromISO(String date) {
        return getTimeStringFromISO(date, "MM/dd/yyyy, hh:mm a");
    }

    public static String getTimeStringForDivider(Date date) {
        return getTimeString(date, "MM/dd/yyyy, EEE");
    }

    public static String getTimeStringForDivider(long dateTime) {
        Locale locale = JandiApplication.getContext().getResources().getConfiguration().locale;
        DateFormat dateFormat = null;
        switch (locale.getLanguage()) {
            case "ko":
                dateFormat = new SimpleDateFormat("yyyy년_MM월_dd일_(EEE)");
                break;
            case "zh":
                dateFormat = new SimpleDateFormat("yyyy年_MM月_dd日_EEE");
                break;
            case "ja":
                dateFormat = new SimpleDateFormat("yyyy年_MM月_dd日_(EEE)");
                break;
            default:
                dateFormat = new SimpleDateFormat("EEE_MMM_dd,_yyyy");
                break;
        }
        return dateFormat.format(dateTime);
    }

    public static String getTimeStringForSimple(Date date) {
        Locale locale = JandiApplication.getContext().getResources().getConfiguration().locale;

        String langCode = locale.getLanguage();

        if (TextUtils.equals(langCode, Locale.KOREA.getLanguage())
                || TextUtils.equals(langCode, Locale.JAPAN.getLanguage())) {

            return getTimeString(date, "a_h:mm");
        } else {
            return getTimeString(date, "h:mm_a");
        }

    }
}

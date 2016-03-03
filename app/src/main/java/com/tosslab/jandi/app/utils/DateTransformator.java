package com.tosslab.jandi.app.utils;

import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;

import org.codehaus.jackson.map.util.ISO8601DateFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by justinygchoi on 2014. 5. 27..
 */
public class DateTransformator {
    public static final long PARSE_FAIL = -1;
    public static final String FORMAT_DEFAULT_KOREA = "MM/dd/yyyy, hh:mm a";
    public static final String FORMAT_DEFAULT_USA = "MM/dd/yyyy, a hh:mm";
    public static final String FORMAT_YYYYMMDD_HHMM_A_KOREA = "yyyy/MM/dd hh:mm a";
    public static final String FORMAT_YYYYMMDD_HHMM_A_USA = "yyyy/MM/dd a hh:mm";
    private static final String FORMAT_MM_DD_YYYY_EEE = "MM/dd/yyyy, EEE";
    private static final String FOMAT_H_MM_KOREA = "a h:mm";
    private static final String FORMAT_H_MM_USA = "h:mm a";

    public static String getTimeString(Date date) {
        if (isKoreaStyle()) {
            return getTimeString(date, FORMAT_DEFAULT_KOREA);
        } else {
            return getTimeString(date, FORMAT_DEFAULT_USA);
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

    public static String getTimeStringFromISO(String date) {
        try {
            ISO8601DateFormat isoFormat = new ISO8601DateFormat();
            Date formatDate = isoFormat.parse(date);
            if (isKoreaStyle()) {
                date = getTimeString(formatDate, FORMAT_YYYYMMDD_HHMM_A_KOREA);
            } else {
                date = getTimeString(formatDate, FORMAT_YYYYMMDD_HHMM_A_USA);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String getTimeStringForDivider(long dateTime) {
        return getTimeString(new Date(dateTime), FORMAT_MM_DD_YYYY_EEE);
    }

    public static String getTimeStringForSimple(Date date) {
        if (isKoreaStyle()) {
            return getTimeString(date, FOMAT_H_MM_KOREA);
        } else {
            return getTimeString(date, FORMAT_H_MM_USA);
        }

    }

    private static boolean isKoreaStyle() {
        Locale locale = JandiApplication.getContext().getResources().getConfiguration().locale;

        String langCode = locale.getLanguage();
        return TextUtils.equals(langCode, Locale.KOREA.getLanguage())
                || TextUtils.equals(langCode, Locale.JAPAN.getLanguage());
    }
}

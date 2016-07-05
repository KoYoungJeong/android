package com.tosslab.jandi.app.utils;

import android.content.res.Resources;
import android.support.annotation.Nullable;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;

import org.joda.time.Interval;

import java.text.DateFormat;
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
            case "ja":
                return getTimeString(date, "yyyy/MM/dd a hh:mm");
            default:
                return getTimeString(date, "MM/dd/yyyy hh:mm a");
        }
    }

    public static String getTimeString(Date date, String format) {
        return new SimpleDateFormat(format).format(date);
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
            case "ja":
                return getTimeString(date, "a h:mm");
            default:
                return getTimeString(date, "h:mm a");
        }
    }

    @Nullable
    public static String getRemainingDays(Date date) {
        if (new Date().compareTo(date) >= 0) {
            return "";
        }

        Interval interval = new Interval(new Date().getTime(), date.getTime());
        long leftDay = interval.toPeriod().getDays();
        long leftHour = interval.toPeriod().getHours();
        long leftMinute = interval.toPeriod().getMinutes();

        Resources resources = JandiApplication.getContext().getResources();
        StringBuilder sb = new StringBuilder();

        if (leftDay > 0) {
            String days = resources.getString(R.string.jandi_date_days);
            sb.append(leftDay + days + " " + resources.getString(R.string.jandi_date_remaining));
            String left = sb.toString();
            return left;
        }

        if (leftHour > 0) {
            String hours = resources.getString(R.string.jandi_date_hours);
            sb.append(leftHour + hours + " ");
        }

        String remaining = resources.getString(R.string.jandi_date_minutes)
                + " "
                + resources.getString(R.string.jandi_date_remaining);
        sb.append(leftMinute + remaining);
        String left = sb.toString();
        return left;
    }

}

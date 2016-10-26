package com.tosslab.jandi.app.utils;

import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by justinygchoi on 2014. 5. 27..
 */
public class DateTransformator {
    public static final long PARSE_FAIL = -1;
    private static final long SECOND_OF_MILL = 1000L;
    private static final long DAY_OF_MILL = SECOND_OF_MILL * 60L * 60L * 24L;
    private static final long HOUR_OF_MILL = SECOND_OF_MILL * 60L * 60L;
    private static final long MINUTE_OF_MILL = SECOND_OF_MILL * 60L;

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
        if (TextUtils.isEmpty(format) || date == null) {
            return "";
        }
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
        if (date == null || new Date().compareTo(date) >= 0) {
            return "";
        }

        long[] diffValue = getDiffTimes(new Date(), date);


        long leftDay = diffValue[0];
        long leftHour = diffValue[1];
        long leftMinute = diffValue[2];

        Resources resources = JandiApplication.getContext().getResources();
        StringBuilder sb = new StringBuilder();

        if (leftDay > 0) {
            return sb.append(leftDay)
                    .append(resources.getString(R.string.jandi_date_days))
                    .append(" ")
                    .append(resources.getString(R.string.jandi_date_remaining)).toString();
        }

        if (leftHour > 0) {
            return sb.append(leftHour)
                    .append(resources.getString(R.string.jandi_date_hours))
                    .append(" ").toString();
        }

        return sb.append(leftMinute)
                .append(resources.getString(R.string.jandi_date_minutes))
                .append(" ")
                .append(resources.getString(R.string.jandi_date_remaining)).toString();
    }

    private static long[] getDiffTimes(Date sourceTime, Date targetTime) {

        long[] diffTimes = new long[4];// Day, Hour, Minute, Second

        long source = sourceTime.getTime();
        long target = targetTime.getTime();

        long diff = target - source;
        long diffDay = diff / DAY_OF_MILL;

        diff = diff % DAY_OF_MILL; // remain time of a day
        long diffHour = diff / HOUR_OF_MILL;

        diff = diff % HOUR_OF_MILL; // remain time of a hour
        long diffMin = diff / MINUTE_OF_MILL;

        diff = diff % MINUTE_OF_MILL; // remain time of a min
        long diffSec = diff / SECOND_OF_MILL;


        diffTimes[0] = diffDay;
        diffTimes[1] = diffHour;
        diffTimes[2] = diffMin;
        diffTimes[3] = diffSec;

        return diffTimes;
    }

}

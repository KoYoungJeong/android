package com.tosslab.jandi.app.utils;

import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.util.Log;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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

        Calendar now = Calendar.getInstance();
        Calendar future = Calendar.getInstance();
        future.setTime(date);

        int nowDay = now.get(Calendar.DAY_OF_MONTH);
        int nowHour = now.get(Calendar.HOUR_OF_DAY);
        int nowMinute = now.get(Calendar.MINUTE);
        int nowSecond = now.get(Calendar.SECOND);

        int futureDay = future.get(Calendar.DAY_OF_MONTH);
        int futureHour = now.get(Calendar.HOUR_OF_DAY);
        int futureMinute = future.get(Calendar.MINUTE);
        int futureSecond = future.get(Calendar.SECOND);

        if (futureSecond < nowSecond) {
            futureMinute = futureMinute - 1;
        }

        int leftMinutes;
        if (futureMinute > nowMinute) {
            leftMinutes = futureMinute - nowMinute;
        } else if (futureMinute < nowMinute) {
            futureHour = futureHour - 1;
            leftMinutes = (futureMinute + 60) - nowMinute;
        } else {
            leftMinutes = 0;
        }

        int leftHours;
        if (futureHour > nowHour) {
            leftHours = futureHour - nowHour;
        } else if (futureHour < nowHour) {
            futureDay = futureDay - 1;
            leftHours = (futureHour + 24) - nowHour;
        } else {
            leftHours = 0;
        }

        int leftDays;
        if (futureDay > nowDay) {
            leftDays = futureDay - nowDay;
        } else if (nowDay > futureDay) {
            int nowMonth = now.get(Calendar.MONTH);
            int futureMonth = future.get(Calendar.MONTH);

            if (futureMonth > nowMonth) {
                Calendar calendar = Calendar.getInstance();
                int maximumDays = calendar.getMaximum(Calendar.DAY_OF_MONTH);
                leftHours = 0;
                leftDays = (futureDay + maximumDays) - nowDay;
            } else {
                leftHours = 0;
                leftDays = 0;
            }

        } else {
            leftDays = 0;
        }

        Resources resources = JandiApplication.getContext().getResources();
        StringBuilder sb = new StringBuilder();
        if (leftDays > 0) {
            String days = resources.getString(R.string.jandi_date_days);
            sb.append(leftDays + days + " " + resources.getString(R.string.jandi_date_remaining));

            String left = sb.toString();
            return left;
        }

        if (leftHours > 0) {
            String hours = resources.getString(R.string.jandi_date_hours);
            sb.append(leftHours + hours + " ");
        }

        String remaining = resources.getString(R.string.jandi_date_minutes)
                + " "
                + resources.getString(R.string.jandi_date_remaining);
        sb.append(leftMinutes + remaining);
        String left = sb.toString();
        return left;
    }

}

package com.tosslab.jandi.app.ui.settings;

import android.preference.PreferenceManager;

import com.tosslab.jandi.app.JandiApplication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Settings {
    public static final String SETTING_PUSH_AUTO_ALARM = "setting_push_auto_alarm";
    public static final String SETTING_PUSH_ALARM_SOUND = "setting_push_alarm_sound";
    public static final String SETTING_PUSH_ALARM_LED = "setting_push_alarm_led";
    public static final String SETTING_PUSH_ALARM_VIBRATION = "setting_push_alarm_vibration";
    public static final String SETTING_PUSH_PREVIEW = "setting_push_preview";
    public static final String SETTING_PUSH_ALARM_SOUND_TOPIC = "setting_push_alarm_sound_topic";
    public static final String SETTING_PUSH_ALARM_SOUND_DM = "setting_push_alarm_sound_direct_message";
    public static final String SETTING_PUSH_ALARM_SOUND_MENTION = "setting_push_alarm_sound_mention";
    public static final String SETTING_PUSH_ALARM_SCHEDULE_HAS_CACHE = "setting_push_alarm_schdule_has_cache";
    public static final String SETTING_PUSH_ALARM_SCHEDULE = "setting_push_alarm_schdule";
    public static final String SETTING_PUSH_ALARM_SCHEDULE_DAYS = "setting_push_alarm_schdule_days";
    public static final String SETTING_PUSH_ALARM_SCHEDULE_START_TIME = "setting_push_alarm_schdule_start_time";
    public static final String SETTING_PUSH_ALARM_SCHEDULE_END_TIME = "setting_push_alarm_schdule_end_time";
    public static final String SETTING_PUSH_ALARM_SCHEDULE_TIMEZONE = "setting_push_alarm_schdule_timezone";

    public static final String SETTING_ORIENTATION = "setting_orientation";

    public static boolean isPushOn() {
        return PreferenceManager.getDefaultSharedPreferences(JandiApplication.getContext())
                .getBoolean(Settings.SETTING_PUSH_AUTO_ALARM, true);
    }

    public static boolean getPreferencePushAlarmSchedule() {
        return PreferenceManager.getDefaultSharedPreferences(JandiApplication.getContext())
                .getBoolean(Settings.SETTING_PUSH_ALARM_SCHEDULE, false);
    }

    public static void setPreferencePushAlarmSchedule(boolean checked) {
        PreferenceManager.getDefaultSharedPreferences(JandiApplication.getContext()).edit()
                .putBoolean(Settings.SETTING_PUSH_ALARM_SCHEDULE, checked)
                .apply();
    }

    public static List<Integer> getPreferencePushAlarmScheduleDays() {
        Set<String> defaultDaysSet = new HashSet<>();
        List<Integer> dayList = new ArrayList<>();
        defaultDaysSet.add("1");
        defaultDaysSet.add("2");
        defaultDaysSet.add("3");
        defaultDaysSet.add("4");
        defaultDaysSet.add("5");
        Set<String> alarmScheduleDays = PreferenceManager.getDefaultSharedPreferences(JandiApplication.getContext())
                .getStringSet(Settings.SETTING_PUSH_ALARM_SCHEDULE_DAYS, defaultDaysSet);
        Iterator iterator = alarmScheduleDays.iterator();
        while (iterator.hasNext()) {
            dayList.add(Integer.valueOf((String) iterator.next()));
        }
        Collections.sort(dayList);
        return dayList;
    }

    public static void setPreferencePushAlarmScheduleDays(List<Integer> dayList) {
        Set<String> daySet = new HashSet<>();

        for (int day : dayList) {
            daySet.add(String.valueOf(day));
        }

        PreferenceManager.getDefaultSharedPreferences(JandiApplication.getContext()).edit()
                .putStringSet(Settings.SETTING_PUSH_ALARM_SCHEDULE_DAYS, daySet)
                .apply();
    }

    public static int getPreferencePushAlarmScheduleStartTime() {
        return PreferenceManager.getDefaultSharedPreferences(JandiApplication.getContext())
                .getInt(Settings.SETTING_PUSH_ALARM_SCHEDULE_START_TIME, 700);
    }

    public static void setPreferencePushAlarmScheduleStartTime(int startTime) {
        PreferenceManager.getDefaultSharedPreferences(JandiApplication.getContext()).edit()
                .putInt(Settings.SETTING_PUSH_ALARM_SCHEDULE_START_TIME, startTime)
                .apply();
    }

    public static int getPreferencePushAlarmScheduleEndTime() {
        return PreferenceManager.getDefaultSharedPreferences(JandiApplication.getContext())
                .getInt(Settings.SETTING_PUSH_ALARM_SCHEDULE_END_TIME, 1900);
    }

    public static void setPreferencePushAlarmScheduleEndTime(int endTime) {
        PreferenceManager.getDefaultSharedPreferences(JandiApplication.getContext()).edit()
                .putInt(Settings.SETTING_PUSH_ALARM_SCHEDULE_END_TIME, endTime)
                .apply();
    }

    public static int getPreferencePushAlarmScheduleTimeZone() {
        return PreferenceManager.getDefaultSharedPreferences(JandiApplication.getContext())
                .getInt(Settings.SETTING_PUSH_ALARM_SCHEDULE_TIMEZONE, 9);
    }

    public static void setPreferencePushAlarmScheduleTimeZone(int timeZone) {
        PreferenceManager.getDefaultSharedPreferences(JandiApplication.getContext()).edit()
                .putInt(Settings.SETTING_PUSH_ALARM_SCHEDULE_TIMEZONE, timeZone)
                .apply();
    }

    public static boolean hasAlarmScheduleCache() {
        return PreferenceManager.getDefaultSharedPreferences(JandiApplication.getContext())
                .getBoolean(Settings.SETTING_PUSH_ALARM_SCHEDULE_HAS_CACHE, false);
    }

    public static void setHasAlarmSchedule(boolean hasCache) {
        PreferenceManager.getDefaultSharedPreferences(JandiApplication.getContext()).edit()
                .putBoolean(Settings.SETTING_PUSH_ALARM_SCHEDULE_HAS_CACHE, hasCache)
                .apply();
    }


}

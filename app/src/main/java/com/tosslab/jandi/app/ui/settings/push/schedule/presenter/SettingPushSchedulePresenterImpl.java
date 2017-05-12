package com.tosslab.jandi.app.ui.settings.push.schedule.presenter;

import com.tosslab.jandi.app.ui.settings.Settings;
import com.tosslab.jandi.app.ui.settings.push.schedule.model.SettingPushScheduleModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by tee on 2017. 4. 18..
 */

public class SettingPushSchedulePresenterImpl implements SettingPushSchedulePresenter {

    @Inject
    SettingPushScheduleModel model;

    @Inject
    SettingPushSchedulePresenter.View view;

    @Inject
    public SettingPushSchedulePresenterImpl() {
    }

    @Override
    public void initValues() {
        if (Settings.hasAlarmScheduleCache()) {
            List<Integer> DayList = Settings.getPreferencePushAlarmScheduleDays();
            int startTime = Settings.getPreferencePushAlarmScheduleStartTime();
            int endTime = Settings.getPreferencePushAlarmScheduleEndTime();
            int timeZone = Settings.getPreferencePushAlarmScheduleTimeZone();
            initSchedule(DayList, startTime, endTime, timeZone);
        } else {
            List<Integer> defaultDays = new ArrayList<>();
            defaultDays.add(1);
            defaultDays.add(2);
            defaultDays.add(3);
            defaultDays.add(4);
            defaultDays.add(5);
            initSchedule(defaultDays, 700, 1900, getTimeZoneInt());
        }
    }

    private void initSchedule(List<Integer> dayList, int startTime, int endTime, int timeZone) {
        if (dayList != null && dayList.size() > 0) {
            int timeZoneDistance = 0;

            if (timeZone != -100) {
                timeZoneDistance = getTimeZoneInt() - timeZone;
            }

            // 표준시로 인해 날짜가 바뀔 수 있음을 염두
            boolean dayPlus = false;
            boolean dayMinus = false;

            if (startTime != -1) {
                int startTimeHour = startTime / 100;
                startTimeHour = startTimeHour + timeZoneDistance;
                if (startTimeHour < 0) {
                    startTimeHour = 24 - startTimeHour;
                    dayMinus = true;
                } else if (startTimeHour > 23) {
                    startTimeHour = startTimeHour - 24;
                    dayPlus = true;
                }
                view.setStartTime(startTimeHour * 100 + startTime % 100);
            }

            if (endTime != -1) {
                int endTimeHour = endTime / 100;
                endTimeHour = endTimeHour - timeZoneDistance;
                if (endTimeHour < 0) {
                    endTimeHour = 24 - endTimeHour;
                } else if (endTimeHour > 23) {
                    endTimeHour = endTimeHour - 24;
                }
                view.setEndTime(endTimeHour * 100 + endTime % 100);
            }

            if (dayMinus) {
                for (int i = 0; i < dayList.size(); i++) {
                    int replaceDay = dayList.get(i) - 1;
                    if (replaceDay < 0) {
                        replaceDay = 6;
                    }
                    dayList.set(i, replaceDay);
                }
            } else if (dayPlus) {
                for (int i = 0; i < dayList.size(); i++) {
                    int replaceDay = dayList.get(i) + 1;
                    if (replaceDay > 6) {
                        replaceDay = 0;
                    }
                    dayList.set(i, replaceDay);
                }
            }

            view.setDays(dayList);
        }
    }

    @Override
    public void setAlarmSchedule(HashMap<Integer, Boolean> days, int startTime, int endTime) {
        view.showProgressWheel();
        List<Integer> dayList = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            if (days.get(i) != null && days.get(i)) {
                dayList.add(i);
            }
        }

        if (dayList.size() < 1) {
            view.dismissProgressWheel();
            view.showLeastOnedaySelectToast();
            return;
        }

        Observable.defer(() -> {
            final int timeZoneInt = getTimeZoneInt();
            boolean success = model.setAlarmSchedule(dayList, startTime, endTime, timeZoneInt);
            return Observable.just(success);
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success -> {
                    view.dismissProgressWheel();
                    if (success) {
                        Settings.setPreferencePushAlarmSchedule(true);
                        Settings.setPreferencePushAlarmScheduleDays(dayList);
                        Settings.setPreferencePushAlarmScheduleStartTime(startTime);
                        Settings.setPreferencePushAlarmScheduleEndTime(endTime);
                        Settings.setPreferencePushAlarmScheduleTimeZone(getTimeZoneInt());
                        view.finishActivity(true);
                    } else {
                    }
                });

    }

    private int getTimeZoneInt() {
        TimeZone timeZone = TimeZone.getDefault();
        String timeZoneString = timeZone.getDisplayName(false, TimeZone.SHORT).replace("GMT", "");

        boolean isPlus = false;

        if (timeZoneString.contains("+")) {
            isPlus = true;
        } else {
            isPlus = false;
        }

        int timeZoneInt = Integer.valueOf(timeZoneString.substring(1, 3));

        if (!isPlus) {
            timeZoneInt = timeZoneInt * -1;
        }
        return timeZoneInt;
    }

}

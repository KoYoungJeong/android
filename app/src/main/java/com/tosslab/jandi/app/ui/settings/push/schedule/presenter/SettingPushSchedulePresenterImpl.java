package com.tosslab.jandi.app.ui.settings.push.schedule.presenter;

import com.tosslab.jandi.app.network.models.ResDeviceSubscribe;
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
            Observable.defer(() -> {
                ResDeviceSubscribe resDeviceSubscribe = model.getDeviceInfo();
                return Observable.just(resDeviceSubscribe);
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(deviceInfo -> {
                        initSchedule(deviceInfo.getDays(),
                                deviceInfo.getStartTime(),
                                deviceInfo.getEndTime(),
                                deviceInfo.getTimeZone());
                        Settings.setHasAlarmSchedule(true);
                        Settings.setPreferencePushAlarmScheduleDays(deviceInfo.getDays());
                        Settings.setPreferencePushAlarmScheduleStartTime(deviceInfo.getStartTime());
                        Settings.setPreferencePushAlarmScheduleEndTime(deviceInfo.getEndTime());
                        Settings.setPreferencePushAlarmScheduleTimeZone(deviceInfo.getTimeZone());
                    });
        }
    }

    private void initSchedule(List<Integer> dayList, int startTime, int endTime, int timeZone) {
        if (dayList != null
                && dayList.size() > 0) {
            view.setDays(dayList);
        } else {
            return;
        }

        int timeZoneDistance = 0;

        if (timeZone != -100) {
            timeZoneDistance = timeZone - 9;
        }

        if (startTime != -1) {
            int startTimeHour = startTime / 100;
            startTimeHour = startTimeHour - timeZoneDistance;
            if (startTimeHour < 0) {
                startTimeHour = 24 - startTimeHour;
            }
            view.setStartTime(startTimeHour * 100 + startTime % 100);
        }

        if (endTime != -1) {
            int endTimeHour = endTime / 100;
            endTimeHour = endTimeHour - timeZoneDistance;
            if (endTimeHour < 0) {
                endTimeHour = 24 - endTimeHour;
            }
            view.setEndTime(endTimeHour * 100 + endTime % 100);
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

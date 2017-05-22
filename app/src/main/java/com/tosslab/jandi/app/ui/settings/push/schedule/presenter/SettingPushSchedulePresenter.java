package com.tosslab.jandi.app.ui.settings.push.schedule.presenter;

import java.util.HashMap;
import java.util.List;

/**
 * Created by tee on 2017. 4. 18..
 */

public interface SettingPushSchedulePresenter {

    void initValues();

    void setAlarmSchedule(HashMap<Integer, Boolean> days, int startTime, int EndTime);

    interface View {
        void setStartTime(int startTime);

        void setEndTime(int endTime);

        void setDays(List<Integer> days);

        void showProgressWheel();

        void dismissProgressWheel();

        void finishActivity(boolean result);

        void showLeastOnedaySelectToast();
    }

}

package com.tosslab.jandi.app.ui.settings.push.schedule.model;

import com.tosslab.jandi.app.network.client.account.devices.DeviceApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqAlarmSchedule;
import com.tosslab.jandi.app.network.models.ResDeviceSubscribe;
import com.tosslab.jandi.app.utils.TokenUtil;

import java.util.List;

import javax.inject.Inject;

import dagger.Lazy;

/**
 * Created by tee on 2017. 4. 18..
 */

public class SettingPushScheduleModel {

    private Lazy<DeviceApi> deviceApi;

    @Inject
    public SettingPushScheduleModel(Lazy<DeviceApi> deviceApi) {
        this.deviceApi = deviceApi;
    }

    public boolean setAlarmSchedule(List<Integer> dayList, int startTime, int endTime, int timeZone) {
        try {
            ReqAlarmSchedule reqAlarmSchedule = new ReqAlarmSchedule(dayList, startTime, endTime, timeZone);
            deviceApi.get().setAlarmSchedule(TokenUtil.getTokenObject().getDeviceId(), reqAlarmSchedule);
            return true;
        } catch (RetrofitException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ResDeviceSubscribe getDeviceInfo() {
        try {
            return deviceApi.get().getDeviceInfo(TokenUtil.getTokenObject().getDeviceId());
        } catch (RetrofitException e) {
            e.printStackTrace();
            return null;
        }
    }

}

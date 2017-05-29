package com.tosslab.jandi.app.ui.settings.push.absence.model;

import com.tosslab.jandi.app.network.client.account.absence.AccountAbsenceApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqAbsenceInfo;

import java.util.Date;

import javax.inject.Inject;

import dagger.Lazy;

/**
 * Created by tee on 2017. 5. 23..
 */

public class SettingAbsenceModel {

    private Lazy<AccountAbsenceApi> accountAbsenceApi;

    @Inject
    public SettingAbsenceModel(Lazy<AccountAbsenceApi> accountAbsenceApi) {
        this.accountAbsenceApi = accountAbsenceApi;
    }

    public boolean setEnableAbsenceInfo(boolean disablePush, String message, Date startAt, Date endAt) {
        try {
            ReqAbsenceInfo reqAbsenceInfo =
                    new ReqAbsenceInfo("enabled", disablePush, message, startAt, endAt);
            accountAbsenceApi.get().updateAbsenceInfo(reqAbsenceInfo);
            return true;
        } catch (RetrofitException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean setDisableAbsenceInfo() {
        try {
            ReqAbsenceInfo reqAbsenceInfo = new ReqAbsenceInfo("disabled");
            accountAbsenceApi.get().updateAbsenceInfo(reqAbsenceInfo);
            return true;
        } catch (RetrofitException e) {
            e.printStackTrace();
            return false;
        }
    }


}

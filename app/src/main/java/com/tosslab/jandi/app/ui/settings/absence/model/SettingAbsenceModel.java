package com.tosslab.jandi.app.ui.settings.absence.model;

import com.tosslab.jandi.app.local.orm.repositories.info.InitialAccountInfoRepository;
import com.tosslab.jandi.app.network.client.account.absence.AccountAbsenceApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqAbsenceInfo;
import com.tosslab.jandi.app.network.models.ResStartAccountInfo;

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
            // 종료날짜에 하루 더해야 함.
            long endTime = endAt.getTime() + 86400000;
            ReqAbsenceInfo reqAbsenceInfo =
                    new ReqAbsenceInfo("enabled", disablePush, message, startAt, new Date(endTime));
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

    public ResStartAccountInfo.Absence getAbsenceInfo() {
        return InitialAccountInfoRepository.getInstance().getAbsenceInfo();
    }


}

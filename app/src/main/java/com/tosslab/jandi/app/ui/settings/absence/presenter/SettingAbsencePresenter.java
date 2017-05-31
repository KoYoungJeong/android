package com.tosslab.jandi.app.ui.settings.absence.presenter;

import java.util.Date;

/**
 * Created by tee on 2017. 5. 23..
 */

public interface SettingAbsencePresenter {

    void onInit();

    void updateAbsence(boolean enabled, Date startDate, Date endDate, boolean disablePush, String message);

    boolean hasChangeInfo(boolean enabled, Date startDate, Date endDate, boolean disablePush, String message);

    interface View {
        void onSettingAbsenceCheckboxClicked();

        void onPushAlarmEnableCheckboxClicked();

        void setOptionText(String text);

        void setStartDate(Date startDate);

        void setEndDate(Date endDate);

        void showEnableAbsenceInfoToast();

        void showDisableAbsenceInfoToast();

        void showChangeAbsenceInfoToast();

        void finish();

        void setPeriodView();

        void showProgressBar();

        void dismissProgressBar();

        void showInvalidDatesDialog();

        void showPastDatesDialog();
    }
}

package com.tosslab.jandi.app.ui.settings.absence.presenter;

import android.text.TextUtils;

import com.tosslab.jandi.app.network.models.start.Absence;
import com.tosslab.jandi.app.ui.settings.absence.model.SettingAbsenceModel;

import java.util.Calendar;
import java.util.Date;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by tee on 2017. 5. 23..
 */

public class SettingAbsencePresenterImpl implements SettingAbsencePresenter {

    @Inject
    SettingAbsenceModel settingAbsenceModel;

    @Inject
    SettingAbsencePresenter.View view;

    @Inject
    public SettingAbsencePresenterImpl() {
    }

    @Override
    public void onInit() {
        Absence absenceInfo = settingAbsenceModel.getAbsenceInfo();
        if (absenceInfo != null && absenceInfo.getStatus().equals("enabled")) {
            view.onSettingAbsenceCheckboxClicked();
            if (absenceInfo.isDisablePush()) {
                view.onPushAlarmEnableCheckboxClicked();
            }
            view.setStartDate(absenceInfo.getStartAt());

            // 1day -> 86400000 ms;
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(absenceInfo.getEndAt().getTime());
            Date endDate = new Date(calendar.getTimeInMillis() - 86400000);
            view.setEndDate(endDate);
            if (!TextUtils.isEmpty(absenceInfo.getMessage())) {
                view.setOptionText(absenceInfo.getMessage());
            }
            view.setPeriodView();
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            // 1day -> 86400000 ms;
            Date tomorrow = new Date(calendar.getTimeInMillis() + 86400000);
            view.setStartDate(tomorrow);
            view.setEndDate(tomorrow);
            view.setPeriodView();
        }
    }

    @Override
    public void updateAbsence(boolean enabled, Date startDate, Date endDate, boolean disablePush, String message) {

        if (!isValidDates(startDate, endDate)) {
            view.showInvalidDatesDialog();
            return;
        }

        if (isPastDates(endDate)) {
            view.showPastDatesDialog();
            return;
        }

        if (isOver3year(startDate, endDate)) {
            view.showOver3YearsDialog();
            return;
        }

        view.showProgressBar();
        boolean isEnableNotChanged = comparePreviousAndCurrentEnabled(enabled);

        if (hasChangeInfo(enabled, startDate, endDate, disablePush, message)) {
            Observable.defer(() -> {
                Boolean result = false;
                if (!enabled) {
                    result = settingAbsenceModel.setDisableAbsenceInfo();
                } else {
                    result = settingAbsenceModel.setEnableAbsenceInfo(disablePush, message, startDate, endDate);
                }
                return Observable.just(result);
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(result -> {
                        if (result) {
                            if (!enabled) {
                                view.showDisableAbsenceInfoToast();
                            } else {
                                if (isEnableNotChanged) {
                                    view.showChangeAbsenceInfoToast();
                                } else {
                                    view.showEnableAbsenceInfoToast();
                                }
                            }
                            view.dismissProgressBar();
                            view.finish();
                        }
                    }, t -> {
                        t.printStackTrace();
                        view.dismissProgressBar();
                    });
        }
    }

    public boolean hasChangeInfo(boolean enabled, Date startDate, Date endDate, boolean disablePush, String message) {
        Absence savedAbsence = settingAbsenceModel.getAbsenceInfo();
        if (savedAbsence == null) {
            return true;
        }

        if (enabled != savedAbsence.getStatus().equals("enabled")) {
            return true;
        } else {
            if (!enabled && !savedAbsence.getStatus().equals("enabled")) {
                return false;
            }
        }

        if (startDate != null && startDate.getTime() != savedAbsence.getStartAt().getTime()) {
            return true;
        }

        if (endDate != null && endDate.getTime() + 86400000 != savedAbsence.getEndAt().getTime()) {
            return true;
        }

        if (disablePush != savedAbsence.isDisablePush()) {
            return true;
        }

        if (!message.equals(savedAbsence.getMessage())) {
            return true;
        }

        return false;
    }


    public boolean comparePreviousAndCurrentEnabled(boolean enabled) {
        Absence savedAbsence = settingAbsenceModel.getAbsenceInfo();
        if (savedAbsence == null) {
            return false;
        }

        if (enabled && savedAbsence.getStatus().equals("enabled")) {
            return true;
        }
        return false;
    }

    private boolean isValidDates(Date startDate, Date endDate) {
        if (endDate.getTime() - startDate.getTime() >= 0) {
            return true;
        }
        return false;
    }

    private boolean isPastDates(Date endDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.getTimeInMillis() > endDate.getTime()) {
            return true;
        }

        return false;
    }

    private boolean isOver3year(Date startDate, Date endDate) {
        // 3year == 94670778000 millisecond;
        if ((endDate.getTime() - startDate.getTime()) > 94670778000l) {
            return true;
        }

        return false;
    }

}
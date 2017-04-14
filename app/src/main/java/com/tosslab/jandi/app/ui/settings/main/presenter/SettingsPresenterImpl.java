package com.tosslab.jandi.app.ui.settings.main.presenter;

import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.client.main.LoginApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.services.socket.JandiSocketService;
import com.tosslab.jandi.app.ui.settings.Settings;
import com.tosslab.jandi.app.ui.settings.model.SettingsModel;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.SignOutUtil;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.UUID;

import javax.inject.Inject;

import dagger.Lazy;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SettingsPresenterImpl implements SettingsPresenter {
    private View view;
    private Lazy<LoginApi> loginApi;

    @Inject
    public SettingsPresenterImpl(View view, Lazy<LoginApi> loginApi) {
        this.view = view;
        this.loginApi = loginApi;
    }

    @Override
    public void onSignOut() {
        view.showSignOutDialog();
    }

    @Override
    public void startSignOut() {
        Observable.defer(() -> {
            String deviceId = TokenUtil.getTokenObject().getDeviceId();
            // deviceId 가 없는 경우에 대한 방어코드, deviceId 가 비어 있는 경우 400 error 가 떨어짐.
            // UUID RFC4122 규격 맞춘 아무 값이나 필요
            if (TextUtils.isEmpty(deviceId)) {
                deviceId = JandiApplication.getDeviceUUID();
            }

            return Observable.just(deviceId);
        })
                .doOnSubscribe(() -> view.showProgressDialog())
                .observeOn(Schedulers.io())
                .concatMap(deviceId -> {
                    try {
                        ResCommon resCommon = loginApi.get().deleteToken(TokenUtil.getRefreshToken(), deviceId);
                        return Observable.just(resCommon);
                    } catch (RetrofitException e) {
                        LogUtil.d(e.getCause().getMessage());
                        return Observable.error(e);
                    }
                })
                .onErrorReturn(throwable -> new ResCommon())
                .doOnNext(resCommon1 -> {
                    SignOutUtil.removeSignData();
                    BadgeUtils.clearBadge(JandiApplication.getContext());
                    JandiSocketService.stopService(JandiApplication.getContext());
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    String toastMessage = JandiApplication.getContext().getString(R.string.jandi_message_logout);
                    view.showSuccessToast(toastMessage);
                    view.dismissProgressDialog();
                    view.moveLoginActivity();
                }, t -> {
                    view.dismissProgressDialog();
                });
    }

    @Override
    public void onSetUpOrientation(String selectedValue) {
        int orientation = SettingsModel.getOrientationValue(selectedValue);
        view.setOrientation(orientation);
        view.setOrientationSummary(selectedValue);

    }

    @Override
    public void onSetUpVersion() {
        String version = SettingsModel.getVersionName();
        view.setVersion(version);
    }

    @Override
    public void onInitViews() {
        boolean portraitOnly = JandiApplication.getContext().getResources().getBoolean(R.bool.portrait_only);
        if (portraitOnly) {
            view.setOrientationViewVisibility(false);
        } else {
            String value = PreferenceManager.getDefaultSharedPreferences(
                    JandiApplication.getContext()).getString(Settings.SETTING_ORIENTATION, "0");
            onSetUpOrientation(value);
        }

        onSetUpVersion();
    }

    @Override
    public void onLaunchHelpPage() {
        view.launchHelpPage(SettingsModel.getSupportUrl());
    }

}

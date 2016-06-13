package com.tosslab.jandi.app.ui.settings.main.presenter;

import android.content.Context;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.client.main.LoginApi;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.services.socket.JandiSocketService;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.settings.Settings;
import com.tosslab.jandi.app.ui.settings.model.SettingsModel;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.SignOutUtil;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;

import java.util.UUID;

@EBean
public class SettingsPresenterImpl implements SettingsPresenter {
    private View view;

    @Override
    public void setView(View view) {
        this.view = view;
    }

    @Override
    public void onSignOut() {
        if (NetworkCheckUtil.isConnected()) {
            view.showSignOutDialog();
        } else {
            view.showCheckNetworkDialog();
        }
    }

    @Background
    @Override
    public void startSignOut() {
        view.showProgressDialog();
        try {

            Context context = JandiApplication.getContext();
            String deviceId = TokenUtil.getTokenObject().getDeviceId();
            // deviceId 가 없는 경우에 대한 방어코드, deviceId 가 비어 있는 경우 400 error 가 떨어짐.
            // UUID RFC4122 규격 맞춘 아무 값이나 필요
            if (TextUtils.isEmpty(deviceId)) {
                deviceId = UUID.randomUUID().toString();
            }
            new LoginApi(RetrofitBuilder.getInstance())
                    .deleteToken(TokenUtil.getRefreshToken(), deviceId);

            SignOutUtil.removeSignData();
            BadgeUtils.clearBadge(context);
            JandiSocketService.stopService(context);

            view.showSuccessToast(context.getString(R.string.jandi_message_logout));

        } catch (Exception e) {
            LogUtil.e(Log.getStackTraceString(e));
        } finally {
            view.dismissProgressDialog();
        }
        view.moveLoginActivity();

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

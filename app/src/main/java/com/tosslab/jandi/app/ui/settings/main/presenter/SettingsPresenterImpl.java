package com.tosslab.jandi.app.ui.settings.main.presenter;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.client.main.LoginApi;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.mixpanel.MixpanelAccountAnalyticsClient;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.services.socket.JandiSocketService;
import com.tosslab.jandi.app.ui.settings.Settings;
import com.tosslab.jandi.app.ui.settings.model.SettingsModel;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.SignOutUtil;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;

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
            new LoginApi(RetrofitBuilder.newInstance())
                    .deleteToken(TokenUtil.getRefreshToken(), TokenUtil.getTokenObject().getDeviceId());

            ResAccountInfo accountInfo = AccountRepository.getRepository().getAccountInfo();
            MixpanelAccountAnalyticsClient
                    .getInstance(context, accountInfo.getId())
                    .trackAccountSigningOut()
                    .flush()
                    .clear();

            EntityManager entityManager = EntityManager.getInstance();

            MixpanelMemberAnalyticsClient
                    .getInstance(context, entityManager.getDistictId())
                    .trackSignOut()
                    .flush()
                    .clear();

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

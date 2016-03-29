package com.tosslab.jandi.app.ui.interfaces.actions;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.orm.OrmDatabaseHelper;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.client.account.AccountApi;
import com.tosslab.jandi.app.network.dagger.DaggerApiClientComponent;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.ui.intro.IntroActivity_;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.TokenUtil;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;

import javax.inject.Inject;

import dagger.Lazy;

/**
 * Created by Steve SeongUg Jung on 14. 12. 28..
 */
@EBean
public class OpenAction implements Action {

    @RootContext
    Activity activity;

    ProgressWheel progressWheel;
    @Inject
    Lazy<AccountApi> accountApi;

    @AfterInject
    void initObject() {
        progressWheel = new ProgressWheel(activity);
        DaggerApiClientComponent
                .create()
                .inject(this);
    }

    @Override
    public void execute(Uri data) {

        showProgress();

        if (data != null) {
            String access_token = data.getQueryParameter("access_token");
            String refresh_token = data.getQueryParameter("refresh_token");

            if (TextUtils.isEmpty(access_token) || TextUtils.isEmpty(refresh_token)) {
                startIntroActivity();
            } else {
                checkSession(access_token, refresh_token);
            }
        } else {
            startIntroActivity();
        }
    }

    @Background
    void checkSession(String access_token, String refresh_token) {

        ResAccessToken accessToken = new ResAccessToken();
        accessToken.setAccessToken(access_token);
        accessToken.setRefreshToken(refresh_token);
        accessToken.setTokenType("bearer");

        try {

            OrmDatabaseHelper helper = OpenHelperManager.getHelper(JandiApplication.getContext(), OrmDatabaseHelper.class);
            helper.clearAllData();

            TokenUtil.saveTokenInfoByRefresh(accessToken);
            ResAccountInfo accountInfo = accountApi.get().getAccountInfo();

            AccountRepository.getRepository().upsertAccountAllInfo(accountInfo);

            successAccessToken(accountInfo);
        } catch (Exception e) {
            TokenUtil.clearTokenInfo();
            AccountRepository.getRepository().clearAccountData();
            failAccessToken();
        }
        startIntroActivity();

    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void showProgress() {

        if (progressWheel != null && !progressWheel.isShowing()) {
            progressWheel.show();
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void dismissProgress() {

        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }

    }

    @UiThread
    void failAccessToken() {
        ColoredToast.showWarning(activity.getString(R.string.jandi_error_web_token));
    }

    @UiThread
    void successAccessToken(ResAccountInfo accountInfo) {
        ColoredToast.show(activity.getString(R.string.jandi_success_web_token));
    }

    @UiThread
    void startIntroActivity() {
        dismissProgress();
        IntroActivity_.intent(activity)
                .flags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .startForInvite(true)
                .start();
        activity.finish();
    }
}

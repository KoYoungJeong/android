package com.tosslab.jandi.app.ui.interfaces.actions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.client.JandiRestClient;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.spring.JandiV2HttpAuthentication;
import com.tosslab.jandi.app.ui.intro.IntroActivity_;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.TokenUtil;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.rest.RestService;
import org.apache.log4j.Logger;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * Created by Steve SeongUg Jung on 14. 12. 28..
 */
@EBean
public class OpenAction implements Action {

    private static final Logger logger = Logger.getLogger(OpenAction.class);

    @RestService
    JandiRestClient jandiRestClient;

    @RootContext
    Context context;

    ProgressWheel progressWheel;

    @AfterInject
    void initObject() {
        progressWheel = new ProgressWheel(context);
        progressWheel.init();
    }

    @Override
    public void execute(Uri uri) {
        checkSession(uri);
    }

    @Background
    void checkSession(Uri data) {
        showProgress();

        String access_token = data.getQueryParameter("access_token");
        String refresh_token = data.getQueryParameter("refresh_token");

        if (TextUtils.isEmpty(access_token) || TextUtils.isEmpty(refresh_token)) {

            startIntroActivity();
            return;
        }

        ResAccessToken accessToken = new ResAccessToken();
        accessToken.setAccessToken(access_token);
        accessToken.setRefreshToken(refresh_token);
        accessToken.setTokenType("bearer");

        try {
            jandiRestClient.setAuthentication(new JandiV2HttpAuthentication(accessToken.getTokenType(), accessToken.getAccessToken()));
            ResAccountInfo accountInfo = jandiRestClient.getAccountInfo();

            TokenUtil.saveTokenInfoByRefresh(context, accessToken);
            JandiAccountDatabaseManager.getInstance(context).upsertAccountInfo(accountInfo);
            JandiAccountDatabaseManager.getInstance(context).upsertAccountTeams(accountInfo.getMemberships());
            JandiAccountDatabaseManager.getInstance(context).upsertAccountEmail(accountInfo.getEmails());
            JandiAccountDatabaseManager.getInstance(context).upsertAccountDevices(accountInfo.getDevices());

            successAccessToken(accountInfo);
        } catch (HttpStatusCodeException e) {
            TokenUtil.clearTokenInfo(context);
            JandiAccountDatabaseManager.getInstance(context).clearAllData();
            failAccessToken();
            logger.debug(e.getStatusCode() + " : " + e.getMessage());
        } finally {
            startIntroActivity();
            dismissProgress();
        }

    }

    @UiThread
    void showProgress() {

        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }

        if (progressWheel != null) {
            progressWheel.show();
        }
    }

    @UiThread
    void dismissProgress() {

        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }

    }

    @UiThread
    void failAccessToken() {
        ColoredToast.showWarning(context, context.getString(R.string.jandi_error_web_token));
    }

    @UiThread
    void successAccessToken(ResAccountInfo accountInfo) {
        ColoredToast.show(context, context.getString(R.string.jandi_success_web_token));
    }

    @UiThread
    void startIntroActivity() {
        IntroActivity_.intent(context)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .start();
        ((Activity) context).finish();
    }
}

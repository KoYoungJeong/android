package com.tosslab.jandi.app.ui.interfaces.actions;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.database.JandiDatabaseManager;
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
        ResAccessToken accessToken = new ResAccessToken();
        accessToken.setAccessToken(data.getQueryParameter("access_token"));
        accessToken.setRefreshToken(data.getQueryParameter("refresh_token"));
        accessToken.setTokenType("bearer");

        try {
            jandiRestClient.setAuthentication(new JandiV2HttpAuthentication(accessToken.getTokenType(), accessToken.getAccessToken()));
            ResAccountInfo accountInfo = jandiRestClient.getAccountInfo();

            TokenUtil.saveTokenInfoByRefresh(context, accessToken);
            JandiDatabaseManager.getInstance(context).upsertAccountInfo(accountInfo);
            JandiDatabaseManager.getInstance(context).upsertAccountTeams(accountInfo.getMemberships());
            JandiDatabaseManager.getInstance(context).upsertAccountEmail(accountInfo.getEmails());
            JandiDatabaseManager.getInstance(context).upsertAccountDevices(accountInfo.getDevices());

            successAccessToken(accountInfo);
        } catch (HttpStatusCodeException e) {
            logger.debug(e.getStatusCode() + " : " + e.getMessage());
        } finally {
            dismissProgress();
        }

    }

    @UiThread
    void showProgress() {
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
        IntroActivity_.intent(context);
        ((Activity) context).finish();
    }

    @UiThread
    void successAccessToken(ResAccountInfo accountInfo) {
        ColoredToast.show(context, context.getString(R.string.jandi_success_web_token));
        IntroActivity_.intent(context);
        ((Activity) context).finish();
    }
}

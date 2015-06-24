package com.tosslab.jandi.app.ui.interfaces.actions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.ui.intro.IntroActivity_;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;

/**
 * Created by Steve SeongUg Jung on 14. 12. 28..
 */
@EBean
public class OpenAction implements Action {

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
            ResAccountInfo accountInfo = RequestApiManager.getInstance().getAccountInfoByMainRest();

            TokenUtil.saveTokenInfoByRefresh(accessToken);
            JandiAccountDatabaseManager.getInstance(context).upsertAccountAllInfo(accountInfo);

            successAccessToken(accountInfo);
        } catch (Exception e) {
            TokenUtil.clearTokenInfo();
            JandiAccountDatabaseManager.getInstance(context).clearAllData();
            failAccessToken();
            LogUtil.d(e.getMessage());
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

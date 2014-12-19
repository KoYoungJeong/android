package com.tosslab.jandi.app.ui.intro.model;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.tosslab.jandi.app.local.database.JandiDatabaseManager;
import com.tosslab.jandi.app.network.client.JandiAuthClient;
import com.tosslab.jandi.app.network.client.JandiRestClient;
import com.tosslab.jandi.app.network.manager.RequestManager;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResConfig;
import com.tosslab.jandi.app.ui.team.select.model.AccountInfoRequest;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.TokenUtil;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SupposeBackground;
import org.androidannotations.annotations.rest.RestService;
import org.apache.log4j.Logger;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * Created by Steve SeongUg Jung on 14. 12. 3..
 */

@EBean
public class IntroActivityModel {

    private final Logger log = Logger.getLogger(IntroActivityModel.class);
    // check for Splash time (1500ms)
    private final long initTime = System.currentTimeMillis();
    @RootContext
    Context context;
    @RestService
    JandiRestClient jandiRestClient;
    @Bean
    JandiAuthClient jandiAuthClient;

    /**
     * Check new app version
     */
    @SupposeBackground
    public boolean checkNewVersion() {
        // 예외가 발생할 경우에도 그저 업데이트 안내만 무시한다.
        boolean isLatestVersion = true;
        try {
            // get current app version
            int thisVersion = retrieveThisAppVersion(context);

            // get stored app version at server
            int latestVersion = getLatestVersionInBackground();
            if (thisVersion < latestVersion) {
                isLatestVersion = false;
                log.info("A new version of JANDI is available.");
            }
        } catch (JandiNetworkException e) {
        } catch (Exception e) {
        } finally {
            return isLatestVersion;
        }
    }


    protected int retrieveThisAppVersion(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            String packageName = context.getPackageName();

            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            return 0;
        }
    }

    @SupposeBackground
    int getLatestVersionInBackground() throws JandiNetworkException {
        ResConfig resConfig = jandiAuthClient.getConfig();
        return resConfig.versions.android;
    }

    public boolean isNeedLogin() {

        String refreshToken = JandiPreference.getRefreshToken(context);
        return TextUtils.isEmpty(refreshToken);
    }

    public void refreshToken() throws JandiNetworkException {
        try {
            String refreshToken = JandiPreference.getRefreshToken(context);

            // Get Access Token
            ReqAccessToken passwordReqToken = ReqAccessToken.createRefreshReqToken(refreshToken);
            ResAccessToken accessToken = jandiRestClient.getAccessToken(passwordReqToken);

            TokenUtil.saveTokenInfoByRefresh(context, accessToken);

        } catch (HttpStatusCodeException e) {
            throw new JandiNetworkException(e);
        }
    }

    public void refreshAccountInfo() throws JandiNetworkException {

        AccountInfoRequest accountInfoRequest = AccountInfoRequest.create(context, jandiRestClient);
        RequestManager<ResAccountInfo> requestManager = RequestManager.newInstance(context, accountInfoRequest);
        ResAccountInfo resAccountInfo = requestManager.request();

        JandiDatabaseManager.getInstance(context).upsertAccountInfo(resAccountInfo);
        JandiDatabaseManager.getInstance(context).upsertAccountEmail(resAccountInfo.getEmails());
        JandiDatabaseManager.getInstance(context).upsertAccountTeams(resAccountInfo.getMemberships());
        JandiDatabaseManager.getInstance(context).upsertAccountDevices(resAccountInfo.getDevices());

    }

    public void clearTokenInfo() {
        TokenUtil.clearTokenInfo(context);
    }

    public void clearAccountInfo() {
        JandiDatabaseManager.getInstance(context).deleteAccountDevices();
        JandiDatabaseManager.getInstance(context).deleteAccountEmails();
        JandiDatabaseManager.getInstance(context).deleteAccountInfo();
        JandiDatabaseManager.getInstance(context).deleteAccountTeams();
    }

    public void sleep(long initTime, long maxDelayMs) {

        long currentTimeMillis = System.currentTimeMillis();
        long currentTimeGap = currentTimeMillis - initTime;
        long sleepTime = maxDelayMs - currentTimeGap;

        try {
            if (sleepTime > 0) {
                // delay for splash
                Thread.sleep(sleepTime);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
}

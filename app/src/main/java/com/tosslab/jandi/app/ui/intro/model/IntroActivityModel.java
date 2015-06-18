package com.tosslab.jandi.app.ui.intro.model;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.local.database.entity.JandiEntityDatabaseManager;
import com.tosslab.jandi.app.network.client.JandiAuthClient;
import com.tosslab.jandi.app.network.client.JandiEntityClient;
import com.tosslab.jandi.app.network.client.JandiEntityClient_;
import com.tosslab.jandi.app.network.client.JandiRestClient;
import com.tosslab.jandi.app.network.client.JandiRestClient_;
import com.tosslab.jandi.app.network.manager.RequestManager;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResConfig;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.ui.team.select.model.AccountInfoRequest;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.rest.RestService;
import org.codehaus.jackson.map.ObjectMapper;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Headers;

/**
 * Created by Steve SeongUg Jung on 14. 12. 3..
 */

@EBean
public class IntroActivityModel {

    // check for Splash time (1500ms)
    private final long initTime = System.currentTimeMillis();
    @RootContext
    Context context;
    @Bean
    JandiAuthClient jandiAuthClient;

    /**
     * Check new app version
     */
    public boolean checkNewVersion() {
        // 예외가 발생할 경우에도 그저 업데이트 안내만 무시한다.
        boolean isLatestVersion = true;
        try {
            // get current app version
            int thisVersion = getInstalledAppVersion(context);

            // get stored app version at server
            int latestVersion = getLatestVersionInBackground();
            if (thisVersion < latestVersion) {
                isLatestVersion = false;
                LogUtil.i("A new version of JANDI is available.");
            }
        } catch (JandiNetworkException e) {
        } catch (Exception e) {
        } finally {
            return isLatestVersion;
        }
    }


    public int getInstalledAppVersion(Context context) {
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

    int getLatestVersionInBackground() throws JandiNetworkException {
        ResConfig resConfig = jandiAuthClient.getConfig();
        return resConfig.versions.android;
    }

    public boolean isNeedLogin() {

        String refreshToken = JandiPreference.getRefreshToken(context);
        return TextUtils.isEmpty(refreshToken);
    }

    public void refreshAccountInfo() throws JandiNetworkException {

////        JacksonConverter converter = new JacksonConverter(new ObjectMapper());
////
////        RestAdapter restAdapter = new RestAdapter.Builder()
////                .setRequestInterceptor(request -> {
////                    request.addHeader("Accept", "application/vnd.tosslab.jandi-v2+json");
////                    request.addHeader("Authorization", TokenUtil.getRequestAuthentication(context).getHeaderValue());
////                })
////                .setConverter(converter)
////                .setEndpoint("http://i2.jandi.io:8888/inner-api")
////                .build();
////        TestAccountInfoService testAccountInfoService = restAdapter.create(TestAccountInfoService.class);
////        ResAccountInfo resAccountInfo = testAccountInfoService.getAccountInfo();
//        LogUtil.d("ResAccountInfo.name : " + resAccountInfo.getName());

//        JandiRestClient_ jandiRestClient = new JandiRestClient_(context);
//        jandiRestClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
//        ResAccountInfo resAccountInfo = jandiRestClient.getAccountInfo();

        AccountInfoRequest accountInfoRequest = AccountInfoRequest.create(context);
        RequestManager<ResAccountInfo> requestManager = RequestManager.newInstance(context, accountInfoRequest);
        ResAccountInfo resAccountInfo = requestManager.request();

        //TokenUtil.getRequestAuthentication(context)
//        TokenUtil.getRequestAuthentication(context).getHeaderValue();

        JandiAccountDatabaseManager.getInstance(context).upsertAccountAllInfo(resAccountInfo);


    }

    public void clearTokenInfo() {
        TokenUtil.clearTokenInfo(context);
    }

    public void clearAccountInfo() {
        JandiAccountDatabaseManager.getInstance(context).deleteAccountDevices();
        JandiAccountDatabaseManager.getInstance(context).deleteAccountEmails();
        JandiAccountDatabaseManager.getInstance(context).deleteAccountInfo();
        JandiAccountDatabaseManager.getInstance(context).deleteAccountTeams();
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

    public boolean hasOldToken() {

        String myToken = JandiPreference.getMyToken(context);

        return !TextUtils.isEmpty(myToken);
    }

    public void removeOldToken() {
        JandiPreference.clearMyToken(context);
    }

    public boolean refreshEntityInfo() {
        LogUtil.d("IntroActivityModel.refreshEntityInfo");
        ResAccountInfo.UserTeam selectedTeamInfo = JandiAccountDatabaseManager.getInstance(context).getSelectedTeamInfo();

        if (selectedTeamInfo == null) {
            return false;
        }
        LogUtil.d("IntroActivityModel.refreshEntityInfo1");
        JandiEntityClient_ jandiEntityClient = JandiEntityClient_.getInstance_(context);

//        JandiRestClient_ jandiRestClient = new JandiRestClient_(context);
//        jandiRestClient.setAuthentication(TokenUtil.getRequestAuthentication(context));

        JacksonConverter converter = new JacksonConverter(new ObjectMapper());
//
//        int selectedTeamId = selectedTeamInfo.getTeamId();
//
//        RestAdapter restAdapter = new RestAdapter.Builder()
//                .setRequestInterceptor(request -> {
//                    request.addHeader("Accept", "application/vnd.tosslab.jandi-v2+json");
//                    request.addHeader("Authorization", TokenUtil.getRequestAuthentication(context).getHeaderValue());
//                })
//                .setConverter(converter)
//                .setEndpoint("http://i2.jandi.io:8888/inner-api")
//                .build();
//        TestAccountInfoService testAccountInfoService = restAdapter.create(TestAccountInfoService.class);


//        LogUtil.d("IntroActivityModel.refreshEntityInfo2");
//
        try {
//        ResLeftSideMenu resLeftSideMenu = testAccountInfoService.getInfosForSideMenu(selectedTeamId);
//            ResLeftSideMenu totalEntitiesInfo = resLeftSideMenu;
//            ResLeftSideMenu totalEntitiesInfo = jandiRestClient.getInfosForSideMenu(selectedTeamId);
            ResLeftSideMenu totalEntitiesInfo = jandiEntityClient.getTotalEntitiesInfo();
            LogUtil.d("IntroActivityModel.refreshEntityInfo3");
            JandiEntityDatabaseManager.getInstance(context).upsertLeftSideMenu(totalEntitiesInfo);
            LogUtil.d("IntroActivityModel.refreshEntityInfo4");
            int totalUnreadCount = BadgeUtils.getTotalUnreadCount(totalEntitiesInfo);
            LogUtil.d("IntroActivityModel.refreshEntityInfo5");
            JandiPreference.setBadgeCount(context, totalUnreadCount);
            LogUtil.d("IntroActivityModel.refreshEntityInfo6");
            BadgeUtils.setBadge(context, totalUnreadCount);
            LogUtil.d("IntroActivityModel.refreshEntityInfo.infrontof");
            EntityManager.getInstance(context).refreshEntity(context);

            return true;
//
        } catch (JandiNetworkException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }


    }

    public ResConfig getConfigInfo() throws JandiNetworkException {
        return jandiAuthClient.getConfig();
    }

    public boolean hasSelectedTeam() {
        ResAccountInfo.UserTeam mySelectedTeam = JandiAccountDatabaseManager.getInstance(context).getSelectedTeamInfo();

        return mySelectedTeam != null;
    }

    @Background
    public void updateParseForAllTeam() {



    }
}

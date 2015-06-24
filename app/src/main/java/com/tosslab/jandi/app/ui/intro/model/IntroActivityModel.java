package com.tosslab.jandi.app.ui.intro.model;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.local.database.entity.JandiEntityDatabaseManager;
import com.tosslab.jandi.app.network.client.JandiAuthClient;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResConfig;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import retrofit.RetrofitError;

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

        ResAccountInfo resAccountInfo = RequestApiManager.getInstance().getAccountInfoByMainRest();
//        JandiRestV2Client jandiRestClient = RestAdapterFactory.getRestAdapter(JandiConstants.REST_TYPE_AUTH).create(JandiRestV2Client.class);
//        ResAccountInfo resAccountInfo = RequestManager.newInstance(context.getApplicationContext(),
//                () -> jandiRestClient.getAccountInfo()).request();
        JandiAccountDatabaseManager.getInstance(context.getApplicationContext()).upsertAccountAllInfo(resAccountInfo);
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
        ResAccountInfo.UserTeam selectedTeamInfo = JandiAccountDatabaseManager.getInstance(context).getSelectedTeamInfo();
        if (selectedTeamInfo == null) {
            return false;
        }
        try {
            int selectedTeamId = selectedTeamInfo.getTeamId();
            for (int i = 0; i < 1000; i++) {
                Thread t = new Thread() {
                    @Override
                    public void run() {
                        RequestApiManager.getInstance().getInfosForSideMenuByMainRest(selectedTeamId);
                    }
                };

                t.start();

            }
            ResLeftSideMenu totalEntitiesInfo = RequestApiManager.getInstance().getInfosForSideMenuByMainRest(selectedTeamId);
//            JandiRestV2Client jandiRestClient = RestAdapterFactory.getRestAdapter(JandiConstants.REST_TYPE_AUTH).create(JandiRestV2Client.class);
//            ResLeftSideMenu totalEntitiesInfo = RequestManager.newInstance(context.getApplicationContext(),
//                    () -> {
//                        return jandiRestClient.getInfosForSideMenu(selectedTeamId);
//                    }).request();
            JandiEntityDatabaseManager.getInstance(context.getApplicationContext()).upsertLeftSideMenu(totalEntitiesInfo);
            int totalUnreadCount = BadgeUtils.getTotalUnreadCount(totalEntitiesInfo);
            JandiPreference.setBadgeCount(context.getApplicationContext(), totalUnreadCount);
            BadgeUtils.setBadge(context.getApplicationContext(), totalUnreadCount);
            EntityManager.getInstance(context.getApplicationContext()).refreshEntity(context.getApplicationContext());
            return true;
        } catch (RetrofitError e) {
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

package com.tosslab.jandi.app.ui.intro.model;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.local.database.entity.JandiEntityDatabaseManager;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResConfig;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.lib.sprinkler.Sprinkler;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import retrofit.RetrofitError;

/**
 * Created by Steve SeongUg Jung on 14. 12. 3..
 */

@EBean
public class IntroActivityModel {

    /**
     * Check new app version
     */
    public boolean checkNewVersion(Context context) {
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
        } catch (RetrofitError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
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

    int getLatestVersionInBackground() throws RetrofitError {
        ResConfig resConfig = getConfigInfo();
        return resConfig.versions.android;
    }

    public boolean isNeedLogin(Context context) {
        String refreshToken = JandiPreference.getRefreshToken(context);
        return TextUtils.isEmpty(refreshToken);
    }

    public void refreshAccountInfo(Context context) throws RetrofitError {

        ResAccountInfo resAccountInfo = RequestApiManager.getInstance().getAccountInfoByMainRest();
        JandiAccountDatabaseManager.getInstance(context.getApplicationContext())
                .upsertAccountAllInfo(resAccountInfo);
    }

    public void clearTokenInfo() {
        TokenUtil.clearTokenInfo();
    }

    public void clearAccountInfo(Context context) {
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

    public boolean hasOldToken(Context context) {
        String myToken = JandiPreference.getMyToken(context);
        return !TextUtils.isEmpty(myToken);
    }

    public void removeOldToken(Context context) {
        JandiPreference.clearMyToken(context);
    }

    public boolean refreshEntityInfo(Context context) {
        ResAccountInfo.UserTeam selectedTeamInfo =
                JandiAccountDatabaseManager.getInstance(context).getSelectedTeamInfo();
        if (selectedTeamInfo == null) {
            return false;
        }
        try {
            int selectedTeamId = selectedTeamInfo.getTeamId();
            ResLeftSideMenu totalEntitiesInfo =
                    RequestApiManager.getInstance().getInfosForSideMenuByMainRest(selectedTeamId);
            JandiEntityDatabaseManager.getInstance(context.getApplicationContext())
                    .upsertLeftSideMenu(totalEntitiesInfo);

            int totalUnreadCount = BadgeUtils.getTotalUnreadCount(totalEntitiesInfo);
            JandiPreference.setBadgeCount(context.getApplicationContext(), totalUnreadCount);
            BadgeUtils.setBadge(context.getApplicationContext(), totalUnreadCount);
            EntityManager.getInstance(context.getApplicationContext())
                    .refreshEntity(context.getApplicationContext());
            return true;
        } catch (RetrofitError e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public ResConfig getConfigInfo() throws RetrofitError {
        return RequestApiManager.getInstance().getConfigByMainRest();
    }

    public boolean hasSelectedTeam(Context context) {
        return JandiAccountDatabaseManager.getInstance(context).getSelectedTeamInfo() != null;
    }

    private String getAccountId(Context context) {
        ResAccountInfo accountInfo =
                JandiAccountDatabaseManager.getInstance(context).getAccountInfo();
        String accountId = accountInfo != null ? accountInfo.getId() : null;
        return accountId;
    }

    private int getMemberId(Context context) {
        ResAccountInfo.UserTeam selectedTeamInfo =
                JandiAccountDatabaseManager.getInstance(context).getSelectedTeamInfo();
        int memberId = selectedTeamInfo != null ? selectedTeamInfo.getMemberId() : -1;
        return memberId;
    }

    public void trackAutoSignInSuccessAndFlush(Context context, boolean hasTeamSelected) {
        String accountId = getAccountId(context);
        int memberId = getMemberId(context);
        FutureTrack.Builder builder = new FutureTrack.Builder()
                .event(Event.SignIn)
                .accountId(accountId)
                .property(PropertyKey.ResponseSuccess, true)
                .property(PropertyKey.AutoSignIn, true);

        if (hasTeamSelected) {
            builder.memberId(memberId);
        }

        Sprinkler.with(context)
                .track(builder.build())
                .flush();
    }

    public void trackSignInFailAndFlush(Context context, int errorCode) {
        String accountId = getAccountId(context);
        int memberId = getMemberId(context);
        Sprinkler.with(context)
                .track(new FutureTrack.Builder()
                        .event(Event.SignIn)
                        .accountId(accountId)
                        .memberId(memberId)
                        .property(PropertyKey.ResponseSuccess, false)
                        .property(PropertyKey.ErrorCode, errorCode)
                        .build())
                .flush();
    }
}

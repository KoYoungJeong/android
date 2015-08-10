package com.tosslab.jandi.app.ui.intro.model;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.database.DatabaseConsts;
import com.tosslab.jandi.app.local.database.JandiDatabaseOpenHelper;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.LeftSideMenuRepository;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResConfig;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.lib.sprinkler.Sprinkler;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

import org.androidannotations.annotations.EBean;

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
        AccountRepository.getRepository().upsertAccountAllInfo(resAccountInfo);
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
                AccountRepository.getRepository().getSelectedTeamInfo();
        if (selectedTeamInfo == null) {
            return false;
        }
        try {
            int selectedTeamId = selectedTeamInfo.getTeamId();
            ResLeftSideMenu totalEntitiesInfo =
                    RequestApiManager.getInstance().getInfosForSideMenuByMainRest(selectedTeamId);
            LeftSideMenuRepository.getRepository().upsertLeftSideMenu(totalEntitiesInfo);
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

    public boolean hasMigration() {
        ResAccountInfo accountInfo = AccountRepository.getRepository().getAccountInfo();
        return accountInfo != null && !TextUtils.isEmpty(accountInfo.getId());
    }

    public long setSelectedTeamId(int teamId) {
        return AccountRepository.getRepository().updateSelectedTeamInfo(teamId);
    }

    public int getSelectedTeamInfoByOldData(Context context) {
        SQLiteDatabase database = JandiDatabaseOpenHelper.getInstance(context).getReadableDatabase();
        String[] columns = {DatabaseConsts.AccountTeam.teamId.name()};
        String selection = DatabaseConsts.AccountTeam.selected.name() + " = 1";
        Cursor cursor = database.query(DatabaseConsts.Table.account_team.name(), columns, selection, null, null, null, null);

        if (cursor == null || cursor.getCount() <= 0) {
            return 0;
        }

        cursor.moveToFirst();
        return cursor.getInt(0);
    }
    
    public void trackAutoSignInSuccessAndFlush(boolean hasTeamSelected) {
        FutureTrack.Builder builder = new FutureTrack.Builder()
                .event(Event.SignIn)
                .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                .property(PropertyKey.ResponseSuccess, true)
                .property(PropertyKey.AutoSignIn, true);

        if (hasTeamSelected) {
            builder.memberId(AccountUtil.getMemberId(JandiApplication.getContext()));
        }

        Sprinkler.with(JandiApplication.getContext())
                .track(builder.build())
                .flush();
    }

    public void trackSignInFailAndFlush(int errorCode) {
        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.SignIn)
                        .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                        .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                        .property(PropertyKey.ResponseSuccess, false)
                        .property(PropertyKey.ErrorCode, errorCode)
                        .build())
                .flush();
    }
}

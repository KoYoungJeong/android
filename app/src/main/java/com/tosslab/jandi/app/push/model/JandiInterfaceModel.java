package com.tosslab.jandi.app.push.model;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.LeftSideMenuRepository;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.client.EntityClientManager_;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResConfig;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.JandiPreference;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SystemService;

import java.util.List;

import retrofit.RetrofitError;

/**
 * Created by Steve SeongUg Jung on 15. 1. 26..
 */
@EBean
public class JandiInterfaceModel {

    @RootContext
    Context context;

    @SystemService
    ActivityManager activityManager;

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

    public ResConfig getConfigInfo() throws RetrofitError {
        return RequestApiManager.getInstance().getConfigByMainRest();
    }

    public void refreshAccountInfo() throws RetrofitError {
        ResAccountInfo resAccountInfo = RequestApiManager.getInstance().getAccountInfoByMainRest();

        AccountRepository.getRepository().upsertAccountAllInfo(resAccountInfo);

        EntityClientManager entityClientManager = EntityClientManager_.getInstance_(context);
        ResLeftSideMenu totalEntitiesInfo = entityClientManager.getTotalEntitiesInfo();
        LeftSideMenuRepository.getRepository().upsertLeftSideMenu(totalEntitiesInfo);
        int totalUnreadCount = BadgeUtils.getTotalUnreadCount(totalEntitiesInfo);
        JandiPreference.setBadgeCount(context, totalUnreadCount);
        BadgeUtils.setBadge(context, totalUnreadCount);

    }

    public boolean hasBackStackActivity() {

        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(1);

        return tasks != null && tasks.size() > 0 && tasks.get(0).numActivities > 1;
    }

    public boolean hasTeamInfo(int teamId) {
        ResAccountInfo.UserTeam teamInfo = AccountRepository.getRepository().getTeamInfo(teamId);

        if (teamInfo == null) {

            try {
                refreshAccountInfo();
                teamInfo = AccountRepository.getRepository().getTeamInfo(teamId);

                if (teamInfo == null) {
                    return false;
                }

            } catch (RetrofitError e) {
                e.printStackTrace();
                return false;
            }

        }
        return true;
    }

    public boolean setupSelectedTeam(int teamId) {

        ResAccountInfo.UserTeam selectedTeamInfo = AccountRepository.getRepository().getSelectedTeamInfo();

        if ((selectedTeamInfo == null || selectedTeamInfo.getTeamId() != teamId)) {

            AccountRepository.getRepository().updateSelectedTeamInfo(teamId);

            return getEntityInfo();

        } else {

            if (hasBackStackActivity()) {
                return true;
            }

            try {
                EntityManager.getInstance(context);
                return true;
            } catch (Exception e) {
                return getEntityInfo();
            }
        }
    }

    private boolean getEntityInfo() {
        try {
            EntityClientManager entityClientManager = EntityClientManager_.getInstance_(context);
            ResLeftSideMenu totalEntitiesInfo = entityClientManager.getTotalEntitiesInfo();
            LeftSideMenuRepository.getRepository().upsertLeftSideMenu(totalEntitiesInfo);
            int totalUnreadCount = BadgeUtils.getTotalUnreadCount(totalEntitiesInfo);
            JandiPreference.setBadgeCount(context, totalUnreadCount);
            BadgeUtils.setBadge(context, totalUnreadCount);
            EntityManager.getInstance(context).refreshEntity(totalEntitiesInfo);
            return true;
        } catch (RetrofitError e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

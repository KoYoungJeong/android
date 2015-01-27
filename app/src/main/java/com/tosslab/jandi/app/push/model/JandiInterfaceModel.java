package com.tosslab.jandi.app.push.model;

import android.app.ActivityManager;
import android.content.Context;

import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.local.database.entity.JandiEntityDatabaseManager;
import com.tosslab.jandi.app.network.client.JandiEntityClient;
import com.tosslab.jandi.app.network.client.JandiEntityClient_;
import com.tosslab.jandi.app.network.manager.RequestManager;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.ui.team.select.model.AccountInfoRequest;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.JandiPreference;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SystemService;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 1. 26..
 */
@EBean
public class JandiInterfaceModel {

    private static final Logger logger = Logger.getLogger(JandiInterfaceModel.class);


    @RootContext
    Context context;

    @SystemService
    ActivityManager activityManager;

    public void refreshAccountInfo() throws JandiNetworkException {
        AccountInfoRequest accountInfoRequest = AccountInfoRequest.create(context);
        RequestManager<ResAccountInfo> requestManager = RequestManager.newInstance(context, accountInfoRequest);
        ResAccountInfo resAccountInfo = requestManager.request();

        JandiAccountDatabaseManager.getInstance(context).upsertAccountInfo(resAccountInfo);
        JandiAccountDatabaseManager.getInstance(context).upsertAccountEmail(resAccountInfo.getEmails());
        JandiAccountDatabaseManager.getInstance(context).upsertAccountTeams(resAccountInfo.getMemberships());
        JandiAccountDatabaseManager.getInstance(context).upsertAccountDevices(resAccountInfo.getDevices());


        JandiEntityClient jandiEntityClient = JandiEntityClient_.getInstance_(context);
        ResLeftSideMenu totalEntitiesInfo = jandiEntityClient.getTotalEntitiesInfo();
        JandiEntityDatabaseManager.getInstance(context).upsertLeftSideMenu(totalEntitiesInfo);
        int totalUnreadCount = BadgeUtils.getTotalUnreadCount(totalEntitiesInfo);
        JandiPreference.setBadgeCount(context, totalUnreadCount);
        BadgeUtils.setBadge(context, totalUnreadCount);

    }

    public boolean hasBackStackActivity() {

        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(1);

        return tasks != null && tasks.size() > 0 && tasks.get(0).numActivities > 1;
    }

    public boolean hasTeamInfo(int teamId) {
        ResAccountInfo.UserTeam teamInfo = JandiAccountDatabaseManager.getInstance(context).getTeamInfo(teamId);

        if (teamInfo == null) {

            try {
                refreshAccountInfo();
                teamInfo = JandiAccountDatabaseManager.getInstance(context).getTeamInfo(teamId);

                if (teamInfo == null) {
                    return false;
                }

            } catch (JandiNetworkException e) {
                logger.error("Get Account Info Fail : " + e.getErrorInfo() + " : " + e.httpBody, e);
                return false;
            }

        }
        return true;
    }

    public boolean setupSelectedTeam(int teamId) {

        ResAccountInfo.UserTeam selectedTeamInfo = JandiAccountDatabaseManager.getInstance(context).getSelectedTeamInfo();

        if ((selectedTeamInfo == null || selectedTeamInfo.getTeamId() != teamId)) {

            JandiAccountDatabaseManager.getInstance(context).updateSelectedTeam(teamId);
            JandiEntityClient jandiEntityClient = JandiEntityClient_.getInstance_(context);

            try {
                ResLeftSideMenu totalEntitiesInfo = jandiEntityClient.getTotalEntitiesInfo();
                JandiEntityDatabaseManager.getInstance(context).upsertLeftSideMenu(totalEntitiesInfo);
                int totalUnreadCount = BadgeUtils.getTotalUnreadCount(totalEntitiesInfo);
                JandiPreference.setBadgeCount(context, totalUnreadCount);
                BadgeUtils.setBadge(context, totalUnreadCount);
                EntityManager.getInstance(context).refreshEntity(totalEntitiesInfo);

                return true;
            } catch (JandiNetworkException e) {
                logger.error("Get Entity Info Fail : " + e.getErrorInfo() + " : " + e.httpBody, e);
                return false;
            }

        } else {
            return true;
        }
    }
}

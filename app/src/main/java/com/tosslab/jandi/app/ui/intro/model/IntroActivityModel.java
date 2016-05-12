package com.tosslab.jandi.app.ui.intro.model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.database.DatabaseConsts;
import com.tosslab.jandi.app.local.database.JandiDatabaseOpenHelper;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.LeftSideMenuRepository;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.network.client.account.AccountApi;
import com.tosslab.jandi.app.network.client.main.ConfigApi;
import com.tosslab.jandi.app.network.client.main.LeftSideApi;
import com.tosslab.jandi.app.network.dagger.DaggerApiClientComponent;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResConfig;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.ApplicationUtil;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;

import java.util.Collection;

import javax.inject.Inject;

import dagger.Lazy;
import rx.Observable;

/**
 * Created by Steve SeongUg Jung on 14. 12. 3..
 */

@EBean
public class IntroActivityModel {

    @Inject
    Lazy<AccountApi> accountApi;
    @Inject
    Lazy<LeftSideApi> leftSideApi;
    @Inject
    Lazy<ConfigApi> configApi;

    @AfterInject
    void initObject() {
        DaggerApiClientComponent
                .create()
                .inject(this);
    }

    public boolean isNetworkConnected() {
        return NetworkCheckUtil.isConnected();
    }

    public int getInstalledAppVersion() {
        return ApplicationUtil.getAppVersionCode();
    }

    public boolean isNeedLogin() {
        return TextUtils.isEmpty(TokenUtil.getRefreshToken());
    }

    public void refreshAccountInfo() throws RetrofitException {

        ResAccountInfo resAccountInfo = accountApi.get().getAccountInfo();
        AccountUtil.removeDuplicatedTeams(resAccountInfo);
        AccountRepository.getRepository().upsertAccountAllInfo(resAccountInfo);

        Collection<ResAccountInfo.UserTeam> teamList = resAccountInfo.getMemberships();

        Observable.from(teamList)
                .map(ResAccountInfo.UserTeam::getUnread)
                .reduce((prev, current) -> prev + current)
                .subscribe(total -> {
                    BadgeUtils.setBadge(JandiApplication.getContext(), total);
                });

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
            long selectedTeamId = selectedTeamInfo.getTeamId();
            ResLeftSideMenu totalEntitiesInfo =
                    leftSideApi.get().getInfosForSideMenu(selectedTeamId);
            LeftSideMenuRepository.getRepository().upsertLeftSideMenu(totalEntitiesInfo);
            EntityManager.getInstance().refreshEntity();
            return true;
        } catch (RetrofitException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public ResConfig getConfigInfo() throws RetrofitException {
        return configApi.get().getConfig();
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

        AnalyticsUtil.trackSprinkler(builder.build());
        AnalyticsUtil.flushSprinkler();
    }

    public void trackSignInFailAndFlush(int errorCode) {
        AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                .event(Event.SignIn)
                .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                .property(PropertyKey.ResponseSuccess, false)
                .property(PropertyKey.ErrorCode, errorCode)
                .build());
        AnalyticsUtil.flushSprinkler();
    }

    public boolean hasLeftSideMenu() {
        return LeftSideMenuRepository.getRepository().getCurrentLeftSideMenu() != null;
    }

    public boolean hasSelectedTeam() {
        ResAccountInfo.UserTeam selectedTeamInfo = AccountRepository.getRepository().getSelectedTeamInfo();
        return selectedTeamInfo != null;
    }

    public int clearLinkRepository() {
        return MessageRepository.getRepository().deleteAllLink();
    }
}

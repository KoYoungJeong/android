package com.tosslab.jandi.app.ui.maintab.navigation.model;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.view.SupportMenuInflater;
import android.support.v7.view.menu.MenuBuilder;
import android.text.TextUtils;
import android.util.Log;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.InitialInfoRepository;
import com.tosslab.jandi.app.network.client.account.AccountApi;
import com.tosslab.jandi.app.network.client.invitation.InvitationApi;
import com.tosslab.jandi.app.network.client.main.LoginApi;
import com.tosslab.jandi.app.network.client.start.StartApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqInvitationAcceptOrIgnore;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResPendingTeamInfo;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.network.models.start.InitialInfo;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.team.select.to.Team;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import dagger.Lazy;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by tonyjs on 2016. 8. 17..
 */
public class NavigationModel {

    private final Lazy<AccountApi> accountApi;
    private final Lazy<StartApi> startApi;
    private final Lazy<InvitationApi> invitationApi;
    private final Lazy<LoginApi> loginApi;

    public NavigationModel(Lazy<AccountApi> accountApi,
                           Lazy<StartApi> startApi,
                           Lazy<InvitationApi> invitationApi,
                           Lazy<LoginApi> loginApi) {
        this.accountApi = accountApi;
        this.startApi = startApi;
        this.invitationApi = invitationApi;
        this.loginApi = loginApi;
    }

    public User getMe() {
        return TeamInfoLoader.getInstance().getUser(TeamInfoLoader.getInstance().getMyId());
    }

    public MenuBuilder getNavigationMenus() {
        Context context = JandiApplication.getContext();
        MenuBuilder menuBuilder = new MenuBuilder(context);
        SupportMenuInflater menuInflater = new SupportMenuInflater(context);
        menuInflater.inflate(R.menu.main_tab_navigation, menuBuilder);
        return menuBuilder;
    }

    public boolean isPhoneMode() {
        return JandiApplication.getContext().getResources().getBoolean(R.bool.portrait_only);
    }

    public void refreshAccountInfo() {
        try {
            ResAccountInfo resAccountInfo = accountApi.get().getAccountInfo();
            AccountUtil.removeDuplicatedTeams(resAccountInfo);
            AccountRepository.getRepository().upsertAccountAllInfo(resAccountInfo);
        } catch (RetrofitException retrofitError) {
            LogUtil.e(Log.getStackTraceString(retrofitError));
        }
    }

    public Observable<List<Team>> getTeamsObservable() {
        return Observable.from(AccountRepository.getRepository().getAccountTeams())
                .map(Team::createTeam)
                .collect(ArrayList::new, List::add);
    }

    public List<Team> getPendingTeams() {
        ArrayList<Team> teams = new ArrayList<>();
        try {
            List<ResPendingTeamInfo> pendingTeamInfoByInvitationApi =
                    invitationApi.get().getPedingTeamInfo();
            Observable.from(pendingTeamInfoByInvitationApi)
                    .filter(resPendingTeamInfo ->
                            TextUtils.equals("pending", resPendingTeamInfo.getStatus()))
                    .map(Team::createTeam)
                    .subscribe(teams::add);

        } catch (RetrofitException e) {
            LogUtil.e(Log.getStackTraceString(e));
        }
        return teams;
    }

    public Observable<Object> getUpdateEntityInfoObservable(final long teamId) {
        return Observable.create(subscriber -> {
            AccountRepository.getRepository().updateSelectedTeamInfo(teamId);

            if (!InitialInfoRepository.getInstance().hasInitialInfo(teamId)) {
                try {
                    InitialInfo initializeInfo = startApi.get().getInitializeInfo(teamId);
                    InitialInfoRepository.getInstance().upsertInitialInfo(initializeInfo);
                    JandiPreference.setSocketConnectedLastTime(initializeInfo.getTs());
                } catch (Exception error) {
                    subscriber.onError(error);
                    return;
                }
            }

            MessageRepository.getRepository().deleteAllLink();
            TeamInfoLoader.getInstance().refresh();
            subscriber.onNext(new Object());
            subscriber.onCompleted();
        });
    }

    public Observable<ResAccountInfo.UserTeam> getSelectedTeamObservable() {
        return Observable.just(AccountRepository.getRepository().getSelectedTeamInfo());
    }

    public Observable<ResTeamDetailInfo> getInviteDecisionObservable(String invitationId, String type) {
        return Observable.<ResTeamDetailInfo>create(subscriber -> {
            try {
                ReqInvitationAcceptOrIgnore requestBody = new ReqInvitationAcceptOrIgnore(type);
                ResTeamDetailInfo resTeamDetailInfo = invitationApi.get().
                        acceptOrDeclineInvitation(invitationId, requestBody);

                subscriber.onNext(resTeamDetailInfo);
            } catch (RetrofitException error) {
                subscriber.onError(error);
            }
            subscriber.onCompleted();
        }).subscribeOn(Schedulers.io());

    }

    public Observable<Object> getUpdateTeamInfoObservable(final long teamId) {
        return Observable.create(subscriber -> {
            try {
                ResAccountInfo resAccountInfo =
                        accountApi.get().getAccountInfo();

                AccountUtil.removeDuplicatedTeams(resAccountInfo);
                AccountRepository.getRepository().upsertAccountAllInfo(resAccountInfo);
                AccountRepository.getRepository().updateSelectedTeamInfo(teamId);

                subscriber.onNext(new Object());
            } catch (Exception error) {
                subscriber.onError(error);
            }
            subscriber.onCompleted();
        }).subscribeOn(Schedulers.io());
    }

    public String getTeamInviteErrorMessage(RetrofitException e, String teamName) {
        Resources resources = JandiApplication.getContext().getResources();
        String errorMessage = resources.getString(R.string.err_network);

        int errorCode = e.getResponseCode();
        switch (errorCode) {
            case JandiConstants.TeamInviteErrorCode.NOT_AVAILABLE_INVITATION_CODE:
                resources.getString(R.string.jandi_expired_invitation_link);
                break;
            case JandiConstants.TeamInviteErrorCode.DISABLED_MEMBER:
                resources.getString(R.string.jandi_disabled_team, teamName);
                break;
            case JandiConstants.TeamInviteErrorCode.REMOVED_TEAM:
                resources.getString(R.string.jandi_deleted_team);
                break;
            case JandiConstants.TeamInviteErrorCode.TEAM_INVITATION_DISABLED:
                resources.getString(R.string.jandi_invite_disabled, "");
                break;
            case JandiConstants.TeamInviteErrorCode.ENABLED_MEMBER:
                resources.getString(R.string.jandi_deleted_team);
                break;
        }

        return errorMessage;
    }

    public boolean isCurrentTeam(long teamId) {
        return TeamInfoLoader.getInstance().getTeamId() == teamId;
    }

    public Observable<ResCommon> getSignOutObservable() {
        return Observable.defer(() -> {
            String deviceId = TokenUtil.getTokenObject().getDeviceId();
            // deviceId 가 없는 경우에 대한 방어코드, deviceId 가 비어 있는 경우 400 error 가 떨어짐.
            // UUID RFC4122 규격 맞춘 아무 값이나 필요
            if (TextUtils.isEmpty(deviceId)) {
                deviceId = UUID.randomUUID().toString();
            }

            return Observable.just(deviceId);
        })
                .observeOn(Schedulers.io())
                .concatMap(deviceId -> {
                    try {
                        ResCommon resCommon = loginApi.get()
                                .deleteToken(TokenUtil.getRefreshToken(), deviceId);
                        return Observable.just(resCommon);
                    } catch (RetrofitException e) {
                        LogUtil.d(e.getCause().getMessage());
                        return Observable.error(e);
                    }
                });
    }
}

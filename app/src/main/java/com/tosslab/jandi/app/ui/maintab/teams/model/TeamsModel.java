package com.tosslab.jandi.app.ui.maintab.teams.model;

import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Pair;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.InitialInfoRepository;
import com.tosslab.jandi.app.network.client.account.AccountApi;
import com.tosslab.jandi.app.network.client.invitation.InvitationApi;
import com.tosslab.jandi.app.network.client.start.StartApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqInvitationAcceptOrIgnore;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResPendingTeamInfo;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.network.models.start.InitialInfo;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.team.select.to.Team;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import java.util.List;

import dagger.Lazy;
import rx.Observable;
import rx.schedulers.Schedulers;

public class TeamsModel {

    Lazy<AccountApi> accountApi;
    Lazy<StartApi> startApi;
    Lazy<InvitationApi> invitationApi;

    public TeamsModel(Lazy<AccountApi> accountApi,
                      Lazy<StartApi> startApi,
                      Lazy<InvitationApi> invitationApi) {

        this.accountApi = accountApi;
        this.startApi = startApi;
        this.invitationApi = invitationApi;
    }

    public boolean isNetworkConnected() {
        return NetworkCheckUtil.isConnected();
    }

    public Observable<Object> getRefreshAccountInfoObservable() {
        return Observable.create(subscriber -> {
            try {
                ResAccountInfo resAccountInfo = accountApi.get().getAccountInfo();
                AccountUtil.removeDuplicatedTeams(resAccountInfo);
                AccountRepository.getRepository().upsertAccountAllInfo(resAccountInfo);
                subscriber.onNext(new Object());
            } catch (RetrofitException retrofitError) {
                subscriber.onError(retrofitError);
            }
            subscriber.onCompleted();
        });
    }

    public Observable<List<Team>> getTeamsObservable(final List<Team> teams) {
        return Observable.from(AccountRepository.getRepository().getAccountTeams())
                .map(Team::createTeam)
                .collect(() -> teams, List::add);
    }

    public Observable<List<Team>> getPendingTeamsObservable(final List<Team> teams) {
        Observable.OnSubscribe<List<ResPendingTeamInfo>> subscribe = subscriber -> {
            try {
                List<ResPendingTeamInfo> pendingTeamInfoByInvitationApi =
                        invitationApi.get().getPedingTeamInfo();

                subscriber.onNext(pendingTeamInfoByInvitationApi);

            } catch (RetrofitException error) {
                subscriber.onError(error);
            }
            subscriber.onCompleted();
        };

        return Observable.create(subscribe)
                .concatMap(Observable::from)
                .filter(resPendingTeamInfo ->
                        TextUtils.equals("pending", resPendingTeamInfo.getStatus()))
                .map(Team::createTeam)
                .collect(() -> teams, List::add);
    }

    public Observable<List<Team>> getSortedTeamListObservable(List<Team> teams) {
        return Observable.from(teams)
                .toSortedList((team, team2) -> team.getStatus() == Team.Status.PENDING ? -1 : 1);
    }

    public Observable<Pair<Long, List<Team>>> getCheckSelectedTeamObservable(final List<Team> teams) {
        return Observable.<Pair<Long, List<Team>>>create(subscriber -> {
            ResAccountInfo.UserTeam selectedTeamInfo =
                    AccountRepository.getRepository().getSelectedTeamInfo();
            Observable.from(teams)
                    .filter(team -> selectedTeamInfo.getTeamId() == team.getTeamId())
                    .subscribe(team -> team.setSelected(true));
            subscriber.onNext(Pair.create(selectedTeamInfo.getTeamId(), teams));
            subscriber.onCompleted();
        });
    }

    public Observable<Object> getUpdateEntityInfoObservable(final long teamId) {
        return Observable.create(subscriber -> {
            try {
                AccountRepository.getRepository().updateSelectedTeamInfo(teamId);
                InitialInfo initializeInfo = startApi.get().getInitializeInfo(teamId);
                InitialInfoRepository.getInstance().upsertInitialInfo(initializeInfo);
                TeamInfoLoader.getInstance().refresh();
                subscriber.onNext(new Object());
            } catch (Exception error) {
                subscriber.onError(error);
            }
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
}

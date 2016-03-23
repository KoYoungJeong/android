package com.tosslab.jandi.app.ui.maintab.teams.model;

import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Pair;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.BadgeCountRepository;
import com.tosslab.jandi.app.local.orm.repositories.LeftSideMenuRepository;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqInvitationAcceptOrIgnore;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResPendingTeamInfo;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.ui.team.select.to.Team;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import java.util.List;

import retrofit.RetrofitError;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by tonyjs on 16. 3. 21..
 */
public class TeamsModel {

    public boolean isNetworkConnected() {
        return NetworkCheckUtil.isConnected();
    }

    public Observable<Object> getRefreshAccountInfoObservable() {
        return Observable.create(subscriber -> {
            try {
                ResAccountInfo resAccountInfo = RequestApiManager.getInstance().getAccountInfoByMainRest();
                AccountRepository.getRepository().upsertAccountAllInfo(resAccountInfo);
                subscriber.onNext(new Object());
            } catch (RetrofitError retrofitError) {
                subscriber.onError(retrofitError);
            }
            subscriber.onCompleted();
        }).subscribeOn(Schedulers.io());
    }

    public Observable<List<Team>> getTeamsObservable(final List<Team> teams) {
        return Observable.from(AccountRepository.getRepository().getAccountTeams())
                .subscribeOn(Schedulers.io())
                .map(Team::createTeam)
                .collect(() -> teams, List::add);
    }

    public Observable<List<Team>> getPendingTeamsObservable(final List<Team> teams) {
        Observable.OnSubscribe<List<ResPendingTeamInfo>> subscribe = subscriber -> {
            try {
                List<ResPendingTeamInfo> pendingTeamInfoByInvitationApi =
                        RequestApiManager.getInstance().getPendingTeamInfoByInvitationApi();

                subscriber.onNext(pendingTeamInfoByInvitationApi);

            } catch (RetrofitError error) {
                subscriber.onError(error);
            }
            subscriber.onCompleted();

        };

        return Observable.create(subscribe)
                .subscribeOn(Schedulers.io())
                .concatMap(Observable::from)
                .filter(resPendingTeamInfo ->
                        TextUtils.equals("pending", resPendingTeamInfo.getStatus()))
                .map(Team::createTeam)
                .collect(() -> teams, List::add);
    }

    public Observable<List<Team>> getUpdateBadgeCountObservable(final List<Team> teams) {
        return Observable.<List<Team>>create(subscriber -> {
            Observable.from(teams)
                    .filter(team -> team.getStatus() == Team.Status.JOINED)
                    .subscribe(team -> {
                        BadgeCountRepository.getRepository()
                                .upsertBadgeCount(team.getTeamId(), team.getUnread());
                    });
            subscriber.onNext(teams);
            subscriber.onCompleted();
        }).subscribeOn(Schedulers.io());
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
        }).subscribeOn(Schedulers.io());
    }

    public Observable<Object> getUpdateEntityInfoObservable(final long teamId) {
        return Observable.create(subscriber -> {
            try {
                updateSelectedTeam(teamId);

                updateEntityInfo(teamId);

                subscriber.onNext(new Object());
            } catch (RetrofitError error) {
                subscriber.onError(error);
            } catch (Exception error) {
                subscriber.onError(error);
            }
            subscriber.onCompleted();
        }).subscribeOn(Schedulers.io());
    }

    private void updateSelectedTeam(long teamId) {
        AccountRepository.getRepository().updateSelectedTeamInfo(teamId);
    }

    private void updateEntityInfo(long teamId) throws Exception {
        ResLeftSideMenu leftSideMenu =
                RequestApiManager.getInstance().getInfosForSideMenuByMainRest(teamId);

        LeftSideMenuRepository.getRepository().upsertLeftSideMenu(leftSideMenu);
        int totalUnreadCount = BadgeUtils.getTotalUnreadCount(leftSideMenu);
        BadgeCountRepository badgeCountRepository = BadgeCountRepository.getRepository();
        badgeCountRepository.upsertBadgeCount(leftSideMenu.team.id, totalUnreadCount);
        BadgeUtils.setBadge(
                JandiApplication.getContext(), badgeCountRepository.getTotalBadgeCount());
        EntityManager entityManager = EntityManager.getInstance();
        entityManager.refreshEntity();
    }

    public Observable<ResAccountInfo.UserTeam> getSelectedTeamObservable() {
        return Observable.just(AccountRepository.getRepository().getSelectedTeamInfo());
    }

    public Observable<ResTeamDetailInfo> getInviteDecisionObservable(String invitationId, String type) {
        return Observable.<ResTeamDetailInfo>create(subscriber -> {
            try {
                ReqInvitationAcceptOrIgnore requestBody = new ReqInvitationAcceptOrIgnore(type);
                ResTeamDetailInfo resTeamDetailInfo = RequestApiManager.getInstance().
                        acceptOrDeclineInvitationByInvitationApi(invitationId, requestBody);

                subscriber.onNext(resTeamDetailInfo);
            } catch (RetrofitError error) {
                subscriber.onError(error);
            }
            subscriber.onCompleted();
        }).subscribeOn(Schedulers.io());

    }

    public Observable<Object> getUpdateTeamInfoObservable(final long teamId) {
        return Observable.create(subscriber -> {
            try {
                ResAccountInfo resAccountInfo =
                        RequestApiManager.getInstance().getAccountInfoByMainRest();

                AccountRepository.getRepository().upsertAccountAllInfo(resAccountInfo);
                AccountRepository.getRepository().updateSelectedTeamInfo(teamId);

                subscriber.onNext(new Object());
            } catch (Exception error) {
                subscriber.onError(error);
            }
            subscriber.onCompleted();
        }).subscribeOn(Schedulers.io());
    }

    public String getTeamInviteErrorMessage(RetrofitError e, String teamName) {
        Resources resources = JandiApplication.getContext().getResources();
        String errorMessage = resources.getString(R.string.err_network);

        int errorCode = e.getResponse() != null ? e.getResponse().getStatus() : -1;
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
}

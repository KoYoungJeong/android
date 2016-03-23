package com.tosslab.jandi.app.ui.maintab.teams.presenter;

import android.util.Log;

import com.tosslab.jandi.app.network.models.ReqInvitationAcceptOrIgnore;
import com.tosslab.jandi.app.ui.maintab.teams.model.TeamsModel;
import com.tosslab.jandi.app.ui.maintab.teams.view.TeamsView;
import com.tosslab.jandi.app.ui.team.select.to.Team;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import retrofit.RetrofitError;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

/**
 * Created by tonyjs on 16. 3. 21..
 */
public class TeamsPresenterImpl implements TeamsPresenter {

    private final TeamsModel model;
    private final TeamsView view;

    private PublishSubject<Object> teamInitializeQueue;
    private Subscription teamInitializeQueueSubscription;

    @Inject
    public TeamsPresenterImpl(TeamsModel model, TeamsView view) {
        this.model = model;
        this.view = view;

        initializeTeamInitializeQueue();
    }

    @Override
    public void initializeTeamInitializeQueue() {
        teamInitializeQueue = PublishSubject.create();
        teamInitializeQueueSubscription =
                teamInitializeQueue.throttleWithTimeout(300, TimeUnit.MILLISECONDS)
                        .onBackpressureBuffer()
                        .subscribe(o -> initializeTeams());
    }

    @Override
    public void initializeTeams() {
        if (!model.isNetworkConnected()) {
            view.clearTeams();
            return;
        }

        final List<Team> teamList = new ArrayList<>();
        model.getRefreshAccountInfoObservable()
                .concatMap(o -> model.getTeamsObservable(teamList))
                .concatMap(model::getPendingTeamsObservable)
                .concatMap(model::getUpdateBadgeCountObservable)
                .concatMap(model::getCheckSelectedTeamObservable)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(myTeams -> {
                    long selectedTeamId = myTeams.first;
                    List<Team> teams = myTeams.second;

                    if (teams == null || teams.isEmpty()) {
                        view.clearTeams();
                    } else {
                        view.setTeams(teams);

                        determineAnotherTeamHasMessage(selectedTeamId, teams);
                    }
                }, e -> {
                    LogUtil.e(TAG, Log.getStackTraceString(e));
                });
    }

    @Override
    public void determineAnotherTeamHasMessage(long selectedTeamId, List<Team> teams) {
        Team team = Observable.from(teams)
                .filter(team1 -> (team1.getTeamId() != selectedTeamId) && team1.getUnread() > 0)
                .toBlocking()
                .firstOrDefault(Team.createEmptyTeam());

        if (team == null || team.getTeamId() <= 0) {
            view.hideAnotherTeamHasMessageMetaphor();
            return;
        }
        view.showAnotherTeamHasMessageMetaphor();
    }

    @Override
    public void reInitializeTeams() {
        teamInitializeQueue.onNext(new Object());
    }

    @Override
    public void onTeamJoinAction(long teamId) {
        view.showProgressWheel();
        model.getUpdateEntityInfoObservable(teamId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    view.dismissProgressWheel();
                    view.moveToSelectTeam();
                }, error -> {
                    view.dismissProgressWheel();
                });
    }

    @Override
    public void onTeamInviteAcceptAction(final Team team) {
        view.showProgressWheel();

        model.getInviteDecisionObservable(
                team.getInvitationId(), ReqInvitationAcceptOrIgnore.Type.ACCEPT.getType())
                .concatMap(resTeamDetailInfo -> model.getUpdateTeamInfoObservable(team.getTeamId()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    view.removePendingTeam(team);

                    // 같은 액션을 한다.
                    onTeamCreated();

                }, error -> {
                    LogUtil.e(TAG, Log.getStackTraceString(error));
                    view.dismissProgressWheel();

                    if (error instanceof RetrofitError) {
                        RetrofitError e = (RetrofitError) error;
                        String errorMessage = model.getTeamInviteErrorMessage(e, team.getName());
                        view.showTeamInviteAcceptFailDialog(errorMessage, team);
                    }
                });
    }

    @Override
    public void onTeamInviteIgnoreAction(final Team team) {
        view.showProgressWheel();

        model.getInviteDecisionObservable(
                team.getInvitationId(), ReqInvitationAcceptOrIgnore.Type.DECLINE.getType())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resTeamDetailInfo -> {
                    view.dismissProgressWheel();
                    view.removePendingTeam(team);
                }, error -> {
                    LogUtil.e(TAG, Log.getStackTraceString(error));
                    view.dismissProgressWheel();

                    if (error instanceof RetrofitError) {
                        RetrofitError e = (RetrofitError) error;
                        String errorMessage = model.getTeamInviteErrorMessage(e, team.getName());
                        view.showTeamInviteIgnoreFailToast(errorMessage);
                    }
                });
    }

    @Override
    public void onTeamCreated() {
        view.showProgressWheel();

        model.getSelectedTeamObservable()
                .concatMap(userTeam ->
                        model.getUpdateEntityInfoObservable(userTeam.getTeamId()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    view.dismissProgressWheel();
                    view.moveToSelectTeam();
                }, error -> {
                    view.dismissProgressWheel();
                });
    }

    @Override
    public void clearTeamInitializeQueue() {
        if (teamInitializeQueueSubscription != null && !teamInitializeQueueSubscription.isUnsubscribed()) {
            teamInitializeQueueSubscription.unsubscribe();
        }
    }

}

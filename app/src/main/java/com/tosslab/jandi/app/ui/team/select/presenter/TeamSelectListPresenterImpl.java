package com.tosslab.jandi.app.ui.team.select.presenter;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqInvitationAcceptOrIgnore;
import com.tosslab.jandi.app.network.models.start.InitialInfo;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.team.select.adapter.datamodel.TeamSelectListAdapterDataModel;
import com.tosslab.jandi.app.ui.team.select.model.TeamSelectListModel;
import com.tosslab.jandi.app.ui.team.select.to.Team;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrLaunchTeam;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by tee on 2016. 9. 27..
 */

public class TeamSelectListPresenterImpl implements TeamSelectListPresenter {

    @Inject
    TeamSelectListModel model;

    @Inject
    TeamSelectListAdapterDataModel adapterDataModel;

    private TeamSelectListPresenter.View view;

    @Inject
    public TeamSelectListPresenterImpl(TeamSelectListPresenter.View view) {
        this.view = view;
    }

    @Override
    public void initTeamDatas(boolean firstEntered) {
        Observable.defer(() -> {
            List<Team> teams = new ArrayList<>();
            try {
                teams = model.getTeamInfos();
            } catch (RetrofitException e) {
                e.printStackTrace();
            }
            adapterDataModel.setDatas(teams);
            return Observable.just(teams);
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(teams -> {
                    // create team 밖에 없을 때
                    if (teams.size() == 1) {
                        view.showEmptyList();
                        if (firstEntered) {
                            view.moveCreateTeam();
                        }
                    } else {
                        view.showList();
                    }
                });
    }

    @Override
    public void onEnterSelectedTeam(long teamId) {
        view.showProgressWheel();
        Observable.defer(() -> {
            model.updateSelectTeam(teamId);
            try {
                InitialInfo initialInfo = model.getEntityInfo(teamId);
                return Observable.just(initialInfo);
            } catch (RetrofitException e) {
                return Observable.error(e);
            }
        })
                .doOnNext(initialInfo -> {
                    model.updateEntityInfo(initialInfo);
                    TeamInfoLoader.getInstance().refresh();
                    JandiPreference.setSocketConnectedLastTime(initialInfo.getTs());
                    SprinklrLaunchTeam.sendLog(teamId);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(it -> {
                    view.dismissProgressWheel();
                    view.moveSelectedTeam();
                }, t -> {
                    if (t instanceof RetrofitException) {
                        RetrofitException t1 = (RetrofitException) t;
                        SprinklrLaunchTeam.sendFailLog(t1.getResponseCode());
                    } else {
                        SprinklrLaunchTeam.sendFailLog(-1);
                    }
                    view.dismissProgressWheel();
                });
    }

    @Override
    public void onRequestAcceptJoin(Team selectedTeam) {
        Observable.defer(() -> {
            try {
                model.acceptOrDeclineInvite(
                        selectedTeam.getInvitationId(), ReqInvitationAcceptOrIgnore.Type.ACCEPT.getType());
                return Observable.just(selectedTeam);
            } catch (RetrofitException e) {
                return Observable.error(e);
            }
        })
                .doOnNext(team -> {
                    try {
                        model.updateTeamInfo(selectedTeam.getTeamId());
                    } catch (RetrofitException e) {
                        e.printStackTrace();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(team1 -> {
                    onEnterSelectedTeam(team1.getTeamId());
                }, t -> {
                    if (t instanceof RetrofitException) {
                        RetrofitException e = (RetrofitException) t;

                        String alertText = getJoinErrorMessage(selectedTeam, e);
                        view.showTextAlertDialog(alertText, (dialog, which) -> {
                            onRequestIgnoreJoin(selectedTeam, false);
                        });
                    }
                });
    }

    @Override
    public void onRequestIgnoreJoin(Team selectedTeam, boolean showErrorToast) {
        view.showProgressWheel();
        Observable.defer(() -> {
            try {
                model.acceptOrDeclineInvite(
                        selectedTeam.getInvitationId(), ReqInvitationAcceptOrIgnore.Type.DECLINE.getType());
                return Observable.just(selectedTeam);
            } catch (RetrofitException e) {
                return Observable.error(e);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(team -> {
                    view.dismissProgressWheel();
                    initTeamDatas(false);
                }, t -> {
                    view.dismissProgressWheel();
                    if (t instanceof RetrofitException) {
                        RetrofitException e = (RetrofitException) t;
                        if (showErrorToast) {
                            view.showErrorToast(getJoinErrorMessage(selectedTeam, e));
                        }
                        initTeamDatas(false);
                    }
                });

    }

    private String getJoinErrorMessage(Team selectedTeam, RetrofitException e) {
        int errorCode = e.getResponseCode();
        String alertText;
        switch (errorCode) {
            case JandiConstants.TeamInviteErrorCode.NOT_AVAILABLE_INVITATION_CODE:
                alertText = JandiApplication.getContext().getResources().getString(R.string.jandi_expired_invitation_link);
                break;
            case JandiConstants.TeamInviteErrorCode.DISABLED_MEMBER:
                alertText = JandiApplication.getContext().getResources().getString(R.string.jandi_disabled_team, selectedTeam.getName());
                break;
            case JandiConstants.TeamInviteErrorCode.REMOVED_TEAM:
                alertText = JandiApplication.getContext().getResources().getString(R.string.jandi_deleted_team);
                break;
            case JandiConstants.TeamInviteErrorCode.TEAM_INVITATION_DISABLED:
                alertText = JandiApplication.getContext().getResources().getString(R.string.jandi_invite_disabled, "");
                break;
            case JandiConstants.TeamInviteErrorCode.ENABLED_MEMBER:
                alertText = JandiApplication.getContext().getResources().getString(R.string.jandi_joined_team, selectedTeam.getName());
                break;
            default:
                alertText = JandiApplication.getContext().getResources().getString(R.string.err_network);
                break;

        }
        return alertText;
    }

    @Override
    public void setUserEmailInfo() {
        String email = model.getMyEmail();
        view.showLoginEmail(email);
    }

}
package com.tosslab.jandi.app.ui.account.presenter;

import android.util.Pair;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqInvitationAcceptOrIgnore;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.start.InitialInfo;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.account.model.AccountHomeModel;
import com.tosslab.jandi.app.ui.team.select.to.Team;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrChangeAccountName;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrLaunchTeam;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import java.util.ArrayList;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AccountHomePresenterImpl implements AccountHomePresenter {

    private AccountHomeModel accountHomeModel;

    private View view;

    @Inject
    public AccountHomePresenterImpl(AccountHomeModel accountHomeModel, View view) {
        this.accountHomeModel = accountHomeModel;
        this.view = view;
    }

    @Override
    public void onInitialize(boolean shouldRefreshAccountInfo) {
        if (!accountHomeModel.checkAccount()) {
            view.invalidAccess();
            return;
        }

        Observable<Boolean> observable = Observable.defer(() -> {
            String accountName = accountHomeModel.getAccountName();
            ResAccountInfo.UserEmail accountEmail = accountHomeModel.getSelectedEmailInfo();

            return Observable.just(Pair.create(accountName, accountEmail));
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(pair -> {
                    view.setAccountName(pair.first);

                    if (pair.second != null) {
                        view.setUserEmailText(pair.second.getId());
                    }

                })
                .observeOn(Schedulers.io())
                .map(it -> NetworkCheckUtil.isConnected())
                .publish().refCount();

        observable.filter(it -> it)
                .observeOn(Schedulers.io())
                .doOnNext(it -> {
                    if (shouldRefreshAccountInfo) {
                        accountHomeModel.refreshAccountInfo();
                    }
                })
                .map(it -> {
                    try {
                        return accountHomeModel.getTeamInfos();
                    } catch (RetrofitException e) {
                        return new ArrayList<Team>(0);
                    }
                })
                .doOnNext(teamList -> {
                    if (!teamList.isEmpty()) {
                        Observable.from(teamList)
                                .map(Team::getUnread)
                                .reduce((prev, current) -> prev + current)
                                .subscribe(total -> {
                                    BadgeUtils.setBadge(JandiApplication.getContext(), total);
                                });
                    }
                })
                .map(it -> Pair.create(it, accountHomeModel.getSelectedTeamInfo()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pair -> {
                    view.setTeamInfo(pair.first, pair.second);
                });

        observable.filter(it -> !it)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(it -> {
                    view.showCheckNetworkDialog();
                });
    }

    @Override
    public void onJoinedTeamSelect(long teamId) {

        view.showProgressWheel();
        Observable.defer(() -> {
            accountHomeModel.updateSelectTeam(teamId);
            try {
                InitialInfo initialInfo = accountHomeModel.getEntityInfo(teamId);
                return Observable.just(initialInfo);
            } catch (RetrofitException e) {
                return Observable.error(e);
            }
        })
                .doOnNext(initialInfo -> {
                    accountHomeModel.updateEntityInfo(initialInfo);
                    TeamInfoLoader.getInstance().refresh();
                    accountHomeModel.refreshPollList(teamId);
                    JandiPreference.setSocketConnectedLastTime(initialInfo.getTs());
                    SprinklrLaunchTeam.sendLog(teamId);
                })
                .subscribeOn(Schedulers.newThread())
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
    public void onCreateTeamSelect() {
        view.loadTeamCreateActivity();
    }

    @Override
    public void onAccountNameEditClick(String oldName) {
        view.showNameEditDialog(oldName);
    }

    @Override
    public void onChangeName(String newName) {
        view.showProgressWheel();
        Observable.defer(() -> {
            try {
                ResAccountInfo resAccountInfo = accountHomeModel.updateAccountName(newName);
                return Observable.just(resAccountInfo);
            } catch (RetrofitException e) {
                return Observable.error(e);
            }
        })
                .doOnNext(resAccountInfo -> {
                    SprinklrChangeAccountName.sendSuccessLog();
                    AccountRepository.getRepository().updateAccountName(resAccountInfo.getName());
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(it -> {
                    view.dismissProgressWheel();
                    view.setAccountName(newName);
                    String message = JandiApplication.getContext()
                            .getString(R.string.jandi_success_update_account_profile);
                    view.showSuccessToast(message);
                }, t -> {
                    view.dismissProgressWheel();
                    if (t instanceof RetrofitException) {
                        RetrofitException e = (RetrofitException) t;
                        int errorCode = e.getResponseCode();

                        SprinklrChangeAccountName.sendFailLog(e.getResponseCode());

                        if (e.getStatusCode() >= 500) {
                            String message = JandiApplication.getContext()
                                    .getString(R.string.err_network);
                            view.showErrorToast(message);
                        }
                    } else {
                        SprinklrChangeAccountName.sendFailLog(-1);
                    }
                });
    }

    @Override
    public void onTeamCreateAcceptResult() {
        ResAccountInfo.UserTeam selectedTeamInfo = accountHomeModel.getSelectedTeamInfo();
        onJoinedTeamSelect(selectedTeamInfo.getTeamId());
    }

    @Override
    public void onAccountEmailEditClick() {
        view.moveEmailEditClick();
    }

    @Override
    public void onEmailChooseResult() {
        ResAccountInfo.UserEmail selectedEmailInfo = accountHomeModel.getSelectedEmailInfo();
        if (selectedEmailInfo != null) {
            view.setUserEmailText(selectedEmailInfo.getId());
        }
    }

    @Override
    public void onRequestJoin(Team selectedTeam) {

        view.showProgressWheel();
        Observable.defer(() -> {
            try {
                accountHomeModel.acceptOrDeclineInvite(
                        selectedTeam.getInvitationId(), ReqInvitationAcceptOrIgnore.Type.ACCEPT.getType());
                return Observable.just(selectedTeam);
            } catch (RetrofitException e) {
                return Observable.error(e);
            }
        })
                .doOnNext(team -> {
                    try {
                        accountHomeModel.updateTeamInfo(selectedTeam.getTeamId());
                    } catch (RetrofitException e) {
                        e.printStackTrace();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(team1 -> {
                    view.removePendingTeamView(selectedTeam);
                    view.dismissProgressWheel();
                    view.moveAfterInvitaionAccept();
                }, t -> {
                    view.dismissProgressWheel();
                    if (t instanceof RetrofitException) {
                        RetrofitException e = (RetrofitException) t;

                        String alertText = getJoinErrorMessage(selectedTeam, e);
                        view.showTextAlertDialog(alertText, (dialog, which) -> {
                            onRequestIgnore(selectedTeam, false);
                            view.removePendingTeamView(selectedTeam);
                        });
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
    public void onRequestIgnore(Team selectedTeam, boolean showErrorToast) {

        view.showProgressWheel();
        Observable.defer(() -> {
            try {
                accountHomeModel.acceptOrDeclineInvite(
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
                    view.removePendingTeamView(team);
                }, t -> {
                    view.dismissProgressWheel();
                    if (t instanceof RetrofitException) {
                        RetrofitException e = (RetrofitException) t;
                        if (showErrorToast) {
                            view.showErrorToast(getJoinErrorMessage(selectedTeam, e));
                        }
                        view.removePendingTeamView(selectedTeam);
                    }
                });

    }

    @Override
    public void onHelpOptionSelect() {
        view.showHelloDialog();
    }

}

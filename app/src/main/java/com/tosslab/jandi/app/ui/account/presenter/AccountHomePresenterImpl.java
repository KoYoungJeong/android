package com.tosslab.jandi.app.ui.account.presenter;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.network.models.ReqInvitationAcceptOrIgnore;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.start.InitialInfo;
import com.tosslab.jandi.app.ui.account.model.AccountHomeModel;
import com.tosslab.jandi.app.ui.team.info.model.TeamDomainInfoModel;
import com.tosslab.jandi.app.ui.team.select.to.Team;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.util.List;

import rx.Observable;

/**
 * Created by Steve SeongUg Jung on 15. 3. 3..
 */
@EBean
public class AccountHomePresenterImpl implements AccountHomePresenter {

    @Bean
    AccountHomeModel accountHomeModel;

    @Bean
    TeamDomainInfoModel teamDomainInfoModel;

    View view;

    @Override
    public void setView(View view) {
        this.view = view;
    }

    @Background
    @Override
    public void onInitialize(boolean shouldRefreshAccountInfo) {
        if (!accountHomeModel.checkAccount()) {
            view.invalidAccess();
            return;
        }

        getAccountInfo();

        if (NetworkCheckUtil.isConnected()) {
            if (shouldRefreshAccountInfo) {
                accountHomeModel.refreshAccountInfo();
            }

            initTeamInfo();
        } else {
            view.showCheckNetworkDialog();
        }
    }

    private void getAccountInfo() {
        String accountName = accountHomeModel.getAccountName();
        ResAccountInfo.UserEmail accountEmail = accountHomeModel.getSelectedEmailInfo();

        view.setAccountName(accountName);

        if (accountEmail != null) {
            view.setUserEmailText(accountEmail.getId());
        }
    }

    //TODO 진입 시점에 네트워크 체킹 ?
    @Background
    @Override
    public void onJoinedTeamSelect(long teamId, boolean firstJoin) {
        view.showProgressWheel();
        try {
            accountHomeModel.updateSelectTeam(teamId);
            InitialInfo initialInfo = accountHomeModel.getEntityInfo(teamId);
            accountHomeModel.updateEntityInfo(initialInfo);
            view.dismissProgressWheel();

            // Track Team List Sign In (with flush)
            accountHomeModel.trackLaunchTeamSuccess(teamId);
            view.moveSelectedTeam(firstJoin);
        } catch (RetrofitException e) {
            int errorCode = e.getResponseCode();
            accountHomeModel.trackLaunchTeamFail(errorCode);
            view.dismissProgressWheel();
            e.printStackTrace();
        } catch (Exception e) {
            accountHomeModel.trackLaunchTeamFail(-1);
            view.dismissProgressWheel();
            e.printStackTrace();
        }
    }

    @Override
    public void onCreateTeamSelect() {
        view.loadTeamCreateActivity();
    }

    @Override
    public void onAccountNameEditClick(String oldName) {
        view.showNameEditDialog(oldName);
    }

    @Background
    @Override
    public void onChangeName(String newName) {
        view.showProgressWheel();
        try {
            ResAccountInfo resAccountInfo = accountHomeModel.updateAccountName(newName);
            accountHomeModel.trackChangeAccountNameSuccess(JandiApplication.getContext(), resAccountInfo.getId());

            AccountRepository.getRepository().updateAccountName(newName);
            view.dismissProgressWheel();
            view.setAccountName(newName);
            view.showSuccessToast(JandiApplication.getContext().getString(R.string.jandi_success_update_account_profile));
        } catch (RetrofitException e) {
            int errorCode = e.getResponseCode();

            accountHomeModel.trackChangeAccountNameFail(errorCode);

            view.dismissProgressWheel();
            if (e.getStatusCode() >= 500) {
                view.showErrorToast(JandiApplication.getContext().getResources().getString(R.string.err_network));
            }
            e.printStackTrace();
        } catch (Exception e) {
            accountHomeModel.trackChangeAccountNameFail(-1);
            view.dismissProgressWheel();
            e.printStackTrace();
        }
    }

    @Override
    public void onTeamCreateAcceptResult() {
        ResAccountInfo.UserTeam selectedTeamInfo = accountHomeModel.getSelectedTeamInfo();
        onJoinedTeamSelect(selectedTeamInfo.getTeamId(), true);
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

    @Background
    @Override
    public void onRequestJoin(Team selectedTeam) {
        view.showProgressWheel();

        try {
            accountHomeModel.acceptOrDeclineInvite(
                    selectedTeam.getInvitationId(), ReqInvitationAcceptOrIgnore.Type.ACCEPT.getType());
            teamDomainInfoModel.updateTeamInfo(selectedTeam.getTeamId());
            MixpanelMemberAnalyticsClient.getInstance(JandiApplication.getContext(), null)
                    .pageViewMemberCreateSuccess();

            view.removePendingTeamView(selectedTeam);
            view.dismissProgressWheel();
            view.moveAfterInvitaionAccept();
        } catch (RetrofitException e) {
            view.dismissProgressWheel();
            e.printStackTrace();

            String alertText = getJoinErrorMessage(selectedTeam, e);
            view.showTextAlertDialog(alertText, (dialog, which) -> {
                onRequestIgnore(selectedTeam, false);
                view.removePendingTeamView(selectedTeam);
            });
        } catch (Exception e) {
            view.dismissProgressWheel();
            e.printStackTrace();
        }
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

    @Background
    @Override
    public void onRequestIgnore(Team selectedTeam, boolean showErrorToast) {
        view.showProgressWheel();

        try {
            accountHomeModel.acceptOrDeclineInvite(
                    selectedTeam.getInvitationId(), ReqInvitationAcceptOrIgnore.Type.DECLINE.getType());
            view.dismissProgressWheel();
            view.removePendingTeamView(selectedTeam);
        } catch (RetrofitException e) {
            view.dismissProgressWheel();
            if (showErrorToast) {
                view.showErrorToast(getJoinErrorMessage(selectedTeam, e));
            }
            view.removePendingTeamView(selectedTeam);
        } catch (Exception e) {
            view.dismissProgressWheel();
        }
    }

    @Override
    public void onHelpOptionSelect() {
        view.showHelloDialog();
    }

    void initTeamInfo() {
        try {
            List<Team> teamList = accountHomeModel.getTeamInfos();

            Observable.from(teamList)
                    .map(Team::getUnread)
                    .reduce((prev, current) -> prev + current)
                    .subscribe(total -> {
                        BadgeUtils.setBadge(JandiApplication.getContext(), total);
                    });

            ResAccountInfo.UserTeam selectedTeamInfo = accountHomeModel.getSelectedTeamInfo();
            view.setTeamInfo(teamList, selectedTeamInfo);
        } catch (RetrofitException e) {
            view.showErrorToast(JandiApplication.getContext().getString(R.string.err_network));
        }
    }

}

package com.tosslab.jandi.app.ui.account.presenter;

import android.content.Context;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.BadgeCountRepository;
import com.tosslab.jandi.app.network.exception.ConnectionNotFoundException;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.network.models.ReqInvitationAcceptOrIgnore;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.ui.account.model.AccountHomeModel;
import com.tosslab.jandi.app.ui.team.info.model.TeamDomainInfoModel;
import com.tosslab.jandi.app.ui.team.select.to.Team;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.ArrayList;
import java.util.List;

import retrofit.RetrofitError;
import rx.Observable;

/**
 * Created by Steve SeongUg Jung on 15. 3. 3..
 */
@EBean
public class AccountHomePresenterImpl implements AccountHomePresenter {
    public static final int NOT_AVAILABLE_INVITATION_CODE = 40003;
    public static final int DISABLED_MEMBER = 40301;
    public static final int REMOVED_TEAM = 40302;
    public static final int TEAM_INVITATION_DISABLED = 40303;
    public static final int ENABLED_MEMBER = 40304;

    @Bean
    AccountHomeModel accountHomeModel;

    @Bean
    TeamDomainInfoModel teamDomainInfoModel;

    @RootContext
    Context context;

    View view;

    @AfterViews
    void initViews() {

        if (!accountHomeModel.checkAccount()) {
            view.invalidAccess();
            return;
        }

        getAccountInfo();

        if (NetworkCheckUtil.isConnected()) {
            getTeamInfo();
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

    @Override
    public void setView(View view) {
        this.view = view;
    }

    //TODO 진입 시점에 네트워크 체킹 ?
    @Background
    @Override
    public void onJoinedTeamSelect(int teamId, boolean firstJoin) {
        view.showProgressWheel();
        try {
            accountHomeModel.updateSelectTeam(teamId);
            ResLeftSideMenu entityInfo = accountHomeModel.getEntityInfo(teamId);
            accountHomeModel.updateEntityInfo(context, entityInfo);
            view.dismissProgressWheel();

            // Track Team List Sign In (with flush)
            accountHomeModel.trackLaunchTeamSuccess(teamId);
            view.moveSelectedTeam(firstJoin);
        } catch (RetrofitError e) {
            int errorCode = e.getResponse() != null ? e.getResponse().getStatus() : -1;
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
            accountHomeModel.trackChangeAccountNameSuccess(context, resAccountInfo.getId());

            AccountRepository.getRepository().updateAccountName(newName);
            view.dismissProgressWheel();
            view.setAccountName(newName);
            view.showSuccessToast(context.getString(R.string.jandi_success_update_account_profile));
        } catch (RetrofitError e) {
            int errorCode = -1;
            if (e.getResponse() != null) {
                errorCode = e.getResponse().getStatus();
            }

            accountHomeModel.trackChangeAccountNameFail(errorCode);

            view.dismissProgressWheel();
            if (e.getCause() instanceof ConnectionNotFoundException) {
                view.showErrorToast(context.getResources().getString(R.string.err_network));
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
            teamDomainInfoModel.acceptOrDclineInvite(
                    selectedTeam.getInvitationId(), ReqInvitationAcceptOrIgnore.Type.ACCEPT.getType());
            teamDomainInfoModel.updateTeamInfo(selectedTeam.getTeamId());
            MixpanelMemberAnalyticsClient.getInstance(context, null)
                    .pageViewMemberCreateSuccess();

            view.removePendingTeamView(selectedTeam);
            view.dismissProgressWheel();
            view.moveAfterInvitaionAccept();
        } catch (RetrofitError e) {
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

    private String getJoinErrorMessage(Team selectedTeam, RetrofitError e) {
        int errorCode = e.getResponse() != null ? e.getResponse().getStatus() : -1;
        String alertText;
        switch (errorCode) {
            case NOT_AVAILABLE_INVITATION_CODE:
                alertText = context.getResources().getString(R.string.jandi_expired_invitation_link);
                break;
            case DISABLED_MEMBER:
                alertText = context.getResources().getString(R.string.jandi_disabled_team, selectedTeam.getName());
                break;
            case REMOVED_TEAM:
                alertText = context.getResources().getString(R.string.jandi_deleted_team);
                break;
            case TEAM_INVITATION_DISABLED:
                alertText = context.getResources().getString(R.string.jandi_invite_disabled, "");
                break;
            case ENABLED_MEMBER:
                alertText = context.getResources().getString(R.string.jandi_joined_team, selectedTeam.getName());
                break;
            default:
                alertText = context.getResources().getString(R.string.err_network);
                break;

        }
        return alertText;
    }

    @Background
    @Override
    public void onRequestIgnore(Team selectedTeam, boolean showErrorToast) {
        view.showProgressWheel();

        try {
            teamDomainInfoModel.acceptOrDclineInvite(
                    selectedTeam.getInvitationId(), ReqInvitationAcceptOrIgnore.Type.DECLINE.getType());
            view.dismissProgressWheel();
            view.removePendingTeamView(selectedTeam);
        } catch (RetrofitError e) {
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

    @Background
    void getTeamInfo() {
        refreshAccountInfo();

        try {
            List<Team> teamList = accountHomeModel.getTeamInfos();

            Observable.from(teamList)
                    .filter(team -> team.getStatus() == Team.Status.JOINED)
                    .subscribe(team -> {
                        BadgeCountRepository.getRepository()
                                .upsertBadgeCount(team.getTeamId(), team.getUnread());
                    });

            BadgeUtils.setBadge(context, BadgeCountRepository.getRepository().getTotalBadgeCount());

            ResAccountInfo.UserTeam selectedTeamInfo = accountHomeModel.getSelectedTeamInfo();
            view.setTeamInfo((ArrayList<Team>) teamList, selectedTeamInfo);
        } catch (RetrofitError e) {
            view.showErrorToast(context.getString(R.string.err_network));
        }
    }

    private void refreshAccountInfo() {
        try {
            ResAccountInfo resAccountInfo = RequestApiManager.getInstance().getAccountInfoByMainRest();
            AccountRepository.getRepository().upsertAccountAllInfo(resAccountInfo);
        } catch (RetrofitError retrofitError) {
            retrofitError.printStackTrace();
        }
    }

}

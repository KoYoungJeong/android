package com.tosslab.jandi.app.ui.account.presenter;

import android.content.Context;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.ResultObject;
import com.tosslab.jandi.app.network.mixpanel.MixpanelAccountAnalyticsClient;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.network.models.ReqInvitationAcceptOrIgnore;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.ui.account.model.AccountHomeModel;
import com.tosslab.jandi.app.ui.team.info.model.TeamDomainInfoModel;
import com.tosslab.jandi.app.ui.team.select.to.Team;
import com.tosslab.jandi.app.utils.JandiNetworkException;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.ArrayList;

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

    private View view;

    @AfterViews
    void initViews() {
        getAccountInfo();
        getTeamInfo();
    }

    private void getAccountInfo() {
        String accountName = accountHomeModel.getAccountName(context);
        ResAccountInfo.UserEmail accountEmail = accountHomeModel.getSelectedEmailInfo(context);

        view.setAccountName(accountName);

        if (accountEmail != null) {
            view.setUserEmailText(accountEmail.getId());
        }
    }

    @Override
    public void setView(View view) {
        this.view = view;
    }

    @Background
    @Override
    public void onJoinedTeamSelect(int teamId, boolean firstJoin) {
        view.showProgressWheel();

        try {
            accountHomeModel.updateSelectTeam(context, teamId);

            ResLeftSideMenu entityInfo = accountHomeModel.getEntityInfo(context, teamId);
            accountHomeModel.updateEntityInfo(context, entityInfo);

            view.moveSelectedTeam(firstJoin);
        } catch (JandiNetworkException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            view.dismissProgressWheel();
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
            ResAccountInfo resAccountInfo = accountHomeModel.updateAccountName(context, newName);
            MixpanelAccountAnalyticsClient
                    .getInstance(context, resAccountInfo.getId())
                    .trackSetAccount();

            JandiAccountDatabaseManager.getInstance(context).upsertAccountInfo(resAccountInfo);
            view.setAccountName(newName);
            view.showSuccessToast(context.getString(R.string.jandi_success_update_account_profile));
        } catch (JandiNetworkException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            view.dismissProgressWheel();
        }
    }

    @Override
    public void onTeamCreateAcceptResult() {

        ResAccountInfo.UserTeam selectedTeamInfo = accountHomeModel.getSelectedTeamInfo(context);
        onJoinedTeamSelect(selectedTeamInfo.getTeamId(), true);
    }


    @Override
    public void onAccountEmailEditClick() {
        view.moveEmailEditClick();
    }

    @Override
    public void onEmailChooseResult() {
        ResAccountInfo.UserEmail selectedEmailInfo = accountHomeModel.getSelectedEmailInfo(context);
        if (selectedEmailInfo != null) {
            view.setUserEmailText(selectedEmailInfo.getId());
        }
    }

    @Background
    @Override
    public void onRequestJoin(Team selectedTeam) {
        view.showProgressWheel();

        try {
            teamDomainInfoModel.acceptOrDclineInvite(selectedTeam.getInvitationId(), ReqInvitationAcceptOrIgnore.Type.ACCEPT.getType());
            teamDomainInfoModel.updateTeamInfo(selectedTeam.getTeamId());
            MixpanelMemberAnalyticsClient.getInstance(context, null)
                    .pageViewMemberCreateSuccess();

            view.removePendingTeamView(selectedTeam);
            view.dismissProgressWheel();
            view.moveAfterinvitaionAccept();
        } catch (JandiNetworkException e) {
            view.dismissProgressWheel();
            e.printStackTrace();

            String alertText = getJoinErrorMessage(selectedTeam, e.errCode);

            view.showTextAlertDialog(alertText, (dialog, which) -> {
                view.dismissProgressWheel();
                onRequestIgnore(selectedTeam);
            });
            onRequestIgnore(selectedTeam);

        } catch (Exception e) {
            view.dismissProgressWheel();
            e.printStackTrace();
        }
    }

    private String getJoinErrorMessage(Team selectedTeam, int errCode) {
        String alertText;
        switch (errCode) {
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
    public void onRequestIgnore(Team selectedTeam) {
        view.showProgressWheel();

        try {
            teamDomainInfoModel.acceptOrDclineInvite(selectedTeam.getInvitationId(), ReqInvitationAcceptOrIgnore.Type.DECLINE.getType());
            view.dismissProgressWheel();
            view.removePendingTeamView(selectedTeam);
        } catch (JandiNetworkException e) {
            view.dismissProgressWheel();
            view.showErrorToast(getJoinErrorMessage(selectedTeam, e.errCode));
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
        ResultObject<ArrayList<Team>> resultObject = accountHomeModel.getTeamInfos(context);
        ResAccountInfo.UserTeam selectedTeamInfo = accountHomeModel.getSelectedTeamInfo(context);

        if (resultObject.getStatusCode() < 400) {
            view.setTeamInfo(resultObject.getResult(), selectedTeamInfo);
        } else {
            view.showErrorToast(context.getString(R.string.err_network));
        }
    }
}

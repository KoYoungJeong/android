package com.tosslab.jandi.app.ui.account.presenter;

import android.content.Context;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.ResultObject;
import com.tosslab.jandi.app.network.mixpanel.MixpanelAccountAnalyticsClient;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.ui.account.model.AccountHomeModel;
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

    @Bean
    AccountHomeModel accountHomeModel;
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
        } catch (JandiNetworkException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            view.dismissProgressWheel();

            view.moveSelectedTeam(firstJoin);
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
    public void onTeamCreateResult() {

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

    @Override
    public void onRequestJoin(Team selectedTeam) {
        view.moveCreatedTeamDomain(selectedTeam);
    }

    @Background
    @Override
    public void onRequestIgnore(Team selectedTeam) {
        view.showProgressWheel();

        try {
            accountHomeModel.ignorePendingTeam(context, selectedTeam);
        } catch (JandiNetworkException e) {
        } catch (Exception e) {
        } finally {
            view.dismissProgressWheel();
            view.removePendingTeamView(selectedTeam);
        }
    }

    @Override
    public void onHelpOptionSelect() {
        view.showHeloDialog();
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

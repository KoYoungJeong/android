package com.tosslab.jandi.app.ui.account.presenter;

import android.content.Context;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.ResultObject;
import com.tosslab.jandi.app.network.mixpanel.MixpanelAccountAnalyticsClient;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
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
        getTeamInfo();
    }

    @Override
    public void setView(View view) {
        this.view = view;
    }

    @Override
    public void onJoinedTeamSelect(Team clickedTeam) {

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
        try {
            ResAccountInfo resAccountInfo = accountHomeModel.updateAccountName(context, newName);
            MixpanelAccountAnalyticsClient
                    .getInstance(context, resAccountInfo.getId())
                    .trackSetAccount();
        } catch (JandiNetworkException e) {
            e.printStackTrace();
        }
    }

    @Background
    void getTeamInfo() {
        ResultObject<ArrayList<Team>> resultObject = accountHomeModel.getTeamInfos(context);

        if (resultObject.getStatusCode() < 400) {
            view.setTeamInfo(resultObject.getResult());
        } else {
            view.showErrorToast(context.getString(R.string.err_network));
        }
    }


}

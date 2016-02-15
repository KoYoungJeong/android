package com.tosslab.jandi.app.ui.team.info.presenter;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.exception.ConnectionNotFoundException;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.network.models.validation.ResValidation;
import com.tosslab.jandi.app.ui.team.info.model.TeamDomainInfoModel;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.util.List;

import retrofit.RetrofitError;

@EBean
public class TeamDomainInfoPresenterImpl implements TeamDomainInfoPresenter {

    @Bean
    TeamDomainInfoModel teamDomainInfoModel;
    private View view;

    @Override
    public void setView(View view) {
        this.view = view;
    }

    @Override
    public void checkEmailInfo() {
        List<ResAccountInfo.UserEmail> userEmails = teamDomainInfoModel.initUserEmailInfo();
        if (userEmails == null || userEmails.isEmpty()) {
            view.showFailToast(JandiApplication.getContext().getString(R.string.err_network));
            view.finishView();
        }
    }

    @Override
    @Background
    public void createTeam(String teamName, String teamDomain) {
        view.showProgressWheel();

        if (!NetworkCheckUtil.isConnected()) {
            view.showFailToast(JandiApplication.getContext().getString(R.string.err_network));
            return;
        }

        // Team Creation
        try {
            ResValidation validation = teamDomainInfoModel.validDomain(teamDomain);
            if (!validation.isValidate()) {
                view.showFailToast(JandiApplication.getContext().getString(R.string.jandi_domain_is_already_taken));
                view.dismissProgressWheel();
                return;
            }

            ResTeamDetailInfo newTeam = teamDomainInfoModel.createNewTeam(teamName, teamDomain);

            long teamId = newTeam.getInviteTeam().getTeamId();
            MixpanelMemberAnalyticsClient.getInstance(JandiApplication.getContext(), null)
                    .pageViewTeamCreateSuccess();

            teamDomainInfoModel.updateTeamInfo(teamId);

            teamDomainInfoModel.trackCreateTeamSuccess(teamId);

            view.dismissProgressWheel();
            view.successCreateTeam(newTeam.getInviteTeam().getName());

        } catch (RetrofitError e) {
            e.printStackTrace();
            int errorCode = -1;
            if (e.getResponse() != null) {
                errorCode = e.getResponse().getStatus();
            }
            teamDomainInfoModel.trackCreateTeamFail(errorCode);

            view.dismissProgressWheel();
            if (e.getCause() instanceof ConnectionNotFoundException) {
                view.showFailToast(JandiApplication.getContext().getString(R.string.err_network));
                return;
            }
            view.failCreateTeam(e.getResponse().getStatus());
        }
    }

}

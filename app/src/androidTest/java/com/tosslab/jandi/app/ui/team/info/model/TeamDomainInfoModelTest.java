package com.tosslab.jandi.app.ui.team.info.model;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.manager.restapiclient.annotation.DELETEWithBody;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.network.models.validation.ResValidation;
import com.tosslab.jandi.app.utils.TokenUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.UUID;

import retrofit.RestAdapter;
import retrofit.http.Body;
import retrofit.http.Headers;
import retrofit.http.Path;
import setup.BaseInitUtil;

import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class TeamDomainInfoModelTest {

    private TeamDomainInfoModel teamDomainInfoModel;

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData();
        teamDomainInfoModel = TeamDomainInfoModel_.getInstance_(JandiApplication.getContext());

    }

    @Test
    public void testValidDomain() throws Exception {
        {
            ResValidation validation = teamDomainInfoModel.validDomain("tosslab");
            assertThat(validation.isValidate(), is(false));
        }
        {
            ResValidation validation = teamDomainInfoModel.validDomain("tosslabsjeyw");
            assertThat(validation.isValidate(), is(true));
        }
    }

    @Test
    public void testInitUserEmailInfo() throws Exception {
        {
            List<ResAccountInfo.UserEmail> userEmails = teamDomainInfoModel.initUserEmailInfo();

            List<ResAccountInfo.UserEmail> accountEmails = AccountRepository.getRepository().getAccountEmails();
            assertThat(userEmails.size(), is(lessThanOrEqualTo(accountEmails.size())));
        }


        {
            BaseInitUtil.clear();
            List<ResAccountInfo.UserEmail> userEmails = teamDomainInfoModel.initUserEmailInfo();
            assertThat(userEmails.size(), is(equalTo(0)));
        }


    }

    @Test
    public void testCreateNewTeam() throws Exception {
        String name = UUID.randomUUID().toString().substring(0, 5);

        ResTeamDetailInfo newTeam = teamDomainInfoModel.createNewTeam(name, name);
        assertThat(newTeam, is(notNullValue()));
        assertThat(newTeam.getInviteTeam().getName(), is(equalTo(name)));
        assertThat(newTeam.getInviteTeam().getTeamDomain(), is(equalTo(name)));

        long createdTeamId = newTeam.getInviteTeam().getTeamId();

        new RestAdapter.Builder()
                .setEndpoint(JandiConstantsForFlavors.SERVICE_INNER_API_URL)
                .setRequestInterceptor(request -> request.addHeader(JandiConstants.AUTH_HEADER, TokenUtil.getRequestAuthentication()))
                .setLogLevel(RestAdapter.LogLevel.HEADERS_AND_ARGS)
                .build()
                .create(Team.class)
                .deleteTeam(createdTeamId, new ReqDeleteTeam());
    }

    @Test
    public void testUpdateTeamInfo() throws Exception {

        AccountRepository.getRepository().clearAccountData();
        teamDomainInfoModel.updateTeamInfo(100);
        assertThat(AccountRepository.getRepository().getAccountInfo(), is(notNullValue()));
        assertThat(AccountRepository.getRepository().getSelectedTeamId(), is(equalTo(100L)));

    }

    public interface Team {
        @DELETEWithBody("/teams/{teamId}")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        ResCommon deleteTeam(@Path("teamId") long teamId, @Body ReqDeleteTeam team);
    }

    public static class ReqDeleteTeam {
        private String lang = "ko";

        public String getLang() {
            return lang;
        }

        public void setLang(String lang) {
            this.lang = lang;
        }
    }

}
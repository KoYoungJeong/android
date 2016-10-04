package com.tosslab.jandi.app.ui.team.select.model;

import android.support.test.runner.AndroidJUnit4;
import android.text.TextUtils;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.InitialInfoRepository;
import com.tosslab.jandi.app.network.client.invitation.InvitationApi;
import com.tosslab.jandi.app.network.client.start.StartApi;
import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResPendingTeamInfo;
import com.tosslab.jandi.app.network.models.start.InitialInfo;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.team.select.to.Team;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import javax.inject.Inject;

import dagger.Component;
import setup.BaseInitUtil;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Created by tee on 2016. 10. 4..
 */

@RunWith(AndroidJUnit4.class)
public class TeamSelectListModelTest {

    @Inject
    TeamSelectListModel teamSelectListModel;

    @BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        BaseInitUtil.releaseDatabase();
    }

    @Before
    public void setUp() throws Exception {
        DaggerTeamSelectListModelTest_TeamSelectListModelTestComponent.builder()
                .build().inject(this);
    }

    @Test
    public void testGetTeamInfos() throws Exception {
        // Given
        List<ResPendingTeamInfo> pendingTeamInfo = new InvitationApi(RetrofitBuilder.getInstance()).getPedingTeamInfo();

        int pendingCount = 0;
        for (ResPendingTeamInfo resPendingTeamInfo : pendingTeamInfo) {
            if (TextUtils.equals(resPendingTeamInfo.getStatus(), "pending")) {
                ++pendingCount;
            }
        }

        // When
        List<Team> teamInfos = teamSelectListModel.getTeamInfos();


        // Then
        List<ResAccountInfo.UserTeam> accountTeams = AccountRepository.getRepository().getAccountTeams();
        assertThat(teamInfos.size(), is(greaterThan(accountTeams.size() + pendingCount)));
        assertThat(teamInfos.size(), is(equalTo(accountTeams.size() + pendingCount + 1)));
    }

    @Test
    public void testUpdateSelectTeam() throws Exception {
        // Given
        long originSelectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
        long newTeamId = getNewTeamId(originSelectedTeamId);

        // When
        teamSelectListModel.updateSelectTeam(newTeamId);

        // Then
        long newSavedTeamId = AccountRepository.getRepository().getSelectedTeamId();

        assertThat(originSelectedTeamId, is(not(equalTo(newSavedTeamId))));
        assertThat(newTeamId, is(equalTo(newSavedTeamId)));
    }

    private long getNewTeamId(long originSelectedTeamId) {
        long newTeamId = -1;
        List<ResAccountInfo.UserTeam> accountTeams = AccountRepository.getRepository().getAccountTeams();
        for (ResAccountInfo.UserTeam userTeam : accountTeams) {
            if (originSelectedTeamId != userTeam.getTeamId()) {
                newTeamId = userTeam.getTeamId();
                break;
            }
        }
        return newTeamId;
    }

    @Test
    public void testGetEntityInfo() throws Exception {
        // Given
        long selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
        TeamInfoLoader instance = TeamInfoLoader.getInstance(selectedTeamId);

        // When
        InitialInfo initialInfo = teamSelectListModel.getEntityInfo(selectedTeamId);

        // Then
        assertThat(initialInfo, is(notNullValue()));
        assertThat(instance.getTeamId(), is(equalTo(initialInfo.getTeam().getId())));
        assertThat(instance.getTeamName(), is(equalTo(initialInfo.getTeam().getName())));
        assertThat(instance.getTeamDomain(), is(equalTo(initialInfo.getTeam().getDomain())));
    }

    @Test
    public void testUpdateEntityInfo() throws Exception {

        // Given
        List<ResAccountInfo.UserTeam> accountTeams = AccountRepository.getRepository().getAccountTeams();
        long teamId = accountTeams.get(accountTeams.size() - 1).getTeamId();
        AccountRepository.getRepository().updateSelectedTeamInfo(teamId);
        InitialInfo leftSideMenu = new StartApi(RetrofitBuilder.getInstance()).getInitializeInfo(teamId);

        // When
        teamSelectListModel.updateEntityInfo(leftSideMenu);

        // Then
        assertThat(InitialInfoRepository.getInstance().hasInitialInfo(teamId), is(notNullValue()));
        assertThat(InitialInfoRepository.getInstance().getInitialInfo(teamId).getMembers().size(),
                is(equalTo(leftSideMenu.getMembers().size())));
        assertThat(InitialInfoRepository.getInstance().getInitialInfo(teamId).getTopics().size(),
                is(equalTo(leftSideMenu.getTopics().size())));

        // Restore
        accountTeams = AccountRepository.getRepository().getAccountTeams();
        teamId = accountTeams.get(0).getTeamId();
        AccountRepository.getRepository().updateSelectedTeamInfo(teamId);

    }

    @Test
    public void testGetMyEmail() {
        // Given
        String expectedEmail = BaseInitUtil.TEST_EMAIL;

        // When
        String myEmail = teamSelectListModel.getMyEmail();

        // Then
        assertEquals(expectedEmail, myEmail);
    }

    @Component(modules = ApiClientModule.class)
    public interface TeamSelectListModelTestComponent {
        void inject(TeamSelectListModelTest test);
    }


}
package com.tosslab.jandi.app.ui.account.model;

import android.support.test.runner.AndroidJUnit4;
import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.LeftSideMenuRepository;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqProfileName;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResPendingTeamInfo;
import com.tosslab.jandi.app.ui.team.select.to.Team;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import rx.Observable;
import setup.BaseInitUtil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;


/**
 * Created by jsuch2362 on 15. 11. 11..
 */
@RunWith(AndroidJUnit4.class)
public class AccountHomeModelTest {

    private AccountHomeModel accountHomeModel;

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData();
        accountHomeModel = AccountHomeModel_.getInstance_(JandiApplication.getContext());
    }

    @After
    public void tearDown() throws Exception {
        BaseInitUtil.clear();
    }

    @Test
    public void testGetTeamInfos() throws Exception {

        // Given
        List<ResPendingTeamInfo> pendingTeamInfo = RequestApiManager.getInstance().getPendingTeamInfoByInvitationApi();
        int pendingCount = 0;
        for (ResPendingTeamInfo resPendingTeamInfo : pendingTeamInfo) {
            if (TextUtils.equals(resPendingTeamInfo.getStatus(), "pending")) {
                ++pendingCount;
            }
        }

        // When
        List<Team> teamInfos = accountHomeModel.getTeamInfos();


        // Then
        List<ResAccountInfo.UserTeam> accountTeams = AccountRepository.getRepository().getAccountTeams();
        assertThat(teamInfos.size(), is(greaterThan(accountTeams.size() + pendingCount)));
        assertThat(teamInfos.size(), is(equalTo(accountTeams.size() + pendingCount + 1)));
    }

    @Test
    public void testUpdateAccountName() throws Exception {

        // Given
        ResAccountInfo accountInfo = AccountRepository.getRepository().getAccountInfo();
        String originName = accountInfo.getName();
        String changedName = "steve1";

        // When
        accountHomeModel.updateAccountName(changedName);

        // Then
        ResAccountInfo newAccountInfo = RequestApiManager.getInstance().getAccountInfoByMainRest();
        String newName = newAccountInfo.getName();

        assertThat(originName, is(not(equalTo(newName))));
        assertThat(changedName, is(equalTo(newName)));

        RequestApiManager.getInstance().changeNameByAccountProfileApi(new ReqProfileName("Steve"));

    }

    @Test
    public void testUpdateSelectTeam() throws Exception {

        // Given
        int originSelectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
        int newTeamId = getNewTeamId(originSelectedTeamId);

        // When
        accountHomeModel.updateSelectTeam(newTeamId);

        // Then
        int newSavedTeamId = AccountRepository.getRepository().getSelectedTeamId();

        assertThat(originSelectedTeamId, is(not(equalTo(newSavedTeamId))));
        assertThat(newTeamId, is(equalTo(newSavedTeamId)));
    }

    private int getNewTeamId(int originSelectedTeamId) {
        int newTeamId = -1;
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
        int selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
        ResLeftSideMenu currentLeftSideMenu = LeftSideMenuRepository.getRepository().getCurrentLeftSideMenu();

        // When
        ResLeftSideMenu entityInfo = accountHomeModel.getEntityInfo(selectedTeamId);

        // Then
        assertThat(entityInfo, is(notNullValue()));
        assertThat(currentLeftSideMenu.team.id, is(equalTo(entityInfo.team.id)));
        assertThat(currentLeftSideMenu.team.t_defaultChannelId, is(equalTo(entityInfo.team.t_defaultChannelId)));
        assertThat(currentLeftSideMenu.team.name, is(equalTo(entityInfo.team.name)));
        assertThat(currentLeftSideMenu.team.t_domain, is(equalTo(entityInfo.team.t_domain)));

    }

    @Test
    public void testUpdateEntityInfo() throws Exception {

        // Given
        List<ResAccountInfo.UserTeam> accountTeams = AccountRepository.getRepository().getAccountTeams();
        int teamId = accountTeams.get(accountTeams.size() - 1).getTeamId();
        AccountRepository.getRepository().updateSelectedTeamInfo(teamId);
        ResLeftSideMenu leftSideMenu = RequestApiManager.getInstance().getInfosForSideMenuByMainRest(teamId);

        // When
        EntityManager entityManager = accountHomeModel.updateEntityInfo(JandiApplication.getContext(), leftSideMenu);

        // Then
        assertThat(entityManager, is(notNullValue()));
        assertThat(leftSideMenu.team.t_defaultChannelId, is(equalTo(entityManager.getDefaultTopicId())));
        assertThat(leftSideMenu.joinEntityCount, is(equalTo(entityManager.getJoinedChannels().size() + entityManager.getGroups().size())));

    }

    @Test
    public void testGetSelectedTeamInfo() throws Exception {

        // Given
        ResAccountInfo.UserTeam rawSelectedTeam = AccountRepository.getRepository().getSelectedTeamInfo();

        // When
        ResAccountInfo.UserTeam selectedTeamInfo = accountHomeModel.getSelectedTeamInfo();

        // Then
        assertThat(rawSelectedTeam.getName(), is(equalTo(selectedTeamInfo.getName())));
        assertThat(rawSelectedTeam.getTeamDomain(), is(equalTo(selectedTeamInfo.getTeamDomain())));
        assertThat(rawSelectedTeam.getMemberId(), is(equalTo(selectedTeamInfo.getMemberId())));
    }

    @Test
    public void testGetSelectedEmailInfo() throws Exception {
        // Given
        ResAccountInfo.UserEmail savedEmail = Observable.from(AccountRepository.getRepository().getAccountEmails())
                .filter(ResAccountInfo.UserEmail::isPrimary)
                .toBlocking()
                .first();

        // When
        ResAccountInfo.UserEmail selectedEmailInfo = accountHomeModel.getSelectedEmailInfo();

        // Then
        assertThat(selectedEmailInfo.getId(), is(equalTo(savedEmail.getId())));
        assertThat(selectedEmailInfo.isPrimary(), is(equalTo(savedEmail.isPrimary())));
        assertThat(selectedEmailInfo.getStatus(), is(equalTo(savedEmail.getStatus())));
        assertThat(selectedEmailInfo.getConfirmedAt(), is(equalTo(savedEmail.getConfirmedAt())));
    }

    @Test
    public void testGetAccountName() throws Exception {
        String savedName = AccountRepository.getRepository().getAccountInfo().getName();
        String accountName = accountHomeModel.getAccountName();
        assertThat(accountName, is(equalTo(savedName)));
    }

    @Test
    public void testCheckAccount() throws Exception {
        assertThat(accountHomeModel.checkAccount(), is(true));
    }
}
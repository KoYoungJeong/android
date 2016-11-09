//package com.tosslab.jandi.app.ui.account.model;
//
//import android.support.test.runner.AndroidJUnit4;
//import android.text.TextUtils;
//
//import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
//import com.tosslab.jandi.app.network.client.account.AccountApi;
//import com.tosslab.jandi.app.network.client.invitation.InvitationApi;
//import com.tosslab.jandi.app.network.client.settings.AccountProfileApi;
//import com.tosslab.jandi.app.network.client.start.StartApi;
//import com.tosslab.jandi.app.network.dagger.ApiClientModule;
//import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
//import com.tosslab.jandi.app.network.models.ReqProfileName;
//import com.tosslab.jandi.app.network.models.ResAccountInfo;
//import com.tosslab.jandi.app.network.models.ResPendingTeamInfo;
//import com.tosslab.jandi.app.network.models.start.InitialInfo;
//import com.tosslab.jandi.app.team.TeamInfoLoader;
//import com.tosslab.jandi.app.ui.team.select.to.Team;
//
//import org.junit.AfterClass;
//import org.junit.Before;
//import org.junit.BeforeClass;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//import java.util.List;
//
//import javax.inject.Inject;
//
//import dagger.Component;
//import rx.Observable;
//import setup.BaseInitUtil;
//
//import static org.hamcrest.MatcherAssert.assertThat;
//import static org.hamcrest.Matchers.equalTo;
//import static org.hamcrest.Matchers.greaterThan;
//import static org.hamcrest.Matchers.is;
//import static org.hamcrest.Matchers.not;
//import static org.hamcrest.Matchers.notNullValue;
//
//
///**
// * Created by jsuch2362 on 15. 11. 11..
// */
//@RunWith(AndroidJUnit4.class)
//public class AccountHomeModelTest {
//
//    @Inject
//    AccountHomeModel accountHomeModel;
//
//    @BeforeClass
//    public static void setUpClass() throws Exception {
//        BaseInitUtil.initData();
//    }
//
//    @AfterClass
//    public static void tearDownClass() throws Exception {
//        BaseInitUtil.releaseDatabase();
//    }
//
//    @Before
//    public void setUp() throws Exception {
//        DaggerAccountHomeModelTest_AccountHomeModelTestComponent.builder()
//                .build()
//                .inject(this);
//    }
//
//    @Test
//    public void testGetTeamInfos() throws Exception {
//
//        // Given
//        List<ResPendingTeamInfo> pendingTeamInfo = new InvitationApi(RetrofitBuilder.initiate()).getPedingTeamInfo();
//        int pendingCount = 0;
//        for (ResPendingTeamInfo resPendingTeamInfo : pendingTeamInfo) {
//            if (TextUtils.equals(resPendingTeamInfo.getStatus(), "pending")) {
//                ++pendingCount;
//            }
//        }
//
//        // When
//        List<Team> teamInfos = accountHomeModel.getTeamInfos();
//
//
//        // Then
//        List<ResAccountInfo.UserTeam> accountTeams = AccountRepository.getRepository().getAccountTeams();
//        assertThat(teamInfos.size(), is(greaterThan(accountTeams.size() + pendingCount)));
//        assertThat(teamInfos.size(), is(equalTo(accountTeams.size() + pendingCount + 1)));
//    }
//
//    @Test
//    public void testUpdateAccountName() throws Exception {
//
//        // Given
//        ResAccountInfo accountInfo = AccountRepository.getRepository().getAccountInfo();
//        String originName = accountInfo.getName();
//        String changedName = "steve1";
//
//        // When
//        accountHomeModel.updateAccountName(changedName);
//
//        // Then
//        ResAccountInfo newAccountInfo = new AccountApi(RetrofitBuilder.initiate()).getAccountInfo();
//        String newName = newAccountInfo.getName();
//
//        assertThat(originName, is(not(equalTo(newName))));
//        assertThat(changedName, is(equalTo(newName)));
//
//        new AccountProfileApi(RetrofitBuilder.initiate()).changeName(new ReqProfileName("Steve"));
//
//    }
//
//    @Test
//    public void testUpdateSelectTeam() throws Exception {
//
//        // Given
//        long originSelectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
//        long newTeamId = getNewTeamId(originSelectedTeamId);
//
//        // When
//        accountHomeModel.updateSelectTeam(newTeamId);
//
//        // Then
//        long newSavedTeamId = AccountRepository.getRepository().getSelectedTeamId();
//
//        assertThat(originSelectedTeamId, is(not(equalTo(newSavedTeamId))));
//        assertThat(newTeamId, is(equalTo(newSavedTeamId)));
//    }
//
//    private long getNewTeamId(long originSelectedTeamId) {
//        long newTeamId = -1;
//        List<ResAccountInfo.UserTeam> accountTeams = AccountRepository.getRepository().getAccountTeams();
//        for (ResAccountInfo.UserTeam userTeam : accountTeams) {
//            if (originSelectedTeamId != userTeam.getTeamId()) {
//                newTeamId = userTeam.getTeamId();
//                break;
//            }
//        }
//        return newTeamId;
//    }
//
//    @Test
//    public void testGetEntityInfo() throws Exception {
//        // Given
//        long selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
//        TeamInfoLoader instance = TeamInfoLoader.initiate(selectedTeamId);
//        // When
//        InitialInfo initialInfo = accountHomeModel.getEntityInfo(selectedTeamId);
//
//        // Then
//        assertThat(initialInfo, is(notNullValue()));
//        assertThat(instance.getTeamId(), is(equalTo(initialInfo.getTeam().getTopicId())));
//        assertThat(instance.getTeamName(), is(equalTo(initialInfo.getTeam().getName())));
//        assertThat(instance.getTeamDomain(), is(equalTo(initialInfo.getTeam().getDomain())));
//
//    }
//
//    @Test
//    public void testUpdateEntityInfo() throws Exception {
//
//        // Given
//        List<ResAccountInfo.UserTeam> accountTeams = AccountRepository.getRepository().getAccountTeams();
//        long teamId = accountTeams.get(accountTeams.size() - 1).getTeamId();
//        AccountRepository.getRepository().updateSelectedTeamInfo(teamId);
//        InitialInfo leftSideMenu = new StartApi(RetrofitBuilder.initiate()).getInitializeInfo(teamId);
//
//        // When
//        accountHomeModel.updateEntityInfo(leftSideMenu);
//
////        // Then
////        assertThat(entityManager, is(notNullValue()));
////        assertThat(leftSideMenu.team.t_defaultChannelId, is(equalTo(entityManager.getDefaultTopicId())));
////        assertThat(leftSideMenu.joinEntityCount, is(equalTo(entityManager.getJoinedChannels().size() + entityManager.getGroups().size())));
//
//        // Restore
//        accountTeams = AccountRepository.getRepository().getAccountTeams();
//        teamId = accountTeams.get(0).getTeamId();
//        AccountRepository.getRepository().updateSelectedTeamInfo(teamId);
//
//    }
//
//    @Test
//    public void testGetSelectedTeamInfo() throws Exception {
//
//        // Given
//        ResAccountInfo.UserTeam rawSelectedTeam = AccountRepository.getRepository().getSelectedTeamInfo();
//
//        // When
//        ResAccountInfo.UserTeam selectedTeamInfo = accountHomeModel.getSelectedTeamInfo();
//
//        // Then
//        assertThat(rawSelectedTeam.getName(), is(equalTo(selectedTeamInfo.getName())));
//        assertThat(rawSelectedTeam.getTeamDomain(), is(equalTo(selectedTeamInfo.getTeamDomain())));
//        assertThat(rawSelectedTeam.getMemberId(), is(equalTo(selectedTeamInfo.getMemberId())));
//    }
//
//    @Test
//    public void testGetSelectedEmailInfo() throws Exception {
//        // Given
//        ResAccountInfo.UserEmail savedEmail = Observable.from(AccountRepository.getRepository().getAccountEmails())
//                .filter(ResAccountInfo.UserEmail::isPrimary)
//                .toBlocking()
//                .first();
//
//        // When
//        ResAccountInfo.UserEmail selectedEmailInfo = accountHomeModel.getSelectedEmailInfo();
//
//        // Then
//        assertThat(selectedEmailInfo.getTopicId(), is(equalTo(savedEmail.getTopicId())));
//        assertThat(selectedEmailInfo.isPrimary(), is(equalTo(savedEmail.isPrimary())));
//        assertThat(selectedEmailInfo.getStatus(), is(equalTo(savedEmail.getStatus())));
//        assertThat(selectedEmailInfo.getConfirmedAt(), is(equalTo(savedEmail.getConfirmedAt())));
//    }
//
//    @Test
//    public void testGetAccountName() throws Exception {
//        String savedName = AccountRepository.getRepository().getAccountInfo().getName();
//        String accountName = accountHomeModel.getAccountName();
//        assertThat(accountName, is(equalTo(savedName)));
//    }
//
//    @Test
//    public void testCheckAccount() throws Exception {
//        assertThat(accountHomeModel.checkAccount(), is(true));
//    }
//
//    @Component(modules = ApiClientModule.class)
//    public interface AccountHomeModelTestComponent {
//        void inject(AccountHomeModelTest test);
//    }
//}
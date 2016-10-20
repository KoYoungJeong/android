//package com.tosslab.jandi.app.ui.account.presenter;
//
//import android.support.test.runner.AndroidJUnit4;
//
//import com.jayway.awaitility.Awaitility;
//import com.tosslab.jandi.app.JandiApplication;
//import com.tosslab.jandi.app.R;
//import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
//import com.tosslab.jandi.app.network.client.settings.AccountProfileApi;
//import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
//import com.tosslab.jandi.app.network.models.ReqProfileName;
//import com.tosslab.jandi.app.ui.account.dagger.AccountHomeModule;
//
//import org.junit.AfterClass;
//import org.junit.Before;
//import org.junit.BeforeClass;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//import javax.inject.Inject;
//
//import dagger.Component;
//import setup.BaseInitUtil;
//
//import static org.hamcrest.MatcherAssert.assertThat;
//import static org.hamcrest.Matchers.equalTo;
//import static org.hamcrest.Matchers.is;
//import static org.hamcrest.Matchers.not;
//import static org.mockito.Mockito.any;
//import static org.mockito.Mockito.anyObject;
//import static org.mockito.Mockito.anyString;
//import static org.mockito.Mockito.doAnswer;
//import static org.mockito.Mockito.eq;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//
///**
// * Created by jsuch2362 on 15. 11. 11..
// */
//@RunWith(AndroidJUnit4.class)
//public class AccountHomePresenterImplTest {
//
//    @Inject
//    AccountHomePresenter accountHomePresenter;
//    @Inject
//    AccountHomePresenter.View viewMock;
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
//        AccountHomePresenter.View viewMock = mock(AccountHomePresenter.View.class);
//        DaggerAccountHomePresenterImplTest_AccountHomePresenterImplTestComponent.builder()
//                .accountHomeModule(new AccountHomeModule(viewMock))
//                .build()
//                .inject(this);
//    }
//
//    @Test
//    public void testInitViews_InvalidAccess() throws Exception {
//
//        // Given
//        BaseInitUtil.clear();
//        final boolean[] finish = {false};
//        doAnswer(invocationOnMock -> {
//            finish[0] = true;
//            return invocationOnMock;
//        }).when(viewMock).invalidAccess();
//
//        // When
//        accountHomePresenter.onInitialize(true);
//
//        Awaitility.await().until(() -> finish[0]);
//
//        // Then
//        verify(viewMock).invalidAccess();
//
//        BaseInitUtil.initData();
//    }
//
//    @Test
//    public void testInitViews_Wifi_Off() throws Exception {
//
//        // Given
//        BaseInitUtil.disconnectWifi();
//
//        final boolean[] finish = {false};
//        doAnswer(invocationOnMock -> {
//            finish[0] = true;
//            return invocationOnMock;
//        }).when(viewMock).showCheckNetworkDialog();
//        // When
//        accountHomePresenter.onInitialize(true);
//
//        Awaitility.await().until(() -> finish[0]);
//
//        // Then
//        verify(viewMock).showCheckNetworkDialog();
//
//        BaseInitUtil.restoreContext();
//
//    }
//
//    @Test
//    public void testInitViews() throws Exception {
//
//
//        // Given
//        final boolean[] finish = {false};
//        doAnswer(invocationOnMock -> {
//            finish[0] = true;
//            return invocationOnMock;
//        }).when(viewMock).setTeamInfo(any(), any());
//
//        // When
//        accountHomePresenter.onInitialize(true);
//
//        Awaitility.await().until(() -> finish[0]);
//
//        // Then
//        verify(viewMock).setAccountName(anyString());
//        verify(viewMock).setUserEmailText(anyString());
//        verify(viewMock).setTeamInfo(any(), anyObject());
//    }
//
//    @Test
//    public void testOnJoinedTeamSelect_Wrong_TeamId() throws Exception {
//
//        long originSelectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
//
//        try {
//            final boolean[] finish = {false};
//            doAnswer(invocationOnMock -> {
//                finish[0] = true;
//                return invocationOnMock;
//            }).when(viewMock).dismissProgressWheel();
//
//            accountHomePresenter.onJoinedTeamSelect(-1);
//
//            Awaitility.await().until(() -> finish[0]);
//
//            verify(viewMock).dismissProgressWheel();
//
//        } finally {
//            AccountRepository.getRepository().updateSelectedTeamInfo(originSelectedTeamId);
//        }
//    }
//
//    @Test
//    public void testOnJoinedTeamSelect() throws Exception {
//        final boolean[] finish = {false};
//        doAnswer(invocationOnMock -> {
//            finish[0] = true;
//            return invocationOnMock;
//        }).when(viewMock).moveSelectedTeam();
//
//        long selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
//        accountHomePresenter.onJoinedTeamSelect(selectedTeamId);
//
//        Awaitility.await().until(() -> finish[0]);
//
//        verify(viewMock).dismissProgressWheel();
//        verify(viewMock).moveSelectedTeam();
//
//    }
//
//    @Test
//    public void testOnCreateTeamSelect() throws Exception {
//        accountHomePresenter.onCreateTeamSelect();
//
//        verify(viewMock, times(1)).loadTeamCreateActivity();
//    }
//
//    @Test
//    public void testOnAccountNameEditClick() throws Exception {
//        String oldName = "click";
//        accountHomePresenter.onAccountNameEditClick(oldName);
//
//        verify(viewMock, times(1)).showNameEditDialog(eq(oldName));
//
//    }
//
//    @Test
//    public void testOnChangeName() throws Exception {
//
//        // Given
//        String originName = AccountRepository.getRepository().getAccountInfo().getName();
//        String newName = "haha";
//
//        final boolean[] finish = {false};
//        doAnswer(invocationOnMock -> {
//            finish[0] = true;
//            return invocationOnMock;
//        }).when(viewMock).showSuccessToast(anyString());
//
//        // When
//        accountHomePresenter.onChangeName(newName);
//
//        Awaitility.await().until(() -> finish[0]);
//
//        // Then
//        verify(viewMock, times(1)).showProgressWheel();
//        verify(viewMock, times(1)).dismissProgressWheel();
//        verify(viewMock, times(1)).setAccountName(eq(newName));
//        verify(viewMock, times(1)).showSuccessToast(eq(JandiApplication.getContext().getString(R.string.jandi_success_update_account_profile)));
//
//        String newSavedName = AccountRepository.getRepository().getAccountInfo().getName();
//        assertThat(newSavedName, is(not(equalTo(originName))));
//        assertThat(newSavedName, is(equalTo(newName)));
//
//        new AccountProfileApi(RetrofitBuilder.getInstance()).changeName(new ReqProfileName(originName));
//    }
//
//    @Test
//    public void testOnAccountEmailEditClick() throws Exception {
//        accountHomePresenter.onAccountEmailEditClick();
//        verify(viewMock, times(1)).moveEmailEditClick();
//    }
//
////    @Ignore
////    @Test
////    public void testOnTeamCreateAcceptResult() throws Exception {
////
////        final boolean[] successToInvitation = {false};
////        doAnswer(invocationOnMock -> {
////            successToInvitation[0] = true;
////            return invocationOnMock;
////        }).when(viewMock).dismissProgressWheel();
////
////        // When
////        accountHomePresenter.onTeamCreateAcceptResult();
////
////        // Then
////        Awaitility.await().until(() -> successToInvitation[0]);
////        verify(viewMock, times(1)).dismissProgressWheel();
////        verify(viewMock, times(1)).moveSelectedTeam(eq(true));
////
////    }
//
//    @Test
//    public void testOnEmailChooseResult() throws Exception {
//        accountHomePresenter.onEmailChooseResult();
//
//        verify(viewMock, times(1)).setUserEmailText(anyString());
//
//    }
//
//    @Test
//    public void testOnHelpOptionSelect() throws Exception {
//        accountHomePresenter.onHelpOptionSelect();
//
//        verify(viewMock, times(1)).showHelloDialog();
//    }
//
//    @Component(modules = AccountHomeModule.class)
//    public interface AccountHomePresenterImplTestComponent {
//        void inject(AccountHomePresenterImplTest test);
//    }
//
//}
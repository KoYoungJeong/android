package com.tosslab.jandi.app.ui.account.presenter;

import android.support.test.runner.AndroidJUnit4;

import com.jayway.awaitility.Awaitility;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.client.settings.AccountProfileApi;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitAdapterBuilder;
import com.tosslab.jandi.app.network.models.ReqProfileName;
import com.tosslab.jandi.app.network.models.ResAccountInfo;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import setup.BaseInitUtil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyObject;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by jsuch2362 on 15. 11. 11..
 */
@RunWith(AndroidJUnit4.class)
public class AccountHomePresenterImplTest {

    private AccountHomePresenterImpl accountHomePresenter;
    private AccountHomePresenter.View viewMock;

    @BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
    }
    @Before
    public void setUp() throws Exception {
        accountHomePresenter = AccountHomePresenterImpl_.getInstance_(JandiApplication.getContext());
        viewMock = mock(AccountHomePresenter.View.class);
        accountHomePresenter.setView(viewMock);

    }

    @Test
    public void testInitViews_InvalidAccess() throws Exception {

        // Given
        BaseInitUtil.clear();

        // When
        accountHomePresenter.initViews();

        // Then
        verify(viewMock).invalidAccess();
    }

    @Test
    public void testInitViews_Wifi_Off() throws Exception {

        // Given
        BaseInitUtil.disconnectWifi();
        // When
        accountHomePresenter.initViews();

        // Then
        verify(viewMock).showCheckNetworkDialog();

        BaseInitUtil.restoreContext();

    }

    @Test
    public void testInitViews() throws Exception {


        // Given
        final boolean[] finish = {false};
        doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(viewMock).setTeamInfo(any(), any());

        // When
        accountHomePresenter.initViews();

        verify(viewMock).setAccountName(anyString());

        Awaitility.await().until(() -> finish[0]);

        // Then
        verify(viewMock).setTeamInfo(any(), anyObject());

    }

    @Test
    public void testSetView() throws Exception {
        assertThat(accountHomePresenter.view, is(equalTo(viewMock)));
    }

    @Test
    public void testOnJoinedTeamSelect_Wrong_TeamId() throws Exception {
        final boolean[] finish = {false};
        doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(viewMock).dismissProgressWheel();

        accountHomePresenter.onJoinedTeamSelect(-1, false);

        Awaitility.await().until(() -> finish[0]);

        verify(viewMock).dismissProgressWheel();

    }

    @Test
    public void testOnJoinedTeamSelect() throws Exception {
        final boolean[] finish = {false};
        doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(viewMock).dismissProgressWheel();

        long selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
        accountHomePresenter.onJoinedTeamSelect(selectedTeamId, false);

        Awaitility.await().until(() -> finish[0]);

        verify(viewMock, times(1)).dismissProgressWheel();
        verify(viewMock, times(1)).moveSelectedTeam(eq(false));

    }

    @Test
    public void testOnCreateTeamSelect() throws Exception {
        accountHomePresenter.onCreateTeamSelect();

        verify(viewMock, times(1)).loadTeamCreateActivity();
    }

    @Test
    public void testOnAccountNameEditClick() throws Exception {
        String oldName = "click";
        accountHomePresenter.onAccountNameEditClick(oldName);

        verify(viewMock, times(1)).showNameEditDialog(eq(oldName));

    }

    @Test
    public void testOnChangeName() throws Exception {

        // Given
        String originName = AccountRepository.getRepository().getAccountInfo().getName();
        String newName = "haha";

        final boolean[] finish = {false};
        doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(viewMock).showSuccessToast(anyString());

        // When
        accountHomePresenter.onChangeName(newName);

        Awaitility.await().until(() -> finish[0]);

        // Then
        verify(viewMock, times(1)).showProgressWheel();
        verify(viewMock, times(1)).dismissProgressWheel();
        verify(viewMock, times(1)).setAccountName(eq(newName));
        verify(viewMock, times(1)).showSuccessToast(eq(JandiApplication.getContext().getString(R.string.jandi_success_update_account_profile)));

        String newSavedName = AccountRepository.getRepository().getAccountInfo().getName();
        assertThat(newSavedName, is(not(equalTo(originName))));
        assertThat(newSavedName, is(equalTo(newName)));

        new AccountProfileApi(RetrofitAdapterBuilder.newInstance()).changeName(new ReqProfileName(originName));
    }

    @Ignore
    @Test
    public void testOnTeamCreateAcceptResult() throws Exception {

        final boolean[] finish = {false};
        doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(viewMock).dismissProgressWheel();

        // When
        accountHomePresenter.onTeamCreateAcceptResult();

        // Then
        Awaitility.await().until(() -> finish[0]);
        verify(viewMock, times(1)).dismissProgressWheel();
        verify(viewMock, times(1)).moveSelectedTeam(eq(true));

    }

    @Test
    public void testOnAccountEmailEditClick() throws Exception {
        accountHomePresenter.onAccountEmailEditClick();
        verify(viewMock, times(1)).moveEmailEditClick();
    }

    @Test
    public void testOnEmailChooseResult() throws Exception {
        accountHomePresenter.onEmailChooseResult();

        verify(viewMock, times(1)).setUserEmailText(anyString());

    }

    @Test
    public void testOnHelpOptionSelect() throws Exception {
        accountHomePresenter.onHelpOptionSelect();

        verify(viewMock, times(1)).showHelloDialog();
    }

    @Test
    public void testGetTeamInfo() throws Exception {

        final boolean[] finish = {false};
        doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(viewMock).setTeamInfo(any(), any());

        accountHomePresenter.getTeamInfo();

        Awaitility.await().until(() -> finish[0]);

        verify(viewMock, times(1)).setTeamInfo(any(), any(ResAccountInfo.UserTeam.class));

    }
}
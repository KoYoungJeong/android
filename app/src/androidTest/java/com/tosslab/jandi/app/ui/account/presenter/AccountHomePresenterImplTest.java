package com.tosslab.jandi.app.ui.account.presenter;

import android.content.Context;
import android.net.ConnectivityManager;
import android.support.test.runner.AndroidJUnit4;

import com.jayway.awaitility.Awaitility;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import setup.BaseIniUtil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * Created by jsuch2362 on 15. 11. 11..
 */
@RunWith(AndroidJUnit4.class)
public class AccountHomePresenterImplTest {

    private AccountHomePresenterImpl accountHomePresenter;
    private AccountHomePresenter.View viewMock;

    @Before
    public void setUp() throws Exception {
        accountHomePresenter = AccountHomePresenterImpl_.getInstance_(JandiApplication.getContext());
        viewMock = Mockito.mock(AccountHomePresenter.View.class);
        accountHomePresenter.setView(viewMock);

        BaseIniUtil.initData();
    }

    @After
    public void tearDown() throws Exception {
        BaseIniUtil.clear();

    }

    @Test
    public void testInitViews_InvalidAccess() throws Exception {

        // Given
        BaseIniUtil.clear();

        // When
        accountHomePresenter.initViews();

        // Then
        Mockito.verify(viewMock).invalidAccess();
    }

    @Test
    public void testInitViews_Wifi_Off() throws Exception {

        // Given
        Context tempContext = JandiApplication.getContext();
        JandiApplication.setContext(Mockito.mock(Context.class));
        ConnectivityManager connectivityManager = Mockito.mock(ConnectivityManager.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.doReturn(null).when(connectivityManager).getActiveNetworkInfo();
        Mockito.doReturn(connectivityManager).when(JandiApplication.getContext()).getSystemService(Context.CONNECTIVITY_SERVICE);

        // When
        accountHomePresenter.initViews();

        // Then
        Mockito.verify(viewMock).showCheckNetworkDialog();

        JandiApplication.setContext(tempContext);

    }

    @Test
    public void testInitViews() throws Exception {


        // Given
        final boolean[] finish = {false};
        Mockito.doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(viewMock).setTeamInfo(Mockito.any(), Mockito.any());

        // When
        accountHomePresenter.initViews();

        Mockito.verify(viewMock).setAccountName(Mockito.anyString());

        Awaitility.await().until(() -> finish[0]);

        // Then
        Mockito.verify(viewMock).setTeamInfo(Mockito.any(), Mockito.anyObject());

    }

    @Test
    public void testSetView() throws Exception {
        assertThat(accountHomePresenter.view, is(equalTo(viewMock)));
    }

    @Test
    public void testOnJoinedTeamSelect_Wrong_TeamId() throws Exception {
        final boolean[] finish = {false};
        Mockito.doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(viewMock).dismissProgressWheel();

        accountHomePresenter.onJoinedTeamSelect(-1, false);

        Awaitility.await().until(() -> finish[0]);

        Mockito.verify(viewMock).dismissProgressWheel();

    }

    @Test
    public void testOnJoinedTeamSelect() throws Exception {
        final boolean[] finish = {false};
        Mockito.doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(viewMock).dismissProgressWheel();

        int selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
        accountHomePresenter.onJoinedTeamSelect(selectedTeamId, false);

        Awaitility.await().until(() -> finish[0]);

        Mockito.verify(viewMock, Mockito.times(1)).dismissProgressWheel();
        Mockito.verify(viewMock, Mockito.times(1)).moveSelectedTeam(Mockito.eq(false));

    }

    @Test
    public void testOnCreateTeamSelect() throws Exception {

    }

    @Test
    public void testOnAccountNameEditClick() throws Exception {

    }

    @Test
    public void testOnChangeName() throws Exception {

    }

    @Test
    public void testOnTeamCreateAcceptResult() throws Exception {

    }

    @Test
    public void testOnAccountEmailEditClick() throws Exception {

    }

    @Test
    public void testOnEmailChooseResult() throws Exception {

    }

    @Test
    public void testOnRequestJoin() throws Exception {

    }

    @Test
    public void testOnRequestIgnore() throws Exception {

    }

    @Test
    public void testOnHelpOptionSelect() throws Exception {

    }

    @Test
    public void testGetTeamInfo() throws Exception {

    }
}
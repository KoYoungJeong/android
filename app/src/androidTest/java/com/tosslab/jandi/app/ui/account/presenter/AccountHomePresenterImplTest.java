package com.tosslab.jandi.app.ui.account.presenter;

import android.support.test.runner.AndroidJUnit4;

import com.jayway.awaitility.Awaitility;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqProfileName;
import com.tosslab.jandi.app.network.models.ResAccountInfo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import setup.BaseInitUtil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

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

        BaseInitUtil.initData();
    }

    @After
    public void tearDown() throws Exception {
        BaseInitUtil.clear();

    }

    @Test
    public void testInitViews_InvalidAccess() throws Exception {

        // Given
        BaseInitUtil.clear();

        // When
        accountHomePresenter.initViews();

        // Then
        Mockito.verify(viewMock).invalidAccess();
    }

    @Test
    public void testInitViews_Wifi_Off() throws Exception {

        // Given
        BaseInitUtil.disconnectWifi();
        // When
        accountHomePresenter.initViews();

        // Then
        Mockito.verify(viewMock).showCheckNetworkDialog();

        BaseInitUtil.restoreContext();

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
        accountHomePresenter.onCreateTeamSelect();

        Mockito.verify(viewMock, Mockito.times(1)).loadTeamCreateActivity();
    }

    @Test
    public void testOnAccountNameEditClick() throws Exception {
        String oldName = "click";
        accountHomePresenter.onAccountNameEditClick(oldName);

        Mockito.verify(viewMock, Mockito.times(1)).showNameEditDialog(Mockito.eq(oldName));

    }

    @Test
    public void testOnChangeName() throws Exception {

        // Given
        String originName = AccountRepository.getRepository().getAccountInfo().getName();
        String newName = "haha";

        final boolean[] finish = {false};
        Mockito.doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(viewMock).showSuccessToast(Mockito.anyString());

        // When
        accountHomePresenter.onChangeName(newName);

        Awaitility.await().until(() -> finish[0]);

        // Then
        Mockito.verify(viewMock, Mockito.times(1)).showProgressWheel();
        Mockito.verify(viewMock, Mockito.times(1)).dismissProgressWheel();
        Mockito.verify(viewMock, Mockito.times(1)).setAccountName(Mockito.eq(newName));
        Mockito.verify(viewMock, Mockito.times(1)).showSuccessToast(Mockito.eq(JandiApplication.getContext().getString(R.string.jandi_success_update_account_profile)));

        String newSavedName = AccountRepository.getRepository().getAccountInfo().getName();
        assertThat(newSavedName, is(not(equalTo(originName))));
        assertThat(newSavedName, is(equalTo(newName)));

        RequestApiManager.getInstance().changeNameByAccountProfileApi(new ReqProfileName(originName));
    }

    @Test
    public void testOnTeamCreateAcceptResult() throws Exception {

        final boolean[] finish = {false};
        Mockito.doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(viewMock).dismissProgressWheel();

        // When
        accountHomePresenter.onTeamCreateAcceptResult();

        // Then
        Awaitility.await().until(() -> finish[0]);
        Mockito.verify(viewMock, Mockito.times(1)).dismissProgressWheel();
        Mockito.verify(viewMock, Mockito.times(1)).showProgressWheel();

    }

    @Test
    public void testOnAccountEmailEditClick() throws Exception {
        accountHomePresenter.onAccountEmailEditClick();
        Mockito.verify(viewMock, Mockito.times(1)).moveEmailEditClick();
    }

    @Test
    public void testOnEmailChooseResult() throws Exception {
        accountHomePresenter.onEmailChooseResult();

        Mockito.verify(viewMock, Mockito.times(1)).setUserEmailText(Mockito.anyString());

    }

    @Test
    public void testOnHelpOptionSelect() throws Exception {
        accountHomePresenter.onHelpOptionSelect();

        Mockito.verify(viewMock, Mockito.times(1)).showHelloDialog();
    }

    @Test
    public void testGetTeamInfo() throws Exception {

        final boolean[] finish = {false};
        Mockito.doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(viewMock).setTeamInfo(Mockito.any(), Mockito.any());

        accountHomePresenter.getTeamInfo();

        Awaitility.await().until(() -> finish[0]);

        Mockito.verify(viewMock, Mockito.times(1)).setTeamInfo(Mockito.any(), Mockito.any(ResAccountInfo.UserTeam.class));

    }
}
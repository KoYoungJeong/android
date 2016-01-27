package com.tosslab.jandi.app.ui.maintab.more.presenter;

import android.support.test.runner.AndroidJUnit4;
import android.view.View;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import setup.BaseInitUtil;

import static com.jayway.awaitility.Awaitility.await;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
public class MainMorePresenterImplTest {

    private MainMorePresenter mainMorePresenter;
    private MainMorePresenter.View mockView;

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData();
        mainMorePresenter = MainMorePresenterImpl_.getInstance_(JandiApplication.getContext());
        mockView = Mockito.mock(MainMorePresenter.View.class);
        mainMorePresenter.setView(mockView);
    }

    @After
    public void tearDown() throws Exception {
        BaseInitUtil.clear();
    }

    @Test
    public void testOnShowJandiVersion() throws Exception {

        final boolean[] finish = {false};
        final int[] visible = new int[1];
        doAnswer(invocationOnMock -> {
            finish[0] = true;
            visible[0] = (int) invocationOnMock.getArguments()[0];
            return invocationOnMock;
        }).when(mockView).setVersionButtonVisibility(anyInt());

        mainMorePresenter.showJandiVersion();

        await().until(() -> finish[0]);

        verify(mockView).setJandiVersion(anyString());
        verify(mockView).setVersionButtonVisibility(anyInt());

        if (visible[0] == View.VISIBLE) {
            verify(mockView).setLatestVersion(anyInt());
        }

    }

    @Test
    public void testOnShowOtherTeamMessageCount() throws Exception {
        mainMorePresenter.showOtherTeamMessageCount();
        verify(mockView).setOtherTeamBadgeCount(anyInt());
    }

    @Test
    public void testOnShowTeamMember() throws Exception {
        mainMorePresenter.showTeamMember();
        verify(mockView).setMemberTextWithCount(anyString());
    }

    @Test
    public void testOnShowUserProfile() throws Exception {
        mainMorePresenter.showUserProfile();
        verify(mockView).showUserProfile(any());
    }

    @Test
    public void testOnLaunchHelpPage() throws Exception {
        mainMorePresenter.onLaunchHelpPage();
        verify(mockView).launchHelpPageOnBrowser(contains("https://jandi.zendesk.com/hc"));
    }

    @Test
    public void testOnReportUserInfo() throws Exception {
        // when 최초 클릭
        for (int idx = 0; idx < 5; idx++) {
            mainMorePresenter.onReportUserInfo();
            Thread.sleep(5);
        }

        verify(mockView).showBugReportDialog(any(), EntityManager.getInstance().getMe().getName());

    }
}
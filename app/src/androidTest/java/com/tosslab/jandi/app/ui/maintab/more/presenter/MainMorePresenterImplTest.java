package com.tosslab.jandi.app.ui.maintab.more.presenter;

import android.support.test.runner.AndroidJUnit4;
import android.view.View;

import com.tosslab.jandi.app.JandiApplication;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import setup.BaseInitUtil;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
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
        VersionClickedInfo versionClickInfo = new VersionClickedInfo();

        // when 최초 클릭
        mainMorePresenter.onReportUserInfo();
        // then 클릭 1회 카운팅
        assertThat(versionClickInfo.getCount(), is(equalTo(1)));

        // when 두번째 클릭
        versionClickInfo.setTime(System.currentTimeMillis() - 1);
        mainMorePresenter.onReportUserInfo();
        // then 클릭 2회 카운팅, 시간은 현재보다 작은 값으로 설정
        assertThat(versionClickInfo.getCount(), is(equalTo(2)));
        assertThat(versionClickInfo.getTime(), is(lessThanOrEqualTo(System.currentTimeMillis())));

        // Given 최초 클릭을 현재보다 3초 이전으로 설정
        versionClickInfo.setTime(System.currentTimeMillis() - 3001);
        // when
        mainMorePresenter.onReportUserInfo();
        // then 클릭 정보 초기화
        assertThat(versionClickInfo.getCount(), is(equalTo(1)));

        // given 3초 이내에 5회 이상 터치 인식
        versionClickInfo.setTime(System.currentTimeMillis() - 100);
        versionClickInfo.setCount(5);
        // when
        mainMorePresenter.onReportUserInfo();
        verify(mockView).showBugReportDialog(any());

    }
}
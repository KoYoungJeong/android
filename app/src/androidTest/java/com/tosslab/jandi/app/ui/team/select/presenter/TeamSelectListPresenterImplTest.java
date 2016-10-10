package com.tosslab.jandi.app.ui.team.select.presenter;

import android.support.test.runner.AndroidJUnit4;

import com.jayway.awaitility.Awaitility;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.ui.team.select.adapter.TeamSelectListAdapter;
import com.tosslab.jandi.app.ui.team.select.dagger.TeamSelectListModule;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import dagger.Component;
import setup.BaseInitUtil;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by tee on 2016. 10. 4..
 */

@RunWith(AndroidJUnit4.class)
public class TeamSelectListPresenterImplTest {

    @Inject
    TeamSelectListPresenter teamSelectListPresenter;
    @Inject
    TeamSelectListPresenter.View mockView;

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
        TeamSelectListPresenter.View viewMock = mock(TeamSelectListPresenter.View.class);
        TeamSelectListAdapter mockAdapter = mock(TeamSelectListAdapter.class);
        DaggerTeamSelectListPresenterImplTest_TeamSelectListPresenterImplTestComponent.builder()
                .teamSelectListModule(new TeamSelectListModule(viewMock, mockAdapter))
                .build()
                .inject(this);
    }

    @Test
    public void testInitTeamDatas() {
        // Given
        final boolean[] finish = {false};
        doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(mockView).showList();

        // When
        teamSelectListPresenter.initTeamDatas(false, true);

        Awaitility.await().until(() -> finish[0]);

        //then
        verify(mockView, times(1)).showList();
    }

    @Test
    public void testOnEnterSelectedTeam() {
        // Given
        final boolean[] finish = {false};

        doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(mockView).moveSelectedTeam();

        final long teamId = AccountRepository.getRepository().getSelectedTeamId();

        // When
        teamSelectListPresenter.onEnterSelectedTeam(teamId);

        Awaitility.await().until(() -> finish[0]);

        //then
        verify(mockView, times(1)).showProgressWheel();
        verify(mockView, times(1)).dismissProgressWheel();
        verify(mockView, times(1)).moveSelectedTeam();
    }


    @Component(modules = TeamSelectListModule.class)
    public interface TeamSelectListPresenterImplTestComponent {
        void inject(TeamSelectListPresenterImplTest test);
    }

}
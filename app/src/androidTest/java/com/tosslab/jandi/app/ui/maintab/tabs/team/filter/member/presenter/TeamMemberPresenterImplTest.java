package com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.presenter;

import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.adapter.TeamMemberDataModel;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.adapter.ToggleCollector;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.dagger.TeamMemberModule;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.inject.Inject;

import dagger.Component;
import setup.BaseInitUtil;

import static org.assertj.core.api.Assertions.assertThat;

@org.junit.runner.RunWith(android.support.test.runner.AndroidJUnit4.class)
public class TeamMemberPresenterImplTest {


    @Mock
    TeamMemberPresenter.View view;
    @Mock
    TeamMemberDataModel dataModel;
    @Mock
    ToggleCollector collector;

    @Inject
    TeamMemberPresenter presenter;
    private TeamMemberPresenterImpl presenterImpl;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
    }

    @Before
    public void setUp() throws Exception {

        try {
            MockitoAnnotations.initMocks(this);
        } catch (Exception e) {
        }

        DaggerTeamMemberPresenterImplTest_TestComponent.builder()
                .teamMemberModule(new TeamMemberModule(view, dataModel, collector, true, TeamInfoLoader.getInstance().getDefaultTopicId()))
                .build()
                .inject(this);

        presenterImpl = ((TeamMemberPresenterImpl) presenter);
    }

    @Test
    public void onCreate() throws Exception {
        presenter.onCreate();
        assertThat(presenterImpl.filterSubject).isNotNull();
        assertThat(presenterImpl.filterSubscription.isUnsubscribed()).isFalse();
        assertThat(presenterImpl.filterSubscription.isUnsubscribed()).isFalse();

    }

    @Test
    public void onDestroy() throws Exception {

    }

    @Test
    public void onItemClick() throws Exception {

    }

    @Test
    public void addToggledUser() throws Exception {

    }

    @Test
    public void addToggleOfAll() throws Exception {

    }

    @Test
    public void onUserSelect() throws Exception {

    }

    @Test
    public void clearToggle() throws Exception {

    }

    @Test
    public void inviteToggle() throws Exception {

    }

    @Test
    public void onSearchKeyword() throws Exception {

    }

    @Component(modules = TeamMemberModule.class)
    interface TestComponent {
        void inject(TeamMemberPresenterImplTest test);
    }
}
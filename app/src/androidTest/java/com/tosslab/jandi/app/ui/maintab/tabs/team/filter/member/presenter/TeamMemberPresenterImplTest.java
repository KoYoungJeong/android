package com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.presenter;

import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.adapter.TeamMemberDataModel;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.adapter.ToggleCollector;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.dagger.TeamMemberModule;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.domain.TeamDisabledMemberItem;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.domain.TeamMemberItem;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.model.TeamMemberModel;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;

import javax.inject.Inject;

import dagger.Component;
import rx.Observable;
import setup.BaseInitUtil;
import setup.RxJavaUtil;

import static com.jayway.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@org.junit.runner.RunWith(android.support.test.runner.AndroidJUnit4.class)
public class TeamMemberPresenterImplTest {


    @Mock
    TeamMemberPresenter.View view;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
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

    }

    @Test
    public void onDestroy() throws Exception {
        presenter.onCreate();
        presenter.onDestroy();
        assertThat(presenterImpl.filterSubject.hasCompleted()).isTrue();
        assertThat(presenterImpl.filterSubscription.isUnsubscribed()).isTrue();
    }

    @Test
    public void onItemClick_disabled() throws Exception {
        presenter.onCreate();
        doReturn(new TeamDisabledMemberItem(null, null)).when(dataModel).getItem(anyInt());
        presenter.onItemClick(0);
        verify(view).moveDisabledMembers();

    }

    @Test
    public void onItemClick_not_select() throws Exception {
        presenter.onCreate();

        reset(view, dataModel);
        doReturn(getMyTeamMemberItem()).when(dataModel).getItem(anyInt());
        presenterImpl.setSelectMode(false);
        presenter.onItemClick(0);

        verify(view).moveProfile(eq(TeamInfoLoader.getInstance().getMyId()));

    }

    @Test
    public void onItemClick_multi_select() throws Exception {
        presenter.onCreate();

        reset(view, dataModel);
        doReturn(getMyTeamMemberItem()).when(dataModel).getItem(anyInt());
        presenterImpl.setSelectMode(true);
        presenterImpl.setRoomId(getNotDefaultTopic());
        presenter.onItemClick(0);

        verify(view).updateToggledUser(anyInt());
        verify(view).refreshDataView();
    }

    @Test
    public void onItemClick_pick() throws Exception {
        presenter.onCreate();

        reset(view, dataModel);
        doReturn(getMyTeamMemberItem()).when(dataModel).getItem(anyInt());
        presenterImpl.setSelectMode(true);
        presenterImpl.setRoomId(-1);
        presenter.onItemClick(0);

        verify(view).moveDirectMessage(anyLong(), anyLong(), anyLong(), anyLong());
    }

    private long getNotDefaultTopic() {
        return Observable.from(TeamInfoLoader.getInstance().getTopicList())
                .takeFirst(it -> it.getId() != TeamInfoLoader.getInstance().getDefaultTopicId())
                .map(TopicRoom::getId)
                .toBlocking().first();
    }

    @Test
    public void addToggledUser() throws Exception {
        presenter.addToggledUser(new long[]{1, 2, 3});
        verify(view).refreshDataView();
        verify(view).updateToggledUser(eq(0));
    }

    @Test
    public void addToggleOfAll_size_0() throws Exception {
        doReturn(0).when(dataModel).getSize();
        presenter.addToggleOfAll();
        verify(collector, never()).addId(anyLong());
    }

    @Test
    public void addToggleOfAll_size_greater_0() throws Exception {
        doReturn(1).when(dataModel).getSize();
        when(dataModel.getItem(0).getChatChooseItem().getEntityId()).thenReturn(TeamInfoLoader.getInstance().getMyId());
        presenter.addToggleOfAll();

        verify(collector).addId(eq(TeamInfoLoader.getInstance().getMyId()));
        verify(view).refreshDataView();
        verify(view).updateToggledUser(anyInt());
    }

    @Test
    public void onUserSelect_me() throws Exception {
        long myId = TeamInfoLoader.getInstance().getMyId();
        long teamId = TeamInfoLoader.getInstance().getTeamId();
        presenter.onUserSelect(myId);
        verify(view).moveDirectMessage(eq(teamId), eq(myId), anyLong(), anyLong());
    }

    @Test
    public void onUserSelect_unknown() throws Exception {
        long myId = 1;
        long teamId = TeamInfoLoader.getInstance().getTeamId();
        presenter.onUserSelect(myId);
        verify(view).moveDirectMessage(eq(teamId), eq(myId), eq(-1L), eq(-1L));
    }

    @Test
    public void onUserSelect_other_member() throws Exception {
        long otherMemberId = getOtherMember();
        long teamId = TeamInfoLoader.getInstance().getTeamId();
        presenter.onUserSelect(otherMemberId);
        verify(view).moveDirectMessage(eq(teamId), eq(otherMemberId), anyInt(), anyInt());
    }

    @Test
    public void clearToggle() throws Exception {
        presenter.clearToggle();
        verify(collector).clearIds();
        verify(collector).count();
        verify(view).refreshDataView();
        verify(view).updateToggledUser(anyInt());
    }

    @Test
    public void inviteToggle_success() throws Exception {

        final boolean[] finish = {false};
        doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(view).successToInvitation();

        TeamMemberModel teamMemberModel = mock(TeamMemberModel.class);
        doReturn(Observable.just(new ResCommon())).when(teamMemberModel).deferInvite(eq(collector), anyLong());
        doReturn(new ArrayList<Long>()).when(collector).getIds();

        presenterImpl.teamMemberModel = teamMemberModel;
        presenter.inviteToggle();

        await().until(() -> finish[0]);
        verify(view).successToInvitation();
        verify(view).dismissProgress();

    }

    @Test
    public void inviteToggle_fail() throws Exception {

        TeamMemberModel teamMemberModel = mock(TeamMemberModel.class);
        presenterImpl.teamMemberModel = teamMemberModel;
        doReturn(Observable.error(new Exception())).when(teamMemberModel).deferInvite(any(), anyLong());

        RxJavaUtil.threadHook();
        presenter.inviteToggle();
        Thread.sleep(1000);
        verify(view).showFailToInvitation();
        verify(view).dismissProgress();
        RxJavaUtil.reset();

    }

    @Test
    public void onSearchKeyword() throws Exception {
        presenter.onCreate();

        presenter.onSearchKeyword("a");

        assertThat(presenterImpl.filterSubject.getValue()).isEqualToIgnoringCase("a");
    }

    private TeamMemberItem getMyTeamMemberItem() {
        return new TeamMemberItem(TeamInfoLoader.getInstance().getUser(TeamInfoLoader.getInstance().getMyId()), "");
    }

    private long getOtherMember() {
        return Observable.from(TeamInfoLoader.getInstance().getUserList())
                .takeFirst(it -> it.getId() != TeamInfoLoader.getInstance().getMyId())
                .map(User::getId)
                .toBlocking()
                .first();
    }

    @Component(modules = TeamMemberModule.class)
    interface TestComponent {
        void inject(TeamMemberPresenterImplTest test);
    }
}
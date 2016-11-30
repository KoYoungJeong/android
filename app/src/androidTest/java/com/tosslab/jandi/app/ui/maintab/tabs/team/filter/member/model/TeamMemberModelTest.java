package com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.model;

import com.tosslab.jandi.app.network.client.privatetopic.GroupApi;
import com.tosslab.jandi.app.network.client.publictopic.ChannelApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.adapter.ToggleCollector;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.domain.TeamMemberItem;

import org.assertj.core.api.Condition;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.observers.TestSubscriber;
import setup.BaseInitUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

@org.junit.runner.RunWith(android.support.test.runner.AndroidJUnit4.class)
public class TeamMemberModelTest {
    private TeamMemberModel teamMemberModel;
    private GroupApi groupApi;
    private ChannelApi channelApi;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
    }

    @Before
    public void setUp() throws Exception {
        channelApi = mock(ChannelApi.class);
        groupApi = mock(GroupApi.class);
        teamMemberModel = new TeamMemberModel(() -> channelApi, () -> groupApi);

    }

    @Test
    public void deferInvite_error() throws Exception {
        ToggleCollector toggleCollector = mock(ToggleCollector.class);
        doReturn(new ArrayList<Long>()).when(toggleCollector).getIds();
        doThrow(RetrofitException.create(400, new Exception())).when(channelApi).invitePublicTopic(anyLong(), any());

        TestSubscriber<ResCommon> subscriber = new TestSubscriber<>();
        teamMemberModel.deferInvite(toggleCollector, TeamInfoLoader.getInstance().getDefaultTopicId())
                .subscribe(subscriber);

        subscriber.awaitTerminalEvent();

        subscriber.assertError(RetrofitException.class);
    }

    @Test
    public void deferInvite_success() throws Exception {
        ToggleCollector toggleCollector = mock(ToggleCollector.class);
        doReturn(new ArrayList<Long>()).when(toggleCollector).getIds();
        doReturn(new ResCommon()).when(channelApi).invitePublicTopic(anyLong(), any());

        TestSubscriber<ResCommon> subscriber = new TestSubscriber<>();
        teamMemberModel.deferInvite(toggleCollector, TeamInfoLoader.getInstance().getDefaultTopicId())
                .subscribe(subscriber);

        subscriber.awaitTerminalEvent();

        subscriber.assertCompleted();
    }

    @Test
    public void getFilteredUser_default() throws Exception {
        {
            TestSubscriber<TeamMemberItem> subscriber = new TestSubscriber<>();
            // 봇 제외한 모든 유저
            teamMemberModel.getFilteredUser("", false, -1)
                    .subscribe(subscriber);

            subscriber.awaitTerminalEvent();

            assertThat(subscriber.getValueCount()).isGreaterThanOrEqualTo(getEnabledUserCount());
        }

        {
            String targetName = getOtherName();

            TestSubscriber<TeamMemberItem> subscriber = new TestSubscriber<>();
            teamMemberModel.getFilteredUser(targetName, false, -1)
                    .subscribe(subscriber);

            subscriber.awaitTerminalEvent();

            assertThat(subscriber.getValueCount()).isLessThanOrEqualTo(getEnabledUserCount());
            assertThat(subscriber.getOnNextEvents())
                    .extracting(TeamMemberItem::getChatChooseItem)
                    .extracting(ChatChooseItem::getName)
                    .are(new Condition<String>() {
                        @Override
                        public boolean matches(String s) {
                            return s.contains(targetName);
                        }
                    });

        }
    }

    @Test
    public void getFilteredUser_multiselect() throws Exception {

        {
            TestSubscriber<TeamMemberItem> subscriber = new TestSubscriber<>();
            teamMemberModel.getFilteredUser("", true, TeamInfoLoader.getInstance().getDefaultTopicId())
                    .subscribe(subscriber);

            subscriber.awaitTerminalEvent();
            subscriber.assertValueCount(0);
        }

        {
            TopicRoom otherTopic = getOtherTopicId();
            TestSubscriber<TeamMemberItem> subscriber = new TestSubscriber<>();
            teamMemberModel.getFilteredUser("", true, otherTopic.getId())
                    .subscribe(subscriber);

            subscriber.awaitTerminalEvent();

            assertThat(subscriber.getOnNextEvents().size())
                    .isGreaterThanOrEqualTo(1);

            assertThat(subscriber.getOnNextEvents())
                    .extracting(TeamMemberItem::getChatChooseItem)
                    .extracting(ChatChooseItem::getEntityId)
                    .doesNotContain(TeamInfoLoader.getInstance().getMyId());

        }

        {
            TopicRoom otherTopic = getOtherTopicId();
            String otherName = getOtherName(otherTopic.getId());
            TestSubscriber<TeamMemberItem> subscriber = new TestSubscriber<>();
            teamMemberModel.getFilteredUser(otherName, true, otherTopic.getId())
                    .subscribe(subscriber);

            subscriber.awaitTerminalEvent();

            assertThat(subscriber.getOnNextEvents().size())
                    .isGreaterThanOrEqualTo(1);

            assertThat(subscriber.getOnNextEvents())
                    .extracting(TeamMemberItem::getChatChooseItem)
                    .extracting(ChatChooseItem::getEntityId)
                    .doesNotContain(TeamInfoLoader.getInstance().getMyId());
            assertThat(subscriber.getOnNextEvents())
                    .extracting(TeamMemberItem::getChatChooseItem)
                    .extracting(ChatChooseItem::getName)
                    .are(new Condition<String>() {
                        @Override
                        public boolean matches(String s) {
                            return s.contains(otherName);
                        }
                    });

        }

    }

    @Test
    public void getFilteredUser_pick() throws Exception {

        {
            TestSubscriber<TeamMemberItem> subscriber = new TestSubscriber<>();
            teamMemberModel.getFilteredUser("", true, -1)
                    .subscribe(subscriber);
            subscriber.awaitTerminalEvent();

            assertThat(subscriber.getOnNextEvents())
                    .extracting(TeamMemberItem::getChatChooseItem)
                    .extracting(ChatChooseItem::getEntityId)
                    .doesNotContain(TeamInfoLoader.getInstance().getMyId());
        }

        {
            String otherName = getOtherName();
            TestSubscriber<TeamMemberItem> subscriber = new TestSubscriber<>();
            teamMemberModel.getFilteredUser(otherName, true, -1)
                    .subscribe(subscriber);
            subscriber.awaitTerminalEvent();

            assertThat(subscriber.getOnNextEvents().size()).isGreaterThanOrEqualTo(1);
            assertThat(subscriber.getOnNextEvents())
                    .extracting(TeamMemberItem::getChatChooseItem)
                    .extracting(ChatChooseItem::getEntityId)
                    .doesNotContain(TeamInfoLoader.getInstance().getMyId());

            assertThat(subscriber.getOnNextEvents())
                    .extracting(TeamMemberItem::getChatChooseItem)
                    .extracting(ChatChooseItem::getName)
                    .are(new Condition<String>() {
                        @Override
                        public boolean matches(String s) {
                            return s.contains(otherName);
                        }
                    });
        }

    }

    private String getOtherName(long topicId) {
        String otherName = Observable.from(TeamInfoLoader.getInstance().getUserList())
                .map(User::getId)
                .takeFirst(it -> !TeamInfoLoader.getInstance().getTopic(topicId).getMembers().contains(it))
                .map(it -> TeamInfoLoader.getInstance().getUser(it))
                .map(User::getName)
                .toBlocking().first();
        if (otherName.length() > 1) {
            return otherName.substring(0, 1);
        } else {
            return otherName;
        }

    }


    private TopicRoom getOtherTopicId() {
        return Observable.from(TeamInfoLoader.getInstance().getTopicList())
                .takeFirst(it -> it.getId() != TeamInfoLoader.getInstance().getDefaultTopicId())
                .toBlocking().first();
    }

    private String getOtherName() {
        String otherName = Observable.from(TeamInfoLoader.getInstance().getUserList())
                .takeFirst(it -> it.isEnabled() && it.getId() != TeamInfoLoader.getInstance().getMyId())
                .map((user) -> {
                    if (user.isInactive()) {
                        return user.getEmail();
                    } else {
                        return user.getName();
                    }
                })
                .toBlocking().firstOrDefault("");
        if (otherName.length() > 1) {
            return otherName.substring(0, 1);
        } else {
            return otherName;
        }
    }

    private int getUserSize() {return TeamInfoLoader.getInstance().getUserList().size();}

    private int getEnabledUserCount() {
        List<User> userList = TeamInfoLoader.getInstance().getUserList();
        return Observable.from(userList)
                .filter(User::isEnabled)
                .filter((user) -> !user.isBot())
                .count()
                .toBlocking()
                .firstOrDefault(0);
    }


}
package com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.model;

import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.domain.TeamMemberItem;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import rx.Observable;
import rx.observers.TestSubscriber;
import setup.BaseInitUtil;

import static org.assertj.core.api.Assertions.assertThat;

@org.junit.runner.RunWith(android.support.test.runner.AndroidJUnit4.class)
public class TeamMemberModelTest {

    private TeamMemberModel teamMemberModel;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
    }

    @Before
    public void setUp() throws Exception {
        teamMemberModel = new TeamMemberModel();

    }

    @Test
    public void getFilteredUser_default() throws Exception {
        TestSubscriber<TeamMemberItem> subscriber = new TestSubscriber<>();
        teamMemberModel.getFilteredUser("", false, -1)
                .subscribe(subscriber);

        subscriber.awaitTerminalEvent();

        assertThat(subscriber.getValueCount()).isGreaterThanOrEqualTo(getEnabledUserCount());
        assertThat(subscriber.getValueCount() > getUserSize()).isEqualTo(getUserSize() > getEnabledUserCount());
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
            TestSubscriber<TeamMemberItem> subscriber = new TestSubscriber<>();
            teamMemberModel.getFilteredUser("", true, -1)
                    .subscribe(subscriber);
            subscriber.awaitTerminalEvent();

            assertThat(subscriber.getOnNextEvents())
                    .extracting(TeamMemberItem::getChatChooseItem)
                    .extracting(ChatChooseItem::getEntityId)
                    .doesNotContain(TeamInfoLoader.getInstance().getMyId());
        }
    }

    private int getUserSize() {return TeamInfoLoader.getInstance().getUserList().size();}

    private int getEnabledUserCount() {
        List<User> userList = TeamInfoLoader.getInstance().getUserList();
        return Observable.from(userList)
                .filter(User::isEnabled)
                .count()
                .toBlocking()
                .firstOrDefault(0);
    }


}
package com.tosslab.jandi.app.ui.maintab.tabs.team.info;

import android.support.test.rule.ActivityTestRule;
import android.view.MenuItem;

import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.utils.ApplicationUtil;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import rx.Observable;
import rx.observers.TestSubscriber;
import setup.BaseInitUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@org.junit.runner.RunWith(android.support.test.runner.AndroidJUnit4.class)
public class TeamInfoActivityTest {

    @Rule
    public ActivityTestRule<TeamInfoActivity> rule = new ActivityTestRule<>(TeamInfoActivity.class);
    private TeamInfoActivity activity;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
    }

    @Before
    public void setUp() throws Exception {
        activity = rule.getActivity();

    }

    @Test
    public void setUpTeamName() throws Exception {
        String content = activity.labelTeamName.getContent();
        assertThat(content).isEqualTo(TeamInfoLoader.getInstance().getTeamName());

    }

    @Test
    public void setUpTeamUrl() throws Exception {
        String content = activity.labelTeamUrl.getContent();
        assertThat(content).isEqualTo(String.format("%s.jandi.com", TeamInfoLoader.getInstance().getTeamDomain()));
    }

    @Test
    public void setUpTeamAdmin() throws Exception {

        String content = activity.labelTeamAdmin.getContent();

        TestSubscriber<String> subscriber = new TestSubscriber<>();
        Observable.from(TeamInfoLoader.getInstance().getUserList())
                .filter(User::isTeamOwner)
                .map(User::getName)
                .subscribe(subscriber);

        subscriber.awaitTerminalEvent();
        subscriber.assertValue(content);

    }

    @Test
    public void setUpMemberCount() throws Exception {

        String content = activity.labelTeamMemberCount.getContent();
        TestSubscriber<Integer> subscriber = new TestSubscriber<>();
        Observable.from(TeamInfoLoader.getInstance().getUserList())
                .filter(User::isEnabled)
                .count()
                .defaultIfEmpty(0)
                .subscribe(subscriber);

        subscriber.awaitTerminalEvent();

        subscriber.assertValue(Integer.parseInt(content));
    }

    @Test
    public void setUpAppVersion() throws Exception {
        String version = activity.tvAppVersion.getText().toString();
        String versionName = String.format("%s : %s", "App Version", ApplicationUtil.getAppVersionName());

        assertThat(version).isEqualTo(versionName);
    }

    @Test
    public void optionHome() throws Exception {
        MenuItem mock = mock(MenuItem.class);
        doReturn(android.R.id.home).when(mock).getItemId();
        activity.onOptionsItemSelected(mock);

        assertThat(activity.isFinishing()).isTrue();

    }
}
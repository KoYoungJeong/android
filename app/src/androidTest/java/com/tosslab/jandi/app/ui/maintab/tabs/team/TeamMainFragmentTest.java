package com.tosslab.jandi.app.ui.maintab.tabs.team;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.view.MenuItem;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.RequestInviteMemberEvent;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.search.TeamMemberSearchActivity;
import com.tosslab.jandi.app.ui.maintab.tabs.team.info.TeamInfoActivity;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import de.greenrobot.event.EventBus;
import setup.BaseInitUtil;

import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@org.junit.runner.RunWith(android.support.test.runner.AndroidJUnit4.class)
public class TeamMainFragmentTest {

    @Rule
    public IntentsTestRule<BaseAppCompatActivity> rule = new IntentsTestRule<>(BaseAppCompatActivity.class, false, false);
    private TeamMainFragment fragment;

    @BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
    }

    @Before
    public void setUp() throws Exception {

        rule.launchActivity(null);

        fragment = new TeamMainFragment();
        rule.getActivity().getSupportFragmentManager()
                .beginTransaction()
                .add(android.R.id.content, fragment)
                .commit();

        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

    }

    @Test
    public void optionInviteToTeam() throws Exception {


        final boolean[] clicked = {false};
        EventBus.getDefault().register(new Object() {
            public void onEvent(RequestInviteMemberEvent event) {
                clicked[0] = true;
            }
        });

        MenuItem mock = mock(MenuItem.class);
        doReturn(R.id.menu_team_add).when(mock).getItemId();
        fragment.onOptionsItemSelected(mock);

        assertThat(clicked[0]).isTrue();


    }

    @Test
    public void optionTeamInfo() throws Exception {
        MenuItem mock = mock(MenuItem.class);
        doReturn(R.id.menu_team_info).when(mock).getItemId();
        fragment.onOptionsItemSelected(mock);


        intending(hasComponent(TeamInfoActivity.class.getName()));

    }

    @Test
    public void optionTeamSearch() throws Exception {

        MenuItem mock = mock(MenuItem.class);
        doReturn(R.id.menu_team_search).when(mock).getItemId();
        fragment.onOptionsItemSelected(mock);

        intending(hasComponent(TeamMemberSearchActivity.class.getName()));
        intending(hasExtra("isSelectMode", false));

    }
}
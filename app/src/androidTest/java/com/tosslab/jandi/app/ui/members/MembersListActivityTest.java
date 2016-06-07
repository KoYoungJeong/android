package com.tosslab.jandi.app.ui.members;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.members.adapter.ModdableMemberListAdapter;
import com.tosslab.jandi.app.ui.members.presenter.MembersListPresenter;
import com.tosslab.jandi.app.ui.members.presenter.MembersListPresenterImpl_;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import setup.BaseInitUtil;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class MembersListActivityTest {

    @Rule
    public ActivityTestRule<MembersListActivity_> rule = new ActivityTestRule<>(MembersListActivity_.class, false, false);
    private MembersListActivity activity;
    private MembersListPresenter membersListPresenter;

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.createDummyTopic();
        BaseInitUtil.inviteDummyMembers();
        Intent i = new Intent();
        i.putExtra("entityId", BaseInitUtil.tempTopicId);
        i.putExtra("type", MembersListActivity.TYPE_MEMBERS_LIST_TOPIC);
        rule.launchActivity(i);
        activity = rule.getActivity();
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        membersListPresenter = MembersListPresenterImpl_.getInstance_(activity);
        membersListPresenter.setView(activity);
    }

    @After
    public void tearDown() throws Exception {
        BaseInitUtil.deleteDummyTopic();
    }

    @Test
    public void testRemoveUser() throws Throwable {
        // Given
        long entityId = BaseInitUtil.getUserIdByEmail(BaseInitUtil.TEST2_EMAIL);

        // When
        rule.runOnUiThread(() -> activity.removeUser(entityId));

        // Then
        ModdableMemberListAdapter adapter = (ModdableMemberListAdapter) activity.memberListView.getAdapter();
        for (int idx = 0; idx < adapter.getItemCount(); idx++) {
            long entityId1 = adapter.getItem(idx).getEntityId();
            if (entityId1 == entityId) {
                fail("삭제 했는데 왜 있지?");
            }
        }

        String name = TeamInfoLoader.getInstance().getName(entityId);

        onView(withText(name))
                // 체크 메소드 다시 설정해야함.
                .check(doesNotExist());

    }

}
package com.tosslab.jandi.app.ui.members;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.ui.entities.chats.to.ChatChooseItem;
import com.tosslab.jandi.app.ui.members.adapter.MembersAdapter;
import com.tosslab.jandi.app.ui.members.presenter.MembersListPresenter;
import com.tosslab.jandi.app.ui.members.presenter.MembersListPresenterImpl_;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import setup.BaseInitUtil;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.fail;
import static org.hamcrest.Matchers.not;

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
        membersListPresenter = MembersListPresenterImpl_.getInstance_(activity);
        membersListPresenter.setView(activity);
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    }

    @After
    public void tearDown() throws Exception {
        BaseInitUtil.deleteDummyTopic();
    }

    @Test
    public void testRemoveUser() throws Throwable {
        // Given
        int entityId = BaseInitUtil.getUserIdByEmail(BaseInitUtil.TEST2_ID);

        // When
        rule.runOnUiThread(() -> activity.removeUser(entityId));

        // Then
        MembersAdapter adapter = (MembersAdapter) activity.memberListView.getAdapter();
        for (int idx = 0; idx < adapter.getItemCount(); idx++) {
            int entityId1 = adapter.getItem(idx).getEntityId();
            if (entityId1 == entityId) {
                fail("삭제 했는데 왜 있지?");
            }
        }

        FormattedEntity entity = EntityManager.getInstance().getEntityById(entityId);

        onView(withText(entity.getName()))
                // 체크 메소드 다시 설정해야함.
                .check(matches(not(isDisplayed())));

    }

    @Test
    public void testShowKickFromTopicDialog() {
        ChatChooseItem item = getTempItem();

        rule.getActivity().showKickFromTopicDialog(item);

        onView(withText(R.string.jandi_confirm))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

    }

    @NonNull
    private ChatChooseItem getTempItem() {
        int entityId = BaseInitUtil.getUserIdByEmail(BaseInitUtil.TEST2_ID);

        ChatChooseItem item = new ChatChooseItem();
        item.name("aaa");
        item.photoUrl("");
        item.entityId(entityId);
        return item;
    }

}
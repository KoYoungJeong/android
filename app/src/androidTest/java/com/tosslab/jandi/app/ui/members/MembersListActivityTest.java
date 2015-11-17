package com.tosslab.jandi.app.ui.members;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;

import com.tosslab.jandi.app.ui.members.presenter.MembersListPresenter;
import com.tosslab.jandi.app.ui.members.presenter.MembersListPresenterImpl_;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import setup.BaseInitUtil;

/**
 * Created by tee on 15. 11. 13..
 */
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

    @Test
    public void kickMemberToast() {
        int id = BaseInitUtil.tempTopicId;
        int userId = BaseInitUtil.getUserIdByEmail(BaseInitUtil.TEST3_ID);
        membersListPresenter.onKickUser(id, userId);

    }

    @After
    public void finish() throws Exception {
        BaseInitUtil.deleteDummyTopic();
    }

}
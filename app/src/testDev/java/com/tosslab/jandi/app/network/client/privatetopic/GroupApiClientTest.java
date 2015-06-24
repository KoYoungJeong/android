package com.tosslab.jandi.app.network.client.privatetopic;

import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqCreateTopic;
import com.tosslab.jandi.app.network.models.ReqTeam;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.shadows.ShadowLog;
import org.springframework.web.client.HttpStatusCodeException;

import java.sql.Timestamp;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(RobolectricGradleTestRunner.class)
public class GroupApiClientTest {

    private ResLeftSideMenu sideMenu;

    @Before
    public void setUp() throws Exception {
        sideMenu = getSideMenu();
        Robolectric.getFakeHttpLayer().interceptHttpRequests(false);
        System.setProperty("robolectric.logging", "stdout");
        ShadowLog.stream = System.out;
    }

    private ResLeftSideMenu getSideMenu() {
        ResLeftSideMenu infosForSideMenu = RequestApiManager.getInstance().getInfosForSideMenuByMainRest(279);

        return infosForSideMenu;
    }

    private ResLeftSideMenu.PrivateGroup getMyPrivateTopic() {
        ResLeftSideMenu.PrivateGroup entity = null;
        for (ResLeftSideMenu.Entity entity1 : sideMenu.joinEntities) {
            if (entity1 instanceof ResLeftSideMenu.PrivateGroup) {
                ResLeftSideMenu.PrivateGroup channel = (ResLeftSideMenu.PrivateGroup) entity1;
                if (channel.pg_creatorId == sideMenu.user.id) {
                    entity = channel;
                    break;
                }
            }
        }
        return entity;
    }

    private ResLeftSideMenu.PrivateGroup getOtherPrivateTopic() {
        ResLeftSideMenu.PrivateGroup entity = null;
        for (ResLeftSideMenu.Entity entity1 : sideMenu.joinEntities) {
            if (entity1 instanceof ResLeftSideMenu.PrivateGroup) {
                ResLeftSideMenu.PrivateGroup channel = (ResLeftSideMenu.PrivateGroup) entity1;
                if (channel.pg_creatorId != sideMenu.user.id) {
                    entity = channel;
                    break;
                }
            }
        }
        return entity;
    }

    @Test
    public void testCreatePrivateGroup() throws Exception {

        ReqCreateTopic reqCreateTopic = new ReqCreateTopic();
        reqCreateTopic.teamId = sideMenu.team.id;
        reqCreateTopic.name = "test_" + new Timestamp(System.currentTimeMillis());
        ResCommon privateGroup = RequestApiManager.getInstance().createPrivateGroupByGroupApi(reqCreateTopic);

        assertThat(privateGroup, is(notNullValue()));

    }

    @Test
    public void testModifyGroup() throws Exception {

        sideMenu = getSideMenu();
        ResLeftSideMenu.PrivateGroup privateTopic = getMyPrivateTopic();

        ReqCreateTopic reqCreateTopic = new ReqCreateTopic();
        reqCreateTopic.teamId = sideMenu.team.id;
        reqCreateTopic.name = "mod_" + new Timestamp(System.currentTimeMillis());

        ResCommon resCommon = null;
        try {
            resCommon = RequestApiManager.getInstance().modifyGroupByGroupApi(reqCreateTopic, privateTopic.id);
        } catch (HttpStatusCodeException e) {
            fail(e.getResponseBodyAsString());
        }

        assertThat(resCommon, is(notNullValue()));

    }

    @Ignore
    @Test
    public void testLeaveGroup() throws Exception {
        ResLeftSideMenu.PrivateGroup otherPrivateTopic = getOtherPrivateTopic();
        ResCommon resCommon = null;
        try {
            resCommon = RequestApiManager.getInstance().leaveGroupByGroupApi(otherPrivateTopic.id, new ReqTeam(sideMenu.team.id));
        } catch (HttpStatusCodeException e) {
            fail(e.getResponseBodyAsString());
        }

        assertThat(resCommon, is(notNullValue()));
    }

    @Test
    public void testDeleteGroup() throws Exception {

        ResLeftSideMenu.PrivateGroup myPrivateTopic = getMyPrivateTopic();

        ResCommon resCommon = null;
        try {
            resCommon = RequestApiManager.getInstance().deleteGroupByGroupApi(sideMenu.team.id, myPrivateTopic.id);
        } catch (HttpStatusCodeException e) {
            fail(e.getResponseBodyAsString());
        }

        assertThat(resCommon, is(notNullValue()));

    }

    @Test
    public void testInviteGroup() throws Exception {

    }
}
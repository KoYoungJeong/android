package com.tosslab.jandi.app.network.client.privatetopic;

import com.tosslab.jandi.app.network.client.JandiRestClient;
import com.tosslab.jandi.app.network.client.JandiRestClient_;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ReqCreateTopic;
import com.tosslab.jandi.app.network.models.ReqTeam;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.spring.JandiV2HttpAuthentication;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
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

    private JandiRestClient jandiRestClient_;
    private GroupApiClient groupApiClient;
    private ResLeftSideMenu sideMenu;

    @Before
    public void setUp() throws Exception {

        jandiRestClient_ = new JandiRestClient_(Robolectric.application);
        groupApiClient = new GroupApiClient_(Robolectric.application);
        ResAccessToken accessToken = getAccessToken();

        jandiRestClient_.setAuthentication(new JandiV2HttpAuthentication(accessToken.getTokenType(), accessToken.getAccessToken()));
        groupApiClient.setAuthentication(new JandiV2HttpAuthentication(accessToken.getTokenType(), accessToken.getAccessToken()));

        sideMenu = getSideMenu();

        Robolectric.getFakeHttpLayer().interceptHttpRequests(false);

        System.setProperty("robolectric.logging", "stdout");
        ShadowLog.stream = System.out;

    }

    private ResLeftSideMenu getSideMenu() {
        ResLeftSideMenu infosForSideMenu = jandiRestClient_.getInfosForSideMenu(279);

        return infosForSideMenu;
    }

    private ResAccessToken getAccessToken() {

        jandiRestClient_.setHeader("Content-Type", "application/json");

        ResAccessToken accessToken = jandiRestClient_.getAccessToken(ReqAccessToken.createPasswordReqToken(BaseInitUtil.TEST_ID, BaseInitUtil.TEST_PASSWORD));
        System.out.println("========= Get Access Token =========");
        return accessToken;
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
        ResCommon privateGroup = groupApiClient.createPrivateGroup(reqCreateTopic);

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
            resCommon = groupApiClient.modifyGroup(reqCreateTopic, privateTopic.id);
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
            resCommon = groupApiClient.leaveGroup(otherPrivateTopic.id, new ReqTeam(sideMenu.team.id));
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
            resCommon = groupApiClient.deleteGroup(sideMenu.team.id, myPrivateTopic.id);
        } catch (HttpStatusCodeException e) {
            fail(e.getResponseBodyAsString());
        }

        assertThat(resCommon, is(notNullValue()));

    }

    @Test
    public void testInviteGroup() throws Exception {

    }
}
package com.tosslab.jandi.app.network.client.privatetopic;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqCreateTopic;
import com.tosslab.jandi.app.network.models.ReqTeam;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.shadows.ShadowLog;

import java.sql.Timestamp;

import retrofit.RetrofitError;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(RobolectricGradleTestRunner.class)
public class GroupApiClientTest {

    private ResLeftSideMenu sideMenu;

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData(Robolectric.application);
        int teamId = AccountRepository.getRepository().getAccountTeams().get(0).getTeamId();
        AccountRepository.getRepository().updateSelectedTeamInfo(teamId);
        sideMenu = getSideMenu();
        Robolectric.getFakeHttpLayer().interceptHttpRequests(false);
        System.setProperty("robolectric.logging", "stdout");
        ShadowLog.stream = System.out;
    }

    private ResLeftSideMenu getSideMenu() {
        int teamId = AccountRepository.getRepository().getSelectedTeamInfo().getTeamId();
        ResLeftSideMenu infosForSideMenu = RequestApiManager.getInstance()
                .getInfosForSideMenuByMainRest(teamId);

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
    public void testModifyGroup_이름변경() throws Exception {

        sideMenu = getSideMenu();
        ResLeftSideMenu.PrivateGroup privateTopic = getMyPrivateTopic();

        String oldDescription = privateTopic.description;

        ReqCreateTopic reqCreateTopic = new ReqCreateTopic();
        reqCreateTopic.teamId = sideMenu.team.id;
        reqCreateTopic.name = "mod_" + new Timestamp(System.currentTimeMillis());

        try {
            ResCommon resCommon = RequestApiManager.getInstance().modifyGroupByGroupApi(reqCreateTopic, privateTopic.id);
            assertThat(resCommon, is(notNullValue()));
        } catch (RetrofitError e) {
            fail(e.getResponse().getBody().toString());
        }

        ResLeftSideMenu infosForSideMenu = RequestApiManager.getInstance().getInfosForSideMenuByMainRest(279);
        EntityManager.getInstance(Robolectric.application).refreshEntity(infosForSideMenu);

        FormattedEntity entity = EntityManager.getInstance(Robolectric.application).getEntityById(privateTopic.id);
        assertThat(entity.getName(), is(reqCreateTopic.name));
        assertThat(((ResLeftSideMenu.PrivateGroup) entity.getEntity()).description, is(oldDescription));


    }

    @Test
    public void testModifyGroup_소개변경() throws Exception {

        sideMenu = getSideMenu();
        ResLeftSideMenu.PrivateGroup privateTopic = getMyPrivateTopic();

        String oldName = privateTopic.name;

        ReqCreateTopic reqCreateTopic = new ReqCreateTopic();
        reqCreateTopic.teamId = sideMenu.team.id;
        reqCreateTopic.description = privateTopic.description
                + "_mod_"
                + new Timestamp(System.currentTimeMillis());

        ResCommon resCommon = null;
        try {
            resCommon = RequestApiManager.getInstance().modifyGroupByGroupApi(reqCreateTopic, privateTopic.id);
            assertThat(resCommon, is(notNullValue()));
        } catch (RetrofitError e) {
            fail(e.getResponse().getBody().toString());
        }

        ResLeftSideMenu infosForSideMenu = RequestApiManager.getInstance().getInfosForSideMenuByMainRest(279);
        EntityManager.getInstance(Robolectric.application).refreshEntity(infosForSideMenu);

        FormattedEntity entity = EntityManager.getInstance(Robolectric.application).getEntityById(privateTopic.id);
        assertThat(entity.getName(), is(oldName));
        assertThat(((ResLeftSideMenu.PrivateGroup) entity.getEntity()).description,
                is(reqCreateTopic.description));


    }

    @Ignore
    @Test
    public void testLeaveGroup() throws Exception {
        ResLeftSideMenu.PrivateGroup otherPrivateTopic = getOtherPrivateTopic();
        ResCommon resCommon = null;
        try {
            resCommon = RequestApiManager.getInstance().leaveGroupByGroupApi(otherPrivateTopic.id, new ReqTeam(sideMenu.team.id));
        } catch (RetrofitError e) {
            fail(e.getResponse().getBody().toString());
        }

        assertThat(resCommon, is(notNullValue()));
    }

    @Test
    public void testDeleteGroup() throws Exception {

        ResLeftSideMenu.PrivateGroup myPrivateTopic = getMyPrivateTopic();

        ResCommon resCommon = null;
        try {
            resCommon = RequestApiManager.getInstance().deleteGroupByGroupApi(sideMenu.team.id, myPrivateTopic.id);
        } catch (RetrofitError e) {
            fail(e.getResponse().getBody().toString());
        }

        assertThat(resCommon, is(notNullValue()));

    }

    @Test
    public void testInviteGroup() throws Exception {

    }
}
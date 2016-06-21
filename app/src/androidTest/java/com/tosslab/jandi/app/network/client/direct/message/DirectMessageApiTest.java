package com.tosslab.jandi.app.network.client.direct.message;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqModifyMessage;
import com.tosslab.jandi.app.network.models.ReqSendMessageV3;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.functions.Func0;
import setup.BaseInitUtil;

import static org.assertj.core.api.Assertions.assertThat;

// 서버에서 응답이 정상적이지 않을때가 있음
@Ignore
@RunWith(AndroidJUnit4.class)
public class DirectMessageApiTest {

    private static long entityId;
    private static long teamId;
    private static long linkId;
    private DirectMessageApi api;

    @BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
        teamId = TeamInfoLoader.getInstance().getTeamId();
        List<User> formattedUsersWithoutMe = Observable.from(TeamInfoLoader.getInstance().getUserList())
                .filter(user -> TeamInfoLoader.getInstance().getMyId() != user.getId())
                .collect((Func0<ArrayList<User>>) ArrayList::new, ArrayList::add)
                .toBlocking()
                .first();
        entityId = formattedUsersWithoutMe.get((int) (Math.random() * formattedUsersWithoutMe.size())).getId();
        ResMessages directMessages = new DirectMessageApi(RetrofitBuilder.getInstance()).getDirectMessages(teamId, entityId, -1, 1);
        linkId = directMessages.records.get(0).id;
    }

    @Before
    public void setUp() throws Exception {
        api = new DirectMessageApi(RetrofitBuilder.getInstance());
    }

    @Test
    public void testGetDirectMessages_team_entity_linkid_count() throws Exception {
        int count = 10;
        ResMessages directMessages = api.getDirectMessages(teamId, entityId, -1, count);
        assertThat(directMessages).isNotNull();
        assertThat(directMessages.records.size())
                .isGreaterThanOrEqualTo(0)
                .isLessThanOrEqualTo(count);
    }

    @Test
    public void testGetDirectMessagesUpdatedForMarker_team_entity_link() throws Exception {
        long id = linkId;
        ResMessages directMessagesUpdatedForMarker = api.getDirectMessagesUpdatedForMarker(teamId, entityId, id);
        assertThat(directMessagesUpdatedForMarker).isNotNull();
        assertThat(directMessagesUpdatedForMarker.records.size()).isGreaterThanOrEqualTo(0);
    }

    @Test
    public void testGetDirectMessagesUpdatedForMarker_team_entity_link_count() throws Exception {
        long id = linkId;
        int count = 10;
        ResMessages resMessages = api.getDirectMessagesUpdatedForMarker(teamId, entityId, id, count);
        assertThat(resMessages).isNotNull();
        assertThat(resMessages.records.size())
                .isGreaterThanOrEqualTo(0)
                .isLessThanOrEqualTo(count);
    }

    @Test
    public void testGetDirectMarkerMessages() throws Exception {
        ResMessages resMessages = api.getDirectMarkerMessages(teamId, entityId, linkId);
        assertThat(resMessages).isNotNull();
        assertThat(resMessages.records.size()).isGreaterThanOrEqualTo(0);
    }

    @Test
    public void testSendDirectMessage() throws Exception {
        ResCommon resCommon = api.sendDirectMessage(entityId, teamId, new ReqSendMessageV3("testSendDirectMessage : " + new Date().toString(), new ArrayList<MentionObject>()));
        assertThat(resCommon).isNotNull();
        assertThat(resCommon.id).isGreaterThan(0);
    }

    @Ignore
    @Test
    public void testModifyDirectMessage() throws Exception {
        ResCommon resCommon = api.sendDirectMessage(entityId, teamId, new ReqSendMessageV3("testModifyDirectMessage : " + new Date().toString(), new ArrayList<MentionObject>()));
        ReqModifyMessage message = new ReqModifyMessage();
        message.content = "testModifyDirectMessage2 : " + new Date().toString();
        message.teamId = teamId;
        ResCommon resCommon1 = api.modifyDirectMessage(message, entityId, resCommon.id);
        assertThat(resCommon1).isNotNull();
    }

    @Test
    public void testDeleteDirectMessage() throws Exception {
        ResCommon resCommon = api.sendDirectMessage(entityId, teamId, new ReqSendMessageV3("testDeleteDirectMessage : " + new Date().toString(), new ArrayList<MentionObject>()));
        ResCommon resCommon1 = api.deleteDirectMessage(teamId, entityId, resCommon.id);
        assertThat(resCommon1).isNotNull();
    }
}
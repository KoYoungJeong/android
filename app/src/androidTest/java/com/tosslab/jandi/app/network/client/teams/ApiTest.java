package com.tosslab.jandi.app.network.client.teams;

import com.tosslab.jandi.app.network.client.rooms.RoomsApi;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.InnerApiRetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqOwner;
import com.tosslab.jandi.app.network.models.ResCommon;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Created by tonyjs on 16. 1. 11..
 */
public class ApiTest {

    @Ignore
    @Test
    public void testAssignToTopicOwner() throws Exception {
        int teamId = 279;
        int topicId = 11365808;

        int memberId = 11169482;

        ResCommon resCommon =
                new RoomsApi(InnerApiRetrofitBuilder.getInstance()).assignToTopicOwner(teamId, topicId, new ReqOwner(memberId));

        assertNotNull(resCommon);
    }
}
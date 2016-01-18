package com.tosslab.jandi.app.network.client.teams;

import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqOwner;
import com.tosslab.jandi.app.network.models.ResCommon;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Created by tonyjs on 16. 1. 11..
 */
public class TeamApiV2ClientTest {

    @Test
    public void testAssignToTopicOwner() throws Exception {
        int teamId = 279;
        int topicId = 11365808;

        int memberId = 11169482;

        ResCommon resCommon =
                RequestApiManager.getInstance().assignToTopicOwner(teamId, topicId, new ReqOwner(memberId));

        assertNotNull(resCommon);
    }
}
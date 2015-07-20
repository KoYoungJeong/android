package com.tosslab.jandi.app.network.models;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
public class ResMessagesTest {

    private static final String JSON_EVENT = "{\n" +
            "  \"lastLinkId\": 98691,\n" +
            "  \"numOfPage\": 20,\n" +
            "  \"firstIdOfReceivedList\": -1,\n" +
            "  \"isFirst\": true,\n" +
            "  \"messageCount\": 14,\n" +
            "  \"records\": [\n" +
            "    {\n" +
            "      \"id\": 32243,\n" +
            "      \"teamId\": 279,\n" +
            "      \"fromEntity\": 1," +
            "      \"time\": 1418199518975,\n" +
            "      \"messageId\": -1,\n" +
            "      \"status\": \"event\",\n" +
            "      \"feedbackId\": -1,\n" +
            "      \"info\": {\n" +
            "        \"eventType\": \"join\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": 26600,\n" +
            "      \"teamId\": 279,\n" +
            "      \"time\": 1417400000102,\n" +
            "      \"messageId\": -1,\n" +
            "      \"status\": \"event\",\n" +
            "      \"feedbackId\": -1,\n" +
            "      \"info\": {\n" +
            "        \"eventType\": \"join\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": 20981,\n" +
            "      \"teamId\": 279,\n" +
            "      \"time\": 1416209432541,\n" +
            "      \"messageId\": -1,\n" +
            "      \"status\": \"event\",\n" +
            "      \"feedbackId\": -1,\n" +
            "      \"info\": {\n" +
            "        \"invitorId\": 282,\n" +
            "        \"inviteUsers\": [\n" +
            "          324,\n" +
            "          291,\n" +
            "          284,\n" +
            "          296,\n" +
            "          289,\n" +
            "          285\n" +
            "        ],\n" +
            "        \"eventType\": \"invite\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": 20980,\n" +
            "      \"teamId\": 279,\n" +
            "      \"time\": 1416209396756,\n" +
            "      \"messageId\": -1,\n" +
            "      \"status\": \"event\",\n" +
            "      \"feedbackId\": -1,\n" +
            "      \"info\": {\n" +
            "        \"entityType\": \"channel\",\n" +
            "        \"createInfo\": {\n" +
            "          \"ch_creatorId\": 282,\n" +
            "          \"ch_createTime\": 1416209396244,\n" +
            "          \"ch_members\": [\n" +
            "            282\n" +
            "          ],\n" +
            "          \"ch_isDefault\": false\n" +
            "        },\n" +
            "        \"eventType\": \"create\"\n" +
            "      }\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    @Test
    public void testInitObject() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        ResMessages resMessages = objectMapper.readValue(JSON_EVENT, ResMessages.class);

        assertThat(resMessages, is(notNullValue()));

        assertThat(resMessages.records.get(0).info.getClass().getName()
                , is(ResMessages.JoinEvent.class.getName()));
        assertThat(resMessages.records.get(1).info.getClass().getName()
                , is(ResMessages.JoinEvent.class.getName()));
        assertThat(resMessages.records.get(2).info.getClass().getName()
                , is(ResMessages.InviteEvent.class.getName()));
        assertThat(resMessages.records.get(3).info.getClass().getName()
                , is(ResMessages.CreateEvent.class.getName()));


    }
}
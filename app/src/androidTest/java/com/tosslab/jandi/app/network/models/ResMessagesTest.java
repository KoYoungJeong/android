package com.tosslab.jandi.app.network.models;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;

import static org.hamcrest.CoreMatchers.is;
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
            "  \"messages\": [\n" +
            "    {\n" +
            "      \"id\": 32243,\n" +
            "      \"teamId\": 279,\n" +
            "      \"fromEntity\": {\n" +
            "        \"id\": 5312,\n" +
            "        \"type\": \"user\",\n" +
            "        \"name\": \"YJ\",\n" +
            "        \"u_photoUrl\": \"uploads/photo/77bf380f7eff2fc9ed997d341765e47e\",\n" +
            "        \"u_photoThumbnailUrl\": {\n" +
            "          \"smallThumbnailUrl\": \"uploads/photo/ths_77bf380f7eff2fc9ed997d341765e47e\",\n" +
            "          \"mediumThumbnailUrl\": \"uploads/photo/77bf380f7eff2fc9ed997d341765e47e\",\n" +
            "          \"largeThumbnailUrl\": \"uploads/photo/77bf380f7eff2fc9ed997d341765e47e\"\n" +
            "        },\n" +
            "        \"u_statusMessage\": \"(선택 사항)\"\n" +
            "      },\n" +
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
            "      \"fromEntity\": {\n" +
            "        \"id\": 6098,\n" +
            "        \"type\": \"user\",\n" +
            "        \"name\": \"Steve Jung\",\n" +
            "        \"u_photoUrl\": \"uploads/photo/f1d0709dc3276368967fa8fea355a41b\",\n" +
            "        \"u_photoThumbnailUrl\": {\n" +
            "          \"smallThumbnailUrl\": \"uploads/photo/ths_f1d0709dc3276368967fa8fea355a41b\",\n" +
            "          \"mediumThumbnailUrl\": \"uploads/photo/f1d0709dc3276368967fa8fea355a41b\",\n" +
            "          \"largeThumbnailUrl\": \"uploads/photo/f1d0709dc3276368967fa8fea355a41b\"\n" +
            "        },\n" +
            "        \"u_statusMessage\": \"개발은 근성!!!\"\n" +
            "      },\n" +
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
            "      \"fromEntity\": {\n" +
            "        \"id\": 282,\n" +
            "        \"type\": \"user\",\n" +
            "        \"name\": \"John Kang\",\n" +
            "        \"u_photoUrl\": \"uploads/photo/f417b33a1795eaedb8551205e28eecf0\",\n" +
            "        \"u_photoThumbnailUrl\": {\n" +
            "          \"smallThumbnailUrl\": \"uploads/photo/ths_f417b33a1795eaedb8551205e28eecf0\",\n" +
            "          \"mediumThumbnailUrl\": \"uploads/photo/f417b33a1795eaedb8551205e28eecf0\",\n" +
            "          \"largeThumbnailUrl\": \"uploads/photo/f417b33a1795eaedb8551205e28eecf0\"\n" +
            "        },\n" +
            "        \"u_firstName\": \"준수\",\n" +
            "        \"u_lastName\": \"강\",\n" +
            "        \"u_nickname\": \"John\",\n" +
            "        \"u_statusMessage\": \"John Kang\"\n" +
            "      },\n" +
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
            "      \"fromEntity\": {\n" +
            "        \"id\": 282,\n" +
            "        \"type\": \"user\",\n" +
            "        \"name\": \"John Kang\",\n" +
            "        \"u_photoUrl\": \"uploads/photo/f417b33a1795eaedb8551205e28eecf0\",\n" +
            "        \"u_photoThumbnailUrl\": {\n" +
            "          \"smallThumbnailUrl\": \"uploads/photo/ths_f417b33a1795eaedb8551205e28eecf0\",\n" +
            "          \"mediumThumbnailUrl\": \"uploads/photo/f417b33a1795eaedb8551205e28eecf0\",\n" +
            "          \"largeThumbnailUrl\": \"uploads/photo/f417b33a1795eaedb8551205e28eecf0\"\n" +
            "        },\n" +
            "        \"u_firstName\": \"준수\",\n" +
            "        \"u_lastName\": \"강\",\n" +
            "        \"u_nickname\": \"John\",\n" +
            "        \"u_statusMessage\": \"John Kang\"\n" +
            "      },\n" +
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

        for (ResMessages.Link message : resMessages.messages) {
            String s = objectMapper.writeValueAsString(message.info);
            ResMessages.EventInfo eventInfo = objectMapper.readValue(s, ResMessages.EventInfo.class);
            assertThat(eventInfo, is(notNullValue()));

        }

    }
}
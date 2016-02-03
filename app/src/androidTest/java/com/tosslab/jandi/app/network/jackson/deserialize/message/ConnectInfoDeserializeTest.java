package com.tosslab.jandi.app.network.jackson.deserialize.message;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.network.json.JacksonMapper;
import com.tosslab.jandi.app.network.models.ResMessages;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class ConnectInfoDeserializeTest {
    private static final String TEST_MESSAGE = "{\n" +
            "  \"id\": 16318926,\n" +
            "  \"fromEntity\": 11552060,\n" +
            "  \"teamId\": 279,\n" +
            "  \"info\": {\n" +
            "    \"inviteUsers\": [],\n" +
            "    \"createInfo\": {\n" +
            "      \"pg_members\": [],\n" +
            "      \"ch_members\": []\n" +
            "    }\n" +
            "  },\n" +
            "  \"feedbackId\": -1,\n" +
            "  \"status\": \"created\",\n" +
            "  \"messageId\": 15531174,\n" +
            "  \"time\": 1454400246509,\n" +
            "  \"toEntity\": [\n" +
            "    11552059\n" +
            "  ],\n" +
            "  \"message\": {\n" +
            "    \"id\": 15531174,\n" +
            "    \"teamId\": 279,\n" +
            "    \"writerId\": 11552060,\n" +
            "    \"contentType\": \"text\",\n" +
            "    \"permission\": 740,\n" +
            "    \"updatedAt\": \"2016-02-02T08:04:06.502Z\",\n" +
            "    \"createdAt\": \"2016-02-02T08:04:06.489Z\",\n" +
            "    \"mentions\": [],\n" +
            "    \"info\": {\n" +
            "      \"mention\": []\n" +
            "    },\n" +
            "    \"commentCount\": 0,\n" +
            "    \"feedbackId\": -1,\n" +
            "    \"shareEntities\": [\n" +
            "      11552059\n" +
            "    ],\n" +
            "    \"status\": \"created\",\n" +
            "    \"linkPreviewId\": null,\n" +
            "    \"content\": {\n" +
            "      \"body\": \"[[대신증권-무료-자유게시판]](http://newsystock.com/Admin/SecuritiesBoard.aspx) 회원 : 글쓴이\",\n" +
            "      \"connectType\": \"incoming\",\n" +
            "      \"connectColor\": \"#FAC11B\",\n" +
            "      \"connectInfo\": [\n" +
            "        {\n" +
            "          \"title\": \"글의 제목\",\n" +
            "          \"description\": null,\n" +
            "          \"imageUrl\": null\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    \"updateTime\": 1454400246502,\n" +
            "    \"createTime\": 1454400246488,\n" +
            "    \"deleterId\": null,\n" +
            "    \"isStarred\": false,\n" +
            "    \"linkPreview\": {}\n" +
            "  }\n" +
            "}";

    @Test
    public void testParsing() throws Exception {
        ObjectMapper objectMapper = JacksonMapper.getInstance().getObjectMapper();
        ResMessages.Link textMessage = objectMapper.readValue(TEST_MESSAGE, ResMessages.Link.class);
        assertThat(textMessage, is(notNullValue()));

    }
}
package com.tosslab.jandi.app.services.socket.to;

import com.tosslab.jandi.app.network.json.JacksonMapper;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.JandiRobolectricGradleTestRunner;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(JandiRobolectricGradleTestRunner.class)
public class SocketMessageEventTest {

    public static final String TEXT_2 = "{\n" +
            "  \"event\": \"message\",\n" +
            "  \"messageType\": \"topic_leave\",\n" +
            "  \"room\": {\n" +
            "    \"id\": 19397,\n" +
            "    \"type\": \"channel\"\n" +
            "  },\n" +
            "  \"writer\": 6098\n" +
            "}";
    private static final String TEXT_1 = "{\n" +
            "  \"event\": \"message\",\n" +
            "  \"messageType\": \"file_comment\",\n" +
            "  \"file\": {\n" +
            "    \"id\": 104588\n" +
            "  },\n" +
            "  \"comment\": {\n" +
            "    \"id\": 104677\n" +
            "  },\n" +
            "  \"writer\": 285,\n" +
            "  \"rooms\": [\n" +
            "    {\n" +
            "      \"id\": 13679,\n" +
            "      \"type\": \"channel\",\n" +
            "      \"members\": [\n" +
            "        4312,\n" +
            "        643,\n" +
            "        783,\n" +
            "        1285\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": 20718,\n" +
            "      \"type\": \"chat\",\n" +
            "      \"members\": [\n" +
            "        282,\n" +
            "        285\n" +
            "      ]\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    @Test
    public void testParsing() throws Exception {
        ObjectMapper objectMapper = JacksonMapper.getInstance().getObjectMapper();

        SocketMessageEvent socketMessageEvent = objectMapper.readValue(TEXT_1, SocketMessageEvent.class);

        assertThat(socketMessageEvent, is(notNullValue()));

        socketMessageEvent = objectMapper.readValue(TEXT_2, SocketMessageEvent.class);

        assertThat(socketMessageEvent, is(notNullValue()));

    }

}
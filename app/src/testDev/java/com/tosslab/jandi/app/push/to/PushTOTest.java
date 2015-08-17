package com.tosslab.jandi.app.push.to;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.JandiRobolectricGradleTestRunner;

import java.io.IOException;

import static junit.framework.Assert.fail;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(JandiRobolectricGradleTestRunner.class)
public class PushTOTest {

    private static final String testObject = "{\n" +
            "  \"action\": \"com.tosslab.jandi.app.Push\",\n" +
            "  \"type\": \"push\",\n" +
            "  \"info\": {\n" +
            "    \"alert\": \"오늘 점심엔 뭐 먹으러 갈까요?\",\n" +
            "    \"contentType\": \"text\",\n" +
            "    \"chatId\": 287,\n" +
            "    \"chatName\": \"점심\",\n" +
            "    \"chatType\": \"channel\",\n" +
            "    \"writerId\": 6098,\n" +
            "    \"writerName\": \"Steve Jung\",\n" +
            "    \"writerThumb\": \"uploads/photo/0134465a171b1727568071c9f3108cdc\",\n" +
            "    \"teamId\": 279,\n" +
            "    \"teamName\": \"Toss Lab, Inc.\"\n" +
            "  }\n" +
            "}";

    @Test
    public void testParsing() throws Exception {


        try {
            PushTO pushTO = new ObjectMapper().readValue(testObject, PushTO.class);

            assertThat(pushTO, is(notNullValue()));
        } catch (IOException e) {
            fail(e.getMessage());
        }


    }
}
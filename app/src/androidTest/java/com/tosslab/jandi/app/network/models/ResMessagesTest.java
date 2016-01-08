package com.tosslab.jandi.app.network.models;

import android.support.test.runner.AndroidJUnit4;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;

@RunWith(AndroidJUnit4.class)
public class ResMessagesTest {

    private static final String CONNECTION_INFO_TEST = "[{\"event\":\"hello1\",\"title\":\"title1\",\"description\":\"desc1\"},{\"event\":\"hello2\",\"title\":null,\"description\":\"desc2\"},{\"event\":\"hello3\",\"description\":\"desc3\"}]";
    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws Exception {

        objectMapper = new ObjectMapper();

    }

    @Test
    public void testParsingConnectionInfo() throws Exception {
        ResMessages.ConnectInfo[] connectInfos = objectMapper.readValue(CONNECTION_INFO_TEST, ResMessages.ConnectInfo[].class);

        assertThat(connectInfos, is(notNullValue()));
        assertThat(connectInfos.length, is(3));
        assertThat(connectInfos[0].title, is("title1"));
        assertThat(connectInfos[1].title, is(equalTo("null")));
        assertThat(connectInfos[2].title, is(nullValue()));
    }
}
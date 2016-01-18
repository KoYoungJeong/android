package com.tosslab.jandi.app.ui.search.messages.model;

import com.tosslab.jandi.app.network.models.ResMessageSearch;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.JandiRobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;

@Config(manifest = "app/src/main/AndroidManifest.xml", sdk = 18)
@RunWith(JandiRobolectricGradleTestRunner.class)
public class MessageSearchModelTest {

    MessageSearchModel messageSearchModel;

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData(RuntimeEnvironment.application);
        messageSearchModel = MessageSearchModel_.getInstance_(RuntimeEnvironment.application);
    }

    @After
    public void tearDown() throws Exception {
        BaseInitUtil.releaseDatabase();

    }


    @Test
    public void testRequestSearchQuery() throws Exception {
        ResMessageSearch resMessageSearch = messageSearchModel.requestSearchQuery(279, "하", 1, 20, -1, -1);

        assertThat(resMessageSearch, is(notNullValue()));
    }
}
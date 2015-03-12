package com.tosslab.jandi.app.ui.search.messages.model;

import com.tosslab.jandi.app.network.models.ResMessageSearch;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;

@Config(manifest = "app/src/main/AndroidManifest.xml", emulateSdk = 18)
@RunWith(RobolectricGradleTestRunner.class)
public class MessageSearchModelTest {

    MessageSearchModel messageSearchModel;

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData(Robolectric.application);
        messageSearchModel = MessageSearchModel_.getInstance_(Robolectric.application);
    }

    @Test
    public void testRequestSearchQuery() throws Exception {
        ResMessageSearch resMessageSearch = messageSearchModel.requestSearchQuery(279, "하", 1, 20, -1, -1);

        assertThat(resMessageSearch, is(notNullValue()));
    }
}
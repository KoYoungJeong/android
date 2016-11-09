package com.tosslab.jandi.app.network.client.rooms;

import com.tosslab.jandi.app.OkHttpClientTestFactory;
import com.tosslab.jandi.app.ValidationUtil;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqCreateAnnouncement;
import com.tosslab.jandi.app.network.models.ReqUpdateAnnouncementStatus;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AnnounceApiDeprecatedTest {

    private AnnounceApi.Api api;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        OkHttpClientTestFactory.init();
    }

    @Before
    public void setUp() throws Exception {
        api = RetrofitBuilder.getInstance().create(AnnounceApi.Api.class);
    }

    @Test
    public void createAnnouncement() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.createAnnouncement(1, 1, new ReqCreateAnnouncement(1)).execute())).isFalse();
    }

    @Test
    public void updateAnnouncementStatus() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.updateAnnouncementStatus(1, 1, new ReqUpdateAnnouncementStatus(1, true)).execute())).isFalse();
    }

    @Test
    public void deleteAnnouncement() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.deleteAnnouncement(1, 1).execute())).isFalse();
    }


}
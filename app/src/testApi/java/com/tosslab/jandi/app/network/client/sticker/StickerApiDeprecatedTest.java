package com.tosslab.jandi.app.network.client.sticker;

import com.tosslab.jandi.app.OkHttpClientTestFactory;
import com.tosslab.jandi.app.ValidationUtil;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.sticker.ReqSendSticker;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

public class StickerApiDeprecatedTest {

    private StickerApi.Api api;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        OkHttpClientTestFactory.init();
    }

    @Before
    public void setUp() throws Exception {
        api = RetrofitBuilder.getInstance().create(StickerApi.Api.class);
    }

    @Test
    public void sendStickerComment() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.sendStickerComment(ReqSendSticker.create(1,"1",1,1,"","", new ArrayList<>())))).isFalse();
    }


}
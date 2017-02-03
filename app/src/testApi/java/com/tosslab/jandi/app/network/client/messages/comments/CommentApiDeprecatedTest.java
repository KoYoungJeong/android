package com.tosslab.jandi.app.network.client.messages.comments;

import com.tosslab.jandi.app.OkHttpClientTestFactory;
import com.tosslab.jandi.app.ValidationUtil;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqSendComment;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

public class CommentApiDeprecatedTest {

    private CommentApi.Api api;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        OkHttpClientTestFactory.init();
    }

    @Before
    public void setUp() throws Exception {
        api = RetrofitBuilder.getInstance().create(CommentApi.Api.class);
    }

    @Test
    public void sendMessageComment() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.sendMessageComment(1,1,new ReqSendComment("asd", new ArrayList<MentionObject>())))).isFalse();
    }

    @Test
    public void deleteMessageComment() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.deleteMessageComment(1,1,1))).isFalse();
    }


}
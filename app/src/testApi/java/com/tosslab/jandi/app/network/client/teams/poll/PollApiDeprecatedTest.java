package com.tosslab.jandi.app.network.client.teams.poll;

import com.tosslab.jandi.app.OkHttpClientTestFactory;
import com.tosslab.jandi.app.ValidationUtil;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqCreatePoll;
import com.tosslab.jandi.app.network.models.ReqSendPollComment;
import com.tosslab.jandi.app.network.models.ReqVotePoll;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class PollApiDeprecatedTest {

    private PollApi.Api api;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        OkHttpClientTestFactory.init();
    }

    @Before
    public void setUp() throws Exception {
        api = RetrofitBuilder.getInstance().create(PollApi.Api.class);
    }

    @Test
    public void getPollDetail() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.getPollDetail(1,1).execute())).isFalse();
    }

    @Test
    public void createPoll() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.createPoll(1, ReqCreatePoll.create(1, "", false, false, new Date(), Arrays.asList("asd","asd12"))).execute())).isFalse();
    }

    @Test
    public void deletePoll() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.deletePoll(1,1).execute())).isFalse();
    }

    @Test
    public void votePoll() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.vote(1,1, ReqVotePoll.create(Arrays.asList(1,2))).execute())).isFalse();
    }

    @Test
    public void getPollComments() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.getPollComments(1,1).execute())).isFalse();
    }

    @Test
    public void sendPollComment() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.sendPollComment(1,1, new ReqSendPollComment(1,"asd","asd", new ArrayList<MentionObject>())).execute())).isFalse();
    }

    @Test
    public void finishPoll() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.finishPoll(1,1).execute())).isFalse();
    }

    @Test
    public void getAllPollParticipants() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.getAllPollParticipants(1, 1).execute())).isFalse();
    }

    @Test
    public void getPollParticipants() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.getPollParticipants(1,1,1).execute())).isFalse();
    }

    @Test
    public void getPollList() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.getPollList(1,1).execute())).isFalse();
    }

    @Test
    public void getPollList1() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.getPollList(1,1, "asd").execute())).isFalse();
    }


}
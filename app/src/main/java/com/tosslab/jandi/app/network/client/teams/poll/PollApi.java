package com.tosslab.jandi.app.network.client.teams.poll;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqCreatePoll;
import com.tosslab.jandi.app.network.models.ReqSendComment;
import com.tosslab.jandi.app.network.models.ReqSendPollComment;
import com.tosslab.jandi.app.network.models.ReqVotePoll;
import com.tosslab.jandi.app.network.models.ResCreatePoll;
import com.tosslab.jandi.app.network.models.ResDeletePoll;
import com.tosslab.jandi.app.network.models.ResPollCommentCreated;
import com.tosslab.jandi.app.network.models.ResPollComments;
import com.tosslab.jandi.app.network.models.ResPollDetail;

import java.util.Collection;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Created by tonyjs on 16. 6. 14..
 */
public class PollApi extends ApiTemplate<PollApi.Api> {

    public PollApi(RetrofitBuilder retrofitBuilder) {
        super(Api.class, retrofitBuilder);
    }

    public ResPollDetail getPollDetail(long teamId, long pollId) throws RetrofitException {
        return call(() -> getApi().getPollDetail(teamId, pollId));
    }

    public ResCreatePoll createPoll(long teamId, ReqCreatePoll reqCreatePoll) throws RetrofitException {
        return call(() -> getApi().createPoll(teamId, reqCreatePoll));
    }

    public ResDeletePoll deletePoll(long teamId, long pollId) throws RetrofitException {
        return call(() -> getApi().deletePoll(teamId, pollId));
    }

    public ResPollDetail votePoll(long teamId, long pollId, ReqVotePoll reqVotePoll) throws RetrofitException {
        return call(() -> getApi().vote(teamId, pollId, reqVotePoll));
    }

    public ResPollComments getPollComments(long teamId, long pollId) throws RetrofitException {
        return call(() -> getApi().getPollComments(teamId, pollId));
    }

    public ResPollCommentCreated sendPollComment(long teamId, long pollId, ReqSendPollComment reqSendComment) throws RetrofitException {
        return call(() -> getApi().sendPollComment(teamId, pollId, reqSendComment));
    }

    interface Api {

        @GET("teams/{teamId}/polls/{pollId}")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResPollDetail> getPollDetail(@Path("teamId") long teamId, @Path("pollId") long pollId);

        @POST("teams/{teamId}/poll")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCreatePoll> createPoll(@Path("teamId") long teamId, @Body ReqCreatePoll reqCreatePoll);

        @DELETE("teams/{teamId}/polls/{pollId}")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResDeletePoll> deletePoll(@Path("teamId") long teamId, @Path("pollId") long pollId);

        @HTTP(path = "teams/{teamId}/polls/{pollId}/vote", hasBody = true, method = "PUT")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResPollDetail> vote(@Path("teamId") long teamId, @Path("pollId") long pollId,
                                 @Body ReqVotePoll reqVotePoll);

        @GET("teams/{teamId}/polls/{pollId}/comments")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResPollComments> getPollComments(@Path("teamId") long teamId, @Path("pollId") long pollId);

        @POST("teams/{teamId}/polls/{pollId}/comment")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResPollCommentCreated> sendPollComment(@Path("teamId") long teamId, @Path("pollId") long pollId,
                                                    @Body ReqSendPollComment reqSendComment);

    }

}

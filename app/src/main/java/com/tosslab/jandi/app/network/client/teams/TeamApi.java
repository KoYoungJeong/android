package com.tosslab.jandi.app.network.client.teams;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.InnerApiRetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqCreateNewTeam;
import com.tosslab.jandi.app.network.models.ReqInvitationMembers;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResInvitationMembers;
import com.tosslab.jandi.app.network.models.ResOnlineStatus;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.network.models.team.rank.Ranks;

import java.util.List;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public class TeamApi extends ApiTemplate<TeamApi.Api> {
    @Inject
    public TeamApi(InnerApiRetrofitBuilder retrofitBuilder) {
        super(Api.class, retrofitBuilder);
    }

    public ResTeamDetailInfo createNewTeam(ReqCreateNewTeam req) throws RetrofitException {
        return call(() -> getApi().createNewTeam(req));
    }

    public List<ResInvitationMembers> inviteToTeam(long teamId, ReqInvitationMembers invitationMembers)
            throws RetrofitException {
        return call(() -> getApi().inviteToTeam(teamId, invitationMembers));
    }

    public ResTeamDetailInfo.InviteTeam getTeamInfo(long teamId) throws RetrofitException {
        return call(() -> getApi().getTeamInfo(teamId));
    }

    public ResCommon cancelInviteTeam(long teamId, long memberId) throws RetrofitException {
        return call(() -> getApi().cancelInviteTeam(teamId, memberId));
    }

    public Ranks getRanks(long teamId) throws RetrofitException {
        return call(() -> getApi().getRanks(teamId));
    }

    public ResOnlineStatus getOnlineStatus(long teamId) throws RetrofitException {
        return call(() -> getApi().getOnlineStatus(teamId));
    }

    interface Api {
        @POST("teams")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
        Call<ResTeamDetailInfo> createNewTeam(@Body ReqCreateNewTeam req);

        @POST("teams/{teamId}/invitations")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<List<ResInvitationMembers>> inviteToTeam(@Path("teamId") long teamId,
                                                      @Body ReqInvitationMembers invitationMembers);

        @DELETE("teams/{teamId}/members/{memberId}/invitation")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> cancelInviteTeam(@Path("teamId") long teamId, @Path("memberId") long memberId);

        @GET("teams/{teamId}")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResTeamDetailInfo.InviteTeam> getTeamInfo(@Path("teamId") long teamId);

        @GET("teams/{teamId}/ranks")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<Ranks> getRanks(@Path("teamId") long teamId);

        @GET("teams/{teamId}/presence")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResOnlineStatus> getOnlineStatus(@Path("teamId") long teamId);
    }

}

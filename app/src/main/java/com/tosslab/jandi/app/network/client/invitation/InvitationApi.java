package com.tosslab.jandi.app.network.client.invitation;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqInvitationAcceptOrIgnore;
import com.tosslab.jandi.app.network.models.ResPendingTeamInfo;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;

import java.util.List;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public class InvitationApi extends ApiTemplate<InvitationApi.Api> {
    @Inject
    public InvitationApi(RetrofitBuilder retrofitBuilder) {
        super(Api.class, retrofitBuilder);
    }

    public ResTeamDetailInfo acceptOrDeclineInvitation(String invitationId,
                                                ReqInvitationAcceptOrIgnore reqInvitationAcceptOrIgnore) throws RetrofitException {
        return call(() -> getApi().acceptOrDeclineInvitation(invitationId, reqInvitationAcceptOrIgnore));
    }

    public List<ResPendingTeamInfo> getPedingTeamInfo() throws RetrofitException {
        return call(() -> getApi().getPedingTeamInfo());
    }


    interface Api {

        @PUT("account/invitations/{invitationId}")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResTeamDetailInfo> acceptOrDeclineInvitation(@Path("invitationId") String invitationId,
                                                          @Body ReqInvitationAcceptOrIgnore reqInvitationAcceptOrIgnore);

        /**
         * 초대된 목록 가져오기
         */
        @GET("account/invitations")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
        Call<List<ResPendingTeamInfo>> getPedingTeamInfo();

    }
}

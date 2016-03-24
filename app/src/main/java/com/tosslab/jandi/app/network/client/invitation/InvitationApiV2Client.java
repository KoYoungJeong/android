package com.tosslab.jandi.app.network.client.invitation;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.manager.restapiclient.annotation.AuthorizedHeader;
import com.tosslab.jandi.app.network.models.ReqInvitationAcceptOrIgnore;
import com.tosslab.jandi.app.network.models.ResPendingTeamInfo;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;

import java.util.List;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Created by tee on 15. 6. 16..
 */
@AuthorizedHeader
public interface InvitationApiV2Client {

    @PUT("/account/invitations/{invitationId}")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResTeamDetailInfo acceptOrDeclineInvitation(@Path("invitationId") String invitationId,
                                                @Body ReqInvitationAcceptOrIgnore reqInvitationAcceptOrIgnore);

    /**
     * 초대된 목록 가져오기
     */
    @GET("/account/invitations")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
    List<ResPendingTeamInfo> getPedingTeamInfo();

}
package com.tosslab.jandi.app.network.client.invitation;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.manager.RestApiClient.annotation.AuthorizedHeader;
import com.tosslab.jandi.app.network.models.ReqInvitationAcceptOrIgnore;
import com.tosslab.jandi.app.network.models.ResPendingTeamInfo;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.network.spring.JandiV3HttpMessageConverter;

import org.androidannotations.annotations.rest.Accept;
import org.androidannotations.annotations.rest.Get;
import org.androidannotations.annotations.rest.Put;
import org.androidannotations.annotations.rest.RequiresAuthentication;

import java.util.List;

import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.PUT;
import retrofit.http.Path;

/**
 * Created by tee on 15. 6. 16..
 */
@AuthorizedHeader
public interface InvitationApiV2Client {

    @PUT("/account/invitations/{invitationId}")
    @Headers("Accept :"+ JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResTeamDetailInfo acceptOrDeclineInvitation(@Path("invitationId") String invitationId,
                                                @Body ReqInvitationAcceptOrIgnore reqInvitationAcceptOrIgnore);

    /**
     * 초대된 목록 가져오기
     */
    @GET("/account/invitations")
    @Headers("Accept :"+ JandiConstants.HTTP_ACCEPT_HEADER_V3)
    List<ResPendingTeamInfo> getPedingTeamInfo();

}
package com.tosslab.jandi.app.network.client.invitation;

import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.network.models.ReqInvitationAcceptOrIgnore;
import com.tosslab.jandi.app.network.models.ReqInvitationConfirm;
import com.tosslab.jandi.app.network.models.ResPendingTeamInfo;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.network.spring.JandiV2HttpMessageConverter;
import com.tosslab.jandi.app.network.spring.JandiV3HttpMessageConverter;
import com.tosslab.jandi.app.network.spring.LoggerInterceptor;

import org.androidannotations.annotations.rest.Accept;
import org.androidannotations.annotations.rest.Get;
import org.androidannotations.annotations.rest.Put;
import org.androidannotations.annotations.rest.RequiresAuthentication;
import org.androidannotations.annotations.rest.Rest;
import org.springframework.http.HttpAuthentication;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 14. 12. 12..
 */
@Rest(
        rootUrl = JandiConstantsForFlavors.SERVICE_ROOT_URL + "inner-api",
        converters = {
                JandiV2HttpMessageConverter.class,
                ByteArrayHttpMessageConverter.class,
                FormHttpMessageConverter.class,
                StringHttpMessageConverter.class},
        interceptors = {LoggerInterceptor.class}
)
@Accept(JandiV2HttpMessageConverter.APPLICATION_VERSION_FULL_NAME)
public interface InvitationApiClient {

    void setHeader(String name, String value);

    void setAuthentication(HttpAuthentication auth);


    /**
     * 초대 수락하기.
     */
    @Put("/invitations")
    @RequiresAuthentication
    ResTeamDetailInfo confirmInvitation(ReqInvitationConfirm reqInvitationConfirm);

    /**
     * 초대 거절하기
     */
    @Put("/invitations")
    @RequiresAuthentication
    List<ResPendingTeamInfo> declineInvitation(ReqInvitationConfirm reqInvitationConfirm);

    /**
     * 초대 수락 또는 거절
     */
    @Put("/account/invitations/{invitationId}")
    @RequiresAuthentication
    ResTeamDetailInfo confirmOrDeclineInvitation(String invitationId, ReqInvitationAcceptOrIgnore reqInvitationAcceptOrIgnore);

    /**
     * 초대된 목록 가져오기
     */
    @Get("/account/invitations")
    @RequiresAuthentication
    @Accept(JandiV3HttpMessageConverter.APPLICATION_VERSION_FULL_NAME)
    List<ResPendingTeamInfo> getPedingTeamInfo();

}

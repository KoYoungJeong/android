package com.tosslab.jandi.app.network.client.account.emails;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ReqConfirmEmail;
import com.tosslab.jandi.app.network.models.ResAccountInfo;

import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.PUT;

/**
 * Created by tee on 15. 6. 16..
 */
public interface AccountEmailsApiV2Client {

    @POST("/account/emails")
    @Headers("Accept :"+ JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResAccountInfo requestAddEmail(@Body ReqAccountEmail reqAccountEmail);

    @PUT("/emails/confirm")
    @Headers("Accept :"+ JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResAccountInfo confirmEmail(@Body ReqConfirmEmail reqConfirmEmail);

    @DELETE("/account/emails")
    @Headers("Accept :"+ JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResAccountInfo deleteEmail(@Body ReqAccountEmail reqConfirmEmail);

}

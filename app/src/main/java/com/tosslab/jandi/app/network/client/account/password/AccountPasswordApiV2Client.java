package com.tosslab.jandi.app.network.client.account.password;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ReqChangePassword;
import com.tosslab.jandi.app.network.models.ResCommon;

import retrofit.http.Body;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.PUT;

/**
 * Created by tee on 15. 6. 16..
 */
public interface AccountPasswordApiV2Client {

    //TOKEN NOT NEDDED
    @POST("/accounts/password/resetToken")
    @Headers("Accept :"+ JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon resetPassword(@Body ReqAccountEmail reqAccountEmail);

    @PUT("/accounts/password")
    @Headers("Accept :"+ JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon changePassword(@Body ReqChangePassword reqConfirmEmail);

}

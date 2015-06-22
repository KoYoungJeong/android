package com.tosslab.jandi.app.network.client.settings;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ReqProfileName;
import com.tosslab.jandi.app.network.models.ResAccountInfo;

import retrofit.http.Body;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.PUT;

/**
 * Created by tee on 15. 6. 16..
 */
public interface AccountProfileV2Client {

    @POST("/settings/name")
    @Headers("Accept :" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResAccountInfo changeName(@Body ReqProfileName reqProfileName);

    @PUT("/settings/primaryEmail")
    @Headers("Accept :" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResAccountInfo changePrimaryEmail(@Body ReqAccountEmail reqAccountEmail);

}

package com.tosslab.jandi.app.network.client.validation;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.models.validation.ResValidation;

import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.Path;

public interface ValidationApi {

    @GET("/validation/domain/{domain}")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResValidation validDomain(@Path("domain") String domain);

}

package com.tosslab.jandi.app.network.client.validation;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.models.validation.ResValidation;

import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;

public interface ValidationApi {

    @GET("/validation/domain/{domain}")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResValidation validDomain(@Path("domain") String domain);

}

package com.tosslab.jandi.app.network.client.validation;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.InnerApiRetrofitBuilder;
import com.tosslab.jandi.app.network.models.validation.EmailTypo;
import com.tosslab.jandi.app.network.models.validation.ResValidation;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;

public class ValidationApi extends ApiTemplate<ValidationApi.Api> {
    @Inject
    public ValidationApi(InnerApiRetrofitBuilder retrofitBuilder) {
        super(Api.class, retrofitBuilder);
    }

    public ResValidation validDomain(String domain) throws RetrofitException {
        return call(() -> getApi().validDomain(domain));
    }

    public EmailTypo getEmailTypo() throws RetrofitException {
        return call(() -> getApi().getEmailTypo());
    }


    interface Api {

        @GET("validation/domain/{domain}")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResValidation> validDomain(@Path("domain") String domain);

        @GET("emails/typo")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<EmailTypo> getEmailTypo();
    }
}

package com.tosslab.jandi.app.ui.signup.model;

import android.content.Context;

import com.tosslab.jandi.app.network.client.JandiRestClient;
import com.tosslab.jandi.app.network.client.JandiRestClient_;
import com.tosslab.jandi.app.network.client.JandiRestV2Client;
import com.tosslab.jandi.app.network.manager.Request;
import com.tosslab.jandi.app.network.models.ReqSignUpInfo;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.ui.intro.model.JacksonConverter;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.TokenUtil;

import org.codehaus.jackson.map.ObjectMapper;

import retrofit.RestAdapter;

/**
 * Created by Steve SeongUg Jung on 14. 12. 24..
 */
public class SignUpRequest implements Request<ResAccountInfo> {

    private final Context context;
    private final ReqSignUpInfo reqSignUpInfo;
    RestAdapter restAdapter;

    private SignUpRequest(Context context, ReqSignUpInfo reqSignUpInfo) {
        this.context = context;
        this.reqSignUpInfo = reqSignUpInfo;

        JacksonConverter converter = new JacksonConverter(new ObjectMapper());

        restAdapter = new RestAdapter.Builder()
                .setRequestInterceptor(request -> {
                    request.addHeader("Authorization", TokenUtil.getRequestAuthentication().getHeaderValue());
                })
                .setConverter(converter)
                .setEndpoint("http://i2.jandi.io:8888/inner-api")
                .build();
    }

    public static SignUpRequest create(Context context, ReqSignUpInfo reqSignUpInfo) {
        return new SignUpRequest(context, reqSignUpInfo);
    }

    @Override
    public ResAccountInfo request() throws JandiNetworkException {
//        JandiRestClient jandiRestClient = new JandiRestClient_(context);
//        return jandiRestClient.signUpAccount(reqSignUpInfo);
          return restAdapter.create(JandiRestV2Client.class).signUpAccount(reqSignUpInfo);
    }
}

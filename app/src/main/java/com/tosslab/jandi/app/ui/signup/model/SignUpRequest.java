package com.tosslab.jandi.app.ui.signup.model;

import android.content.Context;

import com.tosslab.jandi.app.network.client.JandiRestClient;
import com.tosslab.jandi.app.network.client.JandiRestClient_;
import com.tosslab.jandi.app.network.manager.Request;
import com.tosslab.jandi.app.network.models.ReqSignUpInfo;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.utils.JandiNetworkException;

/**
 * Created by Steve SeongUg Jung on 14. 12. 24..
 */
public class SignUpRequest implements Request<ResAccountInfo> {

    private final Context context;
    private final ReqSignUpInfo reqSignUpInfo;

    private SignUpRequest(Context context, ReqSignUpInfo reqSignUpInfo) {
        this.context = context;
        this.reqSignUpInfo = reqSignUpInfo;
    }

    public static SignUpRequest create(Context context, ReqSignUpInfo reqSignUpInfo) {
        return new SignUpRequest(context, reqSignUpInfo);
    }

    @Override
    public ResAccountInfo request() throws JandiNetworkException {

        JandiRestClient jandiRestClient = new JandiRestClient_(context);
        return jandiRestClient.signUpAccount(reqSignUpInfo);
    }
}

package com.tosslab.jandi.app.network.manager.restapiclient;

import com.tosslab.jandi.app.network.client.account.password.AccountPasswordApi;
import com.tosslab.jandi.app.network.client.account.password.IAccountPasswordApiSimple;
import com.tosslab.jandi.app.network.client.main.IMainRestApiSimple;
import com.tosslab.jandi.app.network.client.main.MainRestApiClient;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitAdapterBuilder;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ReqAccountActivate;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ReqAccountVerification;
import com.tosslab.jandi.app.network.models.ReqSignUpInfo;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResAccountActivate;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResConfig;
import com.tosslab.jandi.app.network.models.ResMyTeam;



/**
 * Created by tee on 15. 6. 20..
 */
public class JacksonConvertedSimpleRestApiClient implements IAccountPasswordApiSimple, IMainRestApiSimple {

    @Override
    public ResCommon resetPasswordByAccountPasswordApi(ReqAccountEmail reqAccountEmail) {
        return RetrofitAdapterBuilder.newInstance(AccountPasswordApi.AccountPasswordApiV2Client.class).create().resetPassword(reqAccountEmail);
    }

    @Override
    public ResConfig getConfigByMainRest() {
        return RetrofitAdapterBuilder.newInstance(MainRestApiClient.class).create().getConfig();
    }

    @Override
    public ResMyTeam getTeamIdByMainRest(String userEmail) {
        return RetrofitAdapterBuilder.newInstance(MainRestApiClient.class).create().getTeamId(userEmail);
    }

    @Override
    public ResAccessToken getAccessTokenByMainRest(ReqAccessToken login) {
        return RetrofitAdapterBuilder.newInstance(MainRestApiClient.class).create().getAccessToken(login);
    }

    @Override
    public ResCommon signUpAccountByMainRest(ReqSignUpInfo signUpInfo) {
        return RetrofitAdapterBuilder.newInstance(MainRestApiClient.class).create().signUpAccount(signUpInfo);
    }

    @Override
    public ResAccountActivate activateAccountByMainRest(ReqAccountActivate reqAccountActivate) {
        return RetrofitAdapterBuilder.newInstance(MainRestApiClient.class).create().activateAccount(reqAccountActivate);
    }

    @Override
    public ResCommon accountVerificationByMainRest(ReqAccountVerification reqAccountVerification) {
        return RetrofitAdapterBuilder.newInstance(MainRestApiClient.class).create().accountVerification(reqAccountVerification);
    }

}



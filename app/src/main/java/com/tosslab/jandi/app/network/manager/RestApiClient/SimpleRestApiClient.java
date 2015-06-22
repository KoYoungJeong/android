package com.tosslab.jandi.app.network.manager.RestApiClient;

import com.tosslab.jandi.app.network.client.account.password.AccountPasswordApiV2Client;
import com.tosslab.jandi.app.network.client.account.password.IAccountPasswordApiSimple;
import com.tosslab.jandi.app.network.client.main.IMainRestApiSimple;
import com.tosslab.jandi.app.network.client.main.MainRestApiClient;
import com.tosslab.jandi.app.network.manager.RestApiClient.RestAdapterFactory.RestAdapterFactory;
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

import retrofit.RestAdapter;
import retrofit.RetrofitError;

/**
 * Created by tee on 15. 6. 20..
 */
public class SimpleRestApiClient implements IAccountPasswordApiSimple, IMainRestApiSimple {

    RestAdapter restAdapter = RestAdapterFactory.getJacksonConvertedSimpleRestAdapter();

    @Override
    public ResCommon resetPasswordByAccountPasswordApi(ReqAccountEmail reqAccountEmail) throws RetrofitError {
        return restAdapter.create(AccountPasswordApiV2Client.class).resetPassword(reqAccountEmail);
    }

    @Override
    public ResConfig getConfigByMainRest() throws RetrofitError {
        return restAdapter.create(MainRestApiClient.class).getConfig();
    }

    @Override
    public ResMyTeam getTeamIdByMainRest(String userEmail) throws RetrofitError {
        return restAdapter.create(MainRestApiClient.class).getTeamId(userEmail);
    }

    @Override
    public ResAccessToken getAccessTokenByMainRest(ReqAccessToken login) throws RetrofitError {
        return restAdapter.create(MainRestApiClient.class).getAccessToken(login);
    }

    @Override
    public ResCommon signUpAccountByMainRest(ReqSignUpInfo signUpInfo) throws RetrofitError {
        return restAdapter.create(MainRestApiClient.class).signUpAccount(signUpInfo);
    }

    @Override
    public ResAccountActivate activateAccountByMainRest(ReqAccountActivate reqAccountActivate) throws RetrofitError {
        return restAdapter.create(MainRestApiClient.class).activateAccount(reqAccountActivate);
    }

    @Override
    public ResCommon accountVerificationByMainRest(ReqAccountVerification reqAccountVerification) throws RetrofitError {
        return restAdapter.create(MainRestApiClient.class).accountVerification(reqAccountVerification);
    }

}



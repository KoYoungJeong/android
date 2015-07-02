package com.tosslab.jandi.app.network.client.main;

import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ReqAccountActivate;
import com.tosslab.jandi.app.network.models.ReqAccountVerification;
import com.tosslab.jandi.app.network.models.ReqSignUpInfo;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResAccountActivate;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResConfig;
import com.tosslab.jandi.app.network.models.ResMyTeam;

import retrofit.RetrofitError;

/**
 * Created by tee on 15. 6. 20..
 */
public interface IMainRestApiSimple {

    ResConfig getConfigByMainRest() throws RetrofitError;

    ResMyTeam getTeamIdByMainRest(String userEmail) throws RetrofitError;

    ResAccessToken getAccessTokenByMainRest(ReqAccessToken login) throws RetrofitError;

    ResCommon signUpAccountByMainRest(ReqSignUpInfo signUpInfo) throws RetrofitError;

    ResAccountActivate activateAccountByMainRest(ReqAccountActivate reqAccountActivate) throws RetrofitError;

    ResCommon accountVerificationByMainRest(ReqAccountVerification reqAccountVerification) throws RetrofitError;

}

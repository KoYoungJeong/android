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



/**
 * Created by tee on 15. 6. 20..
 */
public interface IMainRestApiSimple {

    ResConfig getConfigByMainRest() throws IOException;

    ResMyTeam getTeamIdByMainRest(String userEmail) throws IOException;

    ResAccessToken getAccessTokenByMainRest(ReqAccessToken login) throws IOException;

    ResCommon signUpAccountByMainRest(ReqSignUpInfo signUpInfo) throws IOException;

    ResAccountActivate activateAccountByMainRest(ReqAccountActivate reqAccountActivate) throws IOException;

    ResCommon accountVerificationByMainRest(ReqAccountVerification reqAccountVerification) throws IOException;

}

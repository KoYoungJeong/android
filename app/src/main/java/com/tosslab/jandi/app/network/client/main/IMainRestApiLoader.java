package com.tosslab.jandi.app.network.client.main;

import com.tosslab.jandi.app.network.manager.apiexecutor.Executor;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ReqAccountActivate;
import com.tosslab.jandi.app.network.models.ReqAccountVerification;
import com.tosslab.jandi.app.network.models.ReqSearchFile;
import com.tosslab.jandi.app.network.models.ReqSetMarker;
import com.tosslab.jandi.app.network.models.ReqSignUpInfo;
import com.tosslab.jandi.app.network.models.ReqUpdatePrimaryEmailInfo;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResAccountActivate;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResConfig;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMyTeam;
import com.tosslab.jandi.app.network.models.ResSearchFile;

/**
 * Created by tee on 15. 6. 20..
 */
public interface IMainRestApiLoader {

    Executor<ResAccountInfo> loadGetAccountInfoByMainRestApi();

    Executor<ResAccountInfo> loadUpdatePrimaryEmailByMainRestApi(ReqUpdatePrimaryEmailInfo
                                                                          updatePrimaryEmailInfo);

    Executor<ResLeftSideMenu> loadGetInfosForSideMenuByMainRestApi(long teamId);

    Executor<ResCommon> loadSetMarkerByMainRestApi(long entityId, ReqSetMarker reqSetMarker);

    Executor<ResSearchFile> loadSearchFileByMainRestApi(ReqSearchFile reqSearchFile);

    Executor<ResConfig> loadGetConfigByMainRestApi();

    Executor<ResMyTeam> loadGetTeamIdByMainRestApi(String userEmail);

    Executor<ResAccessToken> loadGetAccessTokenByMainRestApi(ReqAccessToken login);

    Executor<ResCommon> loadSignUpAccountByMainRestApi(ReqSignUpInfo signUpInfo);

    Executor<ResAccountActivate> loadActivateAccountByMainRestApi(ReqAccountActivate reqAccountActivate);

    Executor<ResCommon> loadAccountVerificationByMainRestApi(ReqAccountVerification reqAccountVerification);

}

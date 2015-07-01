package com.tosslab.jandi.app.network.client.main;

import com.tosslab.jandi.app.network.manager.apiexecutor.IExecutor;
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

    IExecutor<ResAccountInfo> loadGetAccountInfoByMainRestApi();

    IExecutor<ResAccountInfo> loadUpdatePrimaryEmailByMainRestApi(ReqUpdatePrimaryEmailInfo
                                                                          updatePrimaryEmailInfo);

    IExecutor<ResLeftSideMenu> loadGetInfosForSideMenuByMainRestApi(int teamId);

    IExecutor<ResCommon> loadSetMarkerByMainRestApi(int entityId, ReqSetMarker reqSetMarker);

    IExecutor<ResSearchFile> loadSearchFileByMainRestApi(ReqSearchFile reqSearchFile);

    IExecutor<ResConfig> loadGetConfigByMainRestApi();

    IExecutor<ResMyTeam> loadGetTeamIdByMainRestApi(String userEmail);

    IExecutor<ResAccessToken> loadGetAccessTokenByMainRestApi(ReqAccessToken login);

    IExecutor<ResCommon> loadSignUpAccountByMainRestApi(ReqSignUpInfo signUpInfo);

    IExecutor<ResAccountActivate> loadActivateAccountByMainRestApi(ReqAccountActivate reqAccountActivate);

    IExecutor<ResCommon> loadAccountVerificationByMainRestApi(ReqAccountVerification reqAccountVerification);

}

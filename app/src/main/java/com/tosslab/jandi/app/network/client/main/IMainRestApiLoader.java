package com.tosslab.jandi.app.network.client.main;

import com.tosslab.jandi.app.network.manager.ApiExecutor.IExecutor;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ReqAccountActivate;
import com.tosslab.jandi.app.network.models.ReqAccountVerification;
import com.tosslab.jandi.app.network.models.ReqSearchFile;
import com.tosslab.jandi.app.network.models.ReqSetMarker;
import com.tosslab.jandi.app.network.models.ReqSignUpInfo;
import com.tosslab.jandi.app.network.models.ReqUpdatePrimaryEmailInfo;

/**
 * Created by tee on 15. 6. 20..
 */
public interface IMainRestApiLoader {

    public IExecutor loadGetAccountInfoByMainRestApi();

    public IExecutor loadUpdatePrimaryEmailByMainRestApi(ReqUpdatePrimaryEmailInfo updatePrimaryEmailInfo);

    public IExecutor loadGetInfosForSideMenuByMainRestApi(int teamId);

    public IExecutor loadSetMarkerByMainRestApi(int entityId, ReqSetMarker reqSetMarker);

    public IExecutor loadSearchFileByMainRestApi(ReqSearchFile reqSearchFile);

    public IExecutor loadGetConfigByMainRestApi();

    public IExecutor loadGetTeamIdByMainRestApi(String userEmail);

    public IExecutor loadGetAccessTokenByMainRestApi(ReqAccessToken login);

    public IExecutor loadSignUpAccountByMainRestApi(ReqSignUpInfo signUpInfo);

    public IExecutor loadActivateAccountByMainRestApi(ReqAccountActivate reqAccountActivate);

    public IExecutor loadAccountVerificationByMainRestApi(ReqAccountVerification reqAccountVerification);

}

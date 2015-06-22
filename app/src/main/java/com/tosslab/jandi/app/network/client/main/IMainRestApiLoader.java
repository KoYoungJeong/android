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

    public IExecutor<?> setExecutorGetAccountInfo();

    public IExecutor<?> setExecutorUpdatePrimaryEmail(ReqUpdatePrimaryEmailInfo updatePrimaryEmailInfo);

    public IExecutor<?> setExecutorGetInfosForSideMenu(int teamId);

    public IExecutor<?> setExecutorSetMarker(int entityId, ReqSetMarker reqSetMarker);

    public IExecutor<?> setExecutorSearchFile(ReqSearchFile reqSearchFile);

    public IExecutor<?> setExecutorGetConfig();

    public IExecutor<?> setExecutorGetTeamId(String userEmail);

    public IExecutor<?> setExecutorGetAccessToken(ReqAccessToken login);

    public IExecutor<?> setExecutorSignUpAccount(ReqSignUpInfo signUpInfo);

    public IExecutor<?> setExecutorActivateAccount(ReqAccountActivate reqAccountActivate);

    public IExecutor<?> setExecutorAccountVerification(ReqAccountVerification reqAccountVerification);

}

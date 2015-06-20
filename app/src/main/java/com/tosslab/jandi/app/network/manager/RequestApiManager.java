package com.tosslab.jandi.app.network.manager;

import com.tosslab.jandi.app.network.client.account.devices.IAccountDeviceApiAuth;
import com.tosslab.jandi.app.network.client.account.emails.IAccountEmailApiAuth;
import com.tosslab.jandi.app.network.client.account.password.IAccountPasswordApiSimple;
import com.tosslab.jandi.app.network.client.account.password.IAccountPasswordApiAuth;
import com.tosslab.jandi.app.network.client.main.IMainRestApiSimple;
import com.tosslab.jandi.app.network.client.main.IMainRestApiAuth;
import com.tosslab.jandi.app.network.manager.ApiExecutor.PoolableRequestApiExecutor;
import com.tosslab.jandi.app.network.manager.ApiLoader.RestApiLoader;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ReqAccountActivate;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ReqAccountVerification;
import com.tosslab.jandi.app.network.models.ReqChangePassword;
import com.tosslab.jandi.app.network.models.ReqConfirmEmail;
import com.tosslab.jandi.app.network.models.ReqDeviceToken;
import com.tosslab.jandi.app.network.models.ReqNotificationRegister;
import com.tosslab.jandi.app.network.models.ReqNotificationTarget;
import com.tosslab.jandi.app.network.models.ReqSearchFile;
import com.tosslab.jandi.app.network.models.ReqSetMarker;
import com.tosslab.jandi.app.network.models.ReqSignUpInfo;
import com.tosslab.jandi.app.network.models.ReqSubscibeToken;
import com.tosslab.jandi.app.network.models.ReqUpdatePrimaryEmailInfo;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResAccountActivate;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResConfig;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMyTeam;
import com.tosslab.jandi.app.network.models.ResSearchFile;

import retrofit.RetrofitError;

/**
 * Created by tee on 15. 6. 20..
 */

public class RequestApiManager implements IAccountDeviceApiAuth, IAccountEmailApiAuth,
        IAccountPasswordApiAuth, IAccountPasswordApiSimple, IMainRestApiSimple, IMainRestApiAuth {

    private static final RequestApiManager requestApiManager = new RequestApiManager();
    private RestApiLoader restApiLoader = RestApiLoader.getInstance();

    private RequestApiManager() {
    }

    public static final RequestApiManager getInstance() {
        return requestApiManager;
    }

    @Override
    public ResAccountInfo registerNotificationTokenByAccountDeviceApi(ReqNotificationRegister reqNotificationRegister) throws RetrofitError {
        return (ResAccountInfo) PoolableRequestApiExecutor.obtain().execute(restApiLoader.setExecutorRegisterNotificationToken(reqNotificationRegister));
    }

    @Override
    public ResAccountInfo deleteNotificationTokenByAccountDeviceApi(ReqDeviceToken reqDeviceToken) throws RetrofitError {
        return (ResAccountInfo) PoolableRequestApiExecutor.obtain().execute(restApiLoader.setExecutorDeleteNotificationToken(reqDeviceToken));
    }

    @Override
    public ResAccountInfo subscribeStateNotificationByAccountDeviceApi(ReqSubscibeToken reqDeviceToken) throws RetrofitError {
        return (ResAccountInfo) PoolableRequestApiExecutor.obtain().execute(restApiLoader.setExecutorSubscribeStateNotification(reqDeviceToken));
    }

    @Override
    public ResCommon getNotificationBadgeByAccountDeviceApi(ReqNotificationTarget reqNotificationTarget) throws RetrofitError {
        return (ResCommon) PoolableRequestApiExecutor.obtain().execute(restApiLoader.setExecutorGetNotificationBadge(reqNotificationTarget));
    }

    @Override
    public ResAccountInfo requestAddEmailByAccountEmailApi(ReqAccountEmail reqAccountEmail) throws RetrofitError {
        return (ResAccountInfo) PoolableRequestApiExecutor.obtain().execute(restApiLoader.setExecutorRequestAddEmail(reqAccountEmail));
    }

    @Override
    public ResAccountInfo confirmEmailByAccountEmailApi(ReqConfirmEmail reqConfirmEmail) throws RetrofitError {
        return (ResAccountInfo) PoolableRequestApiExecutor.obtain().execute(restApiLoader.setExecutorConfirmEmail(reqConfirmEmail));
    }

    @Override
    public ResAccountInfo deleteEmailByAccountEmailApi(ReqAccountEmail reqConfirmEmail) throws RetrofitError {
        return (ResAccountInfo) PoolableRequestApiExecutor.obtain().execute(restApiLoader.setExecutorDeleteEmail(reqConfirmEmail));
    }

    @Override
    public ResCommon resetPasswordByAccountPasswordApi(ReqAccountEmail reqAccountEmail) throws RetrofitError {
        return (ResCommon) PoolableRequestApiExecutor.obtain().execute(restApiLoader.setExecutorResetPassword(reqAccountEmail));
    }

    @Override
    public ResCommon changePasswordByAccountPasswordApi(ReqChangePassword reqConfirmEmail) throws RetrofitError {
        return (ResCommon) PoolableRequestApiExecutor.obtain().execute(restApiLoader.setExecutorChangePassword(reqConfirmEmail));
    }

    @Override
    public ResConfig getConfigByMainRest() throws RetrofitError {
        return (ResConfig) PoolableRequestApiExecutor.obtain().execute(restApiLoader.setExecutorGetConfig());
    }

    @Override
    public ResMyTeam getTeamIdByMainRest(String userEmail) throws RetrofitError {
        return (ResMyTeam) PoolableRequestApiExecutor.obtain().execute(restApiLoader.setExecutorGetTeamId(userEmail));
    }

    @Override
    public ResAccessToken getAccessTokenByMainRest(ReqAccessToken login) throws RetrofitError {
        return (ResAccessToken) PoolableRequestApiExecutor.obtain().execute(restApiLoader.setExecutorGetAccessToken(login));
    }

    @Override
    public ResCommon signUpAccountByMainRest(ReqSignUpInfo signUpInfo) throws RetrofitError {
        return (ResCommon) PoolableRequestApiExecutor.obtain().execute(restApiLoader.setExecutorSignUpAccount(signUpInfo));
    }

    @Override
    public ResAccountActivate activateAccountByMainRest(ReqAccountActivate reqAccountActivate) throws RetrofitError {
        return (ResAccountActivate) PoolableRequestApiExecutor.obtain().execute(restApiLoader.setExecutorActivateAccount(reqAccountActivate));
    }

    @Override
    public ResCommon accountVerificationByMainRest(ReqAccountVerification reqAccountVerification) throws RetrofitError {
        return (ResCommon) PoolableRequestApiExecutor.obtain().execute(restApiLoader.setExecutorAccountVerification(reqAccountVerification));
    }

    @Override
    public ResAccountInfo getAccountInfoByMainRest() throws RetrofitError {
        return (ResAccountInfo) PoolableRequestApiExecutor.obtain().execute(restApiLoader.setExecutorGetAccountInfo());
    }

    @Override
    public ResAccountInfo updatePrimaryEmailByMainRest(ReqUpdatePrimaryEmailInfo updatePrimaryEmailInfo) throws RetrofitError {
        return (ResAccountInfo) PoolableRequestApiExecutor.obtain().execute(restApiLoader.setExecutorUpdatePrimaryEmail(updatePrimaryEmailInfo));
    }

    @Override
    public ResLeftSideMenu getInfosForSideMenuByMainRest(int teamId) throws RetrofitError {
        return (ResLeftSideMenu) PoolableRequestApiExecutor.obtain().execute(restApiLoader.setExecutorGetInfosForSideMenu(teamId));
    }

    @Override
    public ResCommon setMarkerByMainRest(int entityId, ReqSetMarker reqSetMarker) throws RetrofitError {
        return (ResCommon) PoolableRequestApiExecutor.obtain().execute(restApiLoader.setExecutorSetMarker(entityId, reqSetMarker));
    }

    @Override
    public ResSearchFile searchFileByMainRest(ReqSearchFile reqSearchFile) throws RetrofitError {
        return (ResSearchFile) PoolableRequestApiExecutor.obtain().execute(restApiLoader.setExecutorSearchFile(reqSearchFile));
    }

}
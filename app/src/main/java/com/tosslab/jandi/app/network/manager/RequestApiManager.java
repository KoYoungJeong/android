package com.tosslab.jandi.app.network.manager;

import com.tosslab.jandi.app.network.client.account.devices.IAccountDeviceApiAuth;
import com.tosslab.jandi.app.network.client.account.emails.IAccountEmailApiAuth;
import com.tosslab.jandi.app.network.client.account.password.IAccountPasswordApiAuth;
import com.tosslab.jandi.app.network.client.account.password.IAccountPasswordApiSimple;
import com.tosslab.jandi.app.network.client.main.IMainRestApiAuth;
import com.tosslab.jandi.app.network.client.main.IMainRestApiSimple;
import com.tosslab.jandi.app.network.manager.ApiExecutor.IExecutor;
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

    public Object RequestApiExecute(IExecutor executor) {
        PoolableRequestApiExecutor requestApiexecutor = PoolableRequestApiExecutor.obtain();
        Object result = requestApiexecutor.execute(executor);
        requestApiexecutor.recycle();
        return result;
    }

    @Override
    public synchronized ResAccountInfo registerNotificationTokenByAccountDeviceApi(ReqNotificationRegister reqNotificationRegister) throws RetrofitError {
        return (ResAccountInfo) RequestApiExecute(restApiLoader.setExecutorRegisterNotificationToken(reqNotificationRegister));
    }

    @Override
    public synchronized ResAccountInfo deleteNotificationTokenByAccountDeviceApi(ReqDeviceToken reqDeviceToken) throws RetrofitError {
        return (ResAccountInfo) RequestApiExecute(restApiLoader.setExecutorDeleteNotificationToken(reqDeviceToken));
    }

    @Override
    public synchronized ResAccountInfo subscribeStateNotificationByAccountDeviceApi(ReqSubscibeToken reqDeviceToken) throws RetrofitError {
        return (ResAccountInfo) RequestApiExecute(restApiLoader.setExecutorSubscribeStateNotification(reqDeviceToken));
    }

    @Override
    public synchronized ResCommon getNotificationBadgeByAccountDeviceApi(ReqNotificationTarget reqNotificationTarget) throws RetrofitError {
        return (ResCommon) RequestApiExecute(restApiLoader.setExecutorGetNotificationBadge(reqNotificationTarget));
    }

    @Override
    public synchronized ResAccountInfo requestAddEmailByAccountEmailApi(ReqAccountEmail reqAccountEmail) throws RetrofitError {
        return (ResAccountInfo) RequestApiExecute(restApiLoader.setExecutorRequestAddEmail(reqAccountEmail));
    }

    @Override
    public synchronized ResAccountInfo confirmEmailByAccountEmailApi(ReqConfirmEmail reqConfirmEmail) throws RetrofitError {
        return (ResAccountInfo) RequestApiExecute(restApiLoader.setExecutorConfirmEmail(reqConfirmEmail));
    }

    @Override
    public synchronized ResAccountInfo deleteEmailByAccountEmailApi(ReqAccountEmail reqConfirmEmail) throws RetrofitError {
        return (ResAccountInfo) RequestApiExecute(restApiLoader.setExecutorDeleteEmail(reqConfirmEmail));
    }

    @Override
    public synchronized ResCommon resetPasswordByAccountPasswordApi(ReqAccountEmail reqAccountEmail) throws RetrofitError {
        return (ResCommon) RequestApiExecute(restApiLoader.setExecutorResetPassword(reqAccountEmail));

    }

    @Override
    public synchronized ResCommon changePasswordByAccountPasswordApi(ReqChangePassword reqConfirmEmail) throws RetrofitError {
        return (ResCommon) RequestApiExecute(restApiLoader.setExecutorChangePassword(reqConfirmEmail));
    }

    @Override
    public synchronized ResConfig getConfigByMainRest() throws RetrofitError {
        return (ResConfig) RequestApiExecute(restApiLoader.setExecutorGetConfig());
    }

    @Override
    public synchronized ResMyTeam getTeamIdByMainRest(String userEmail) throws RetrofitError {
        return (ResMyTeam) RequestApiExecute(restApiLoader.setExecutorGetTeamId(userEmail));
    }

    @Override
    public synchronized ResAccessToken getAccessTokenByMainRest(ReqAccessToken login) throws RetrofitError {
        return (ResAccessToken) RequestApiExecute(restApiLoader.setExecutorGetAccessToken(login));
    }

    @Override
    public synchronized ResCommon signUpAccountByMainRest(ReqSignUpInfo signUpInfo) throws RetrofitError {
        return (ResCommon) RequestApiExecute(restApiLoader.setExecutorSignUpAccount(signUpInfo));
    }

    @Override
    public synchronized ResAccountActivate activateAccountByMainRest(ReqAccountActivate reqAccountActivate) throws RetrofitError {
        return (ResAccountActivate) RequestApiExecute(restApiLoader.setExecutorActivateAccount(reqAccountActivate));
    }

    @Override
    public ResCommon accountVerificationByMainRest(ReqAccountVerification reqAccountVerification) throws RetrofitError {
        return (ResCommon) RequestApiExecute(restApiLoader.setExecutorAccountVerification(reqAccountVerification));
    }

    @Override
    public synchronized ResAccountInfo getAccountInfoByMainRest() throws RetrofitError {
        return (ResAccountInfo) RequestApiExecute(restApiLoader.setExecutorGetAccountInfo());
    }

    @Override
    public synchronized ResAccountInfo updatePrimaryEmailByMainRest(ReqUpdatePrimaryEmailInfo updatePrimaryEmailInfo) throws RetrofitError {
        return (ResAccountInfo) RequestApiExecute(restApiLoader.setExecutorUpdatePrimaryEmail(updatePrimaryEmailInfo));
    }

    @Override
    public synchronized ResLeftSideMenu getInfosForSideMenuByMainRest(int teamId) throws RetrofitError {
        return (ResLeftSideMenu) RequestApiExecute(restApiLoader.setExecutorGetInfosForSideMenu(teamId));
    }

    @Override
    public synchronized ResCommon setMarkerByMainRest(int entityId, ReqSetMarker reqSetMarker) throws RetrofitError {
        return (ResCommon) RequestApiExecute(restApiLoader.setExecutorSetMarker(entityId, reqSetMarker));
    }

    @Override
    public synchronized ResSearchFile searchFileByMainRest(ReqSearchFile reqSearchFile) throws RetrofitError {
        return (ResSearchFile) RequestApiExecute(restApiLoader.setExecutorSearchFile(reqSearchFile));
    }

}
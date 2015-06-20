package com.tosslab.jandi.app.network.client;

import com.tosslab.jandi.app.network.client.account.devices.IAccountDeviceApiForAuth;
import com.tosslab.jandi.app.network.client.account.emails.IAccountEmailApiForAuth;
import com.tosslab.jandi.app.network.client.account.password.IAccountPasswordApi;
import com.tosslab.jandi.app.network.client.account.password.IAccountPasswordApiForAuth;
import com.tosslab.jandi.app.network.client.main.IMainRestApi;
import com.tosslab.jandi.app.network.client.main.IMainRestApiforAuth;
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

public class RequestApiManager extends RequestApiExecutor implements IAccountDeviceApiForAuth, IAccountEmailApiForAuth,
        IAccountPasswordApiForAuth, IAccountPasswordApi, IMainRestApi, IMainRestApiforAuth {

    RestApiLoader restApiLoader = RestApiLoader.getInstance();

    @Override
    public ResAccountInfo accountDeviceRegisterNotificationToken(ReqNotificationRegister reqNotificationRegister) throws RetrofitError {
        return (ResAccountInfo) execute(restApiLoader.setExecutorRegisterNotificationToken(reqNotificationRegister));
    }

    @Override
    public ResAccountInfo accountDeviceDeleteNotificationToken(ReqDeviceToken reqDeviceToken) throws RetrofitError {
        return (ResAccountInfo) execute(restApiLoader.setExecutorDeleteNotificationToken(reqDeviceToken));
    }

    @Override
    public ResAccountInfo accountDeviceSubscribeStateNotification(ReqSubscibeToken reqDeviceToken) throws RetrofitError {
        return (ResAccountInfo) execute(restApiLoader.setExecutorSubscribeStateNotification(reqDeviceToken));
    }

    @Override
    public ResCommon accountDeviceGetNotificationBadge(ReqNotificationTarget reqNotificationTarget) throws RetrofitError {
        return (ResCommon) execute(restApiLoader.setExecutorGetNotificationBadge(reqNotificationTarget));
    }

    @Override
    public ResAccountInfo accountEmailRequestAddEmail(ReqAccountEmail reqAccountEmail) throws RetrofitError {
        return (ResAccountInfo) execute(restApiLoader.setExecutorRequestAddEmail(reqAccountEmail));
    }

    @Override
    public ResAccountInfo accountEmailConfirmEmail(ReqConfirmEmail reqConfirmEmail) throws RetrofitError {
        return (ResAccountInfo) execute(restApiLoader.setExecutorConfirmEmail(reqConfirmEmail));
    }

    @Override
    public ResAccountInfo accountEmailDeleteEmail(ReqAccountEmail reqConfirmEmail) throws RetrofitError {
        return (ResAccountInfo) execute(restApiLoader.setExecutorDeleteEmail(reqConfirmEmail));
    }

    @Override
    public ResCommon accountPasswordResetPassword(ReqAccountEmail reqAccountEmail) throws RetrofitError {
        return (ResCommon) execute(restApiLoader.setExecutorResetPassword(reqAccountEmail));
    }

    @Override
    public ResCommon accountPasswordChangePassword(ReqChangePassword reqConfirmEmail) throws RetrofitError {
        return (ResCommon) execute(restApiLoader.setExecutorChangePassword(reqConfirmEmail));
    }

    @Override
    public ResConfig mainRestGetConfig() throws RetrofitError {
        return (ResConfig) execute(restApiLoader.setExecutorGetConfig());
    }

    @Override
    public ResMyTeam mainRestGetTeamId(String userEmail) throws RetrofitError {
        return (ResMyTeam) execute(restApiLoader.setExecutorGetTeamId(userEmail));
    }

    @Override
    public ResAccessToken mainRestGetAccessToken(ReqAccessToken login) throws RetrofitError {
        return (ResAccessToken) execute(restApiLoader.setExecutorGetAccessToken(login));
    }

    @Override
    public ResCommon mainRestSignUpAccount(ReqSignUpInfo signUpInfo) throws RetrofitError {
        return (ResCommon) execute(restApiLoader.setExecutorSignUpAccount(signUpInfo));
    }

    @Override
    public ResAccountActivate mainRestActivateAccount(ReqAccountActivate reqAccountActivate) throws RetrofitError {
        return (ResAccountActivate) execute(restApiLoader.setExecutorActivateAccount(reqAccountActivate));
    }

    @Override
    public ResCommon mainRestAccountVerification(ReqAccountVerification reqAccountVerification) throws RetrofitError {
        return (ResCommon) execute(restApiLoader.setExecutorAccountVerification(reqAccountVerification));
    }

    @Override
    public ResAccountInfo mainRestGetAccountInfo() throws RetrofitError {
        return (ResAccountInfo) execute(restApiLoader.setExecutorGetAccountInfo());
    }

    @Override
    public ResAccountInfo mainRestUpdatePrimaryEmail(ReqUpdatePrimaryEmailInfo updatePrimaryEmailInfo) throws RetrofitError {
        return (ResAccountInfo) execute(restApiLoader.setExecutorUpdatePrimaryEmail(updatePrimaryEmailInfo));
    }

    @Override
    public ResLeftSideMenu mainRestGetInfosForSideMenu(int teamId) throws RetrofitError {
        return (ResLeftSideMenu) execute(restApiLoader.setExecutorGetInfosForSideMenu(teamId));
    }

    @Override
    public ResCommon mainRestSetMarker(int entityId, ReqSetMarker reqSetMarker) throws RetrofitError {
        return (ResCommon) execute(restApiLoader.setExecutorSetMarker(entityId, reqSetMarker));
    }

    @Override
    public ResSearchFile mainRestSearchFile(ReqSearchFile reqSearchFile) throws RetrofitError {
        return (ResSearchFile) execute(restApiLoader.setExecutorSearchFile(reqSearchFile));
    }

}
package com.tosslab.jandi.app.network.manager.ApiLoader;

import com.tosslab.jandi.app.network.client.account.devices.IAccountDeviceApiLoader;
import com.tosslab.jandi.app.network.client.account.emails.IAccountEmailsApiLoader;
import com.tosslab.jandi.app.network.client.account.password.IAccountPasswordApiLoader;
import com.tosslab.jandi.app.network.client.main.IMainRestApiLoader;
import com.tosslab.jandi.app.network.manager.ApiExecutor.IExecutor;
import com.tosslab.jandi.app.network.manager.RestApiClient.JacksonConvertedAuthRestApiClient;
import com.tosslab.jandi.app.network.manager.RestApiClient.JacksonConvertedSimpleRestApiClient;
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

import retrofit.RetrofitError;

/**
 * Created by tee on 15. 6. 20..
 */
public class RestApiLoader implements IAccountDeviceApiLoader, IAccountEmailsApiLoader, IAccountPasswordApiLoader, IMainRestApiLoader {

    JacksonConvertedAuthRestApiClient authRestApiClient = new JacksonConvertedAuthRestApiClient();

    JacksonConvertedSimpleRestApiClient SimpleRestApiClient = new JacksonConvertedSimpleRestApiClient();

    private RestApiLoader() {
    }

    public static RestApiLoader getInstance() {
        return new RestApiLoader();
    }

    @Override
    public IExecutor setExecutorRegisterNotificationToken(ReqNotificationRegister reqNotificationRegister) {
        return () -> authRestApiClient.registerNotificationTokenByAccountDeviceApi(reqNotificationRegister);
    }

    @Override
    public IExecutor setExecutorDeleteNotificationToken(ReqDeviceToken reqDeviceToken) {
        return () -> authRestApiClient.deleteNotificationTokenByAccountDeviceApi(reqDeviceToken);
    }

    @Override
    public IExecutor setExecutorSubscribeStateNotification(ReqSubscibeToken reqDeviceToken) {
        return () -> authRestApiClient.subscribeStateNotificationByAccountDeviceApi(reqDeviceToken);
    }

    @Override
    public IExecutor setExecutorGetNotificationBadge(ReqNotificationTarget reqNotificationTarget) {
        return () -> authRestApiClient.getNotificationBadgeByAccountDeviceApi(reqNotificationTarget);
    }

    @Override
    public IExecutor setExecutorRequestAddEmail(ReqAccountEmail reqAccountEmail) {
        return () -> authRestApiClient.requestAddEmailByAccountEmailApi(reqAccountEmail);
    }

    @Override
    public IExecutor setExecutorConfirmEmail(ReqConfirmEmail reqConfirmEmail) {
        return () -> authRestApiClient.confirmEmailByAccountEmailApi(reqConfirmEmail);
    }

    @Override
    public IExecutor setExecutorDeleteEmail(ReqAccountEmail reqConfirmEmail) {
        return () -> authRestApiClient.deleteEmailByAccountEmailApi(reqConfirmEmail);
    }

    @Override
    public IExecutor setExecutorChangePassword(ReqChangePassword reqConfirmEmail) {
        return () -> authRestApiClient.changePasswordByAccountPasswordApi(reqConfirmEmail);
    }

    @Override
    public IExecutor setExecutorGetAccountInfo() throws RetrofitError {
        return () -> authRestApiClient.getAccountInfoByMainRest();
    }

    @Override
    public IExecutor setExecutorUpdatePrimaryEmail(ReqUpdatePrimaryEmailInfo updatePrimaryEmailInfo) {
        return () -> authRestApiClient.updatePrimaryEmailByMainRest(updatePrimaryEmailInfo);
    }

    @Override
    public IExecutor setExecutorGetInfosForSideMenu(int teamId) {
        return () -> authRestApiClient.getInfosForSideMenuByMainRest(teamId);
    }

    @Override
    public IExecutor setExecutorSetMarker(int entityId, ReqSetMarker reqSetMarker) {
        return () -> authRestApiClient.setMarkerByMainRest(entityId, reqSetMarker);
    }

    @Override
    public IExecutor setExecutorSearchFile(ReqSearchFile reqSearchFile) throws RetrofitError {
        return () -> authRestApiClient.searchFileByMainRest(reqSearchFile);
    }

    @Override
    public IExecutor setExecutorResetPassword(ReqAccountEmail reqAccountEmail) throws RetrofitError {
        return () -> SimpleRestApiClient.resetPasswordByAccountPasswordApi(reqAccountEmail);
    }

    @Override
    public IExecutor setExecutorGetConfig() throws RetrofitError {
        return () -> SimpleRestApiClient.getConfigByMainRest();
    }

    @Override
    public IExecutor setExecutorGetTeamId(String userEmail) throws RetrofitError {
        return () -> SimpleRestApiClient.getTeamIdByMainRest(userEmail);
    }

    @Override
    public IExecutor setExecutorGetAccessToken(ReqAccessToken login) throws RetrofitError {
        return () -> SimpleRestApiClient.getAccessTokenByMainRest(login);
    }

    @Override
    public IExecutor setExecutorSignUpAccount(ReqSignUpInfo signUpInfo) throws RetrofitError {
        return () -> SimpleRestApiClient.signUpAccountByMainRest(signUpInfo);
    }

    @Override
    public IExecutor setExecutorActivateAccount(ReqAccountActivate reqAccountActivate) throws RetrofitError {
        return () -> SimpleRestApiClient.activateAccountByMainRest(reqAccountActivate);
    }

    @Override
    public IExecutor setExecutorAccountVerification(ReqAccountVerification reqAccountVerification) throws RetrofitError {
        return () -> SimpleRestApiClient.accountVerificationByMainRest(reqAccountVerification);
    }

}
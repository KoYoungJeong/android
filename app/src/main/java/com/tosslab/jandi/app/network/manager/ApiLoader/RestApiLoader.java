package com.tosslab.jandi.app.network.manager.ApiLoader;

import com.tosslab.jandi.app.network.client.account.devices.IAccountDeviceApiLoader;
import com.tosslab.jandi.app.network.client.account.emails.IAccountEmailsApiLoader;
import com.tosslab.jandi.app.network.client.account.password.IAccountPasswordApiLoader;
import com.tosslab.jandi.app.network.client.chat.IChatApiLoader;
import com.tosslab.jandi.app.network.client.direct.message.IDirectMessageApiLoader;
import com.tosslab.jandi.app.network.client.invitation.IInvitationApiLoader;
import com.tosslab.jandi.app.network.client.main.IMainRestApiLoader;
import com.tosslab.jandi.app.network.client.messages.IMessagesApiLoader;
import com.tosslab.jandi.app.network.client.messages.comments.ICommentsApiLoader;
import com.tosslab.jandi.app.network.client.messages.search.IMessageSearchApiLoader;
import com.tosslab.jandi.app.network.client.privatetopic.IGroupApiLoader;
import com.tosslab.jandi.app.network.client.privatetopic.messages.IGroupMessageApiLoader;
import com.tosslab.jandi.app.network.client.publictopic.IChannelApiLoader;
import com.tosslab.jandi.app.network.client.publictopic.messages.IChannelMessageApiLoader;
import com.tosslab.jandi.app.network.client.rooms.IRoomsApiLoader;
import com.tosslab.jandi.app.network.client.settings.IAccountProfileApiLoader;
import com.tosslab.jandi.app.network.client.settings.IStarredEntityApiLoader;
import com.tosslab.jandi.app.network.client.sticker.IStickerApiLoader;
import com.tosslab.jandi.app.network.client.teams.ITeamApiLoader;
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
public class RestApiLoader implements IAccountDeviceApiLoader, IAccountEmailsApiLoader, IAccountPasswordApiLoader, IChatApiLoader,
        IDirectMessageApiLoader, IInvitationApiLoader, IMainRestApiLoader, ICommentsApiLoader, IMessageSearchApiLoader,
        IMessagesApiLoader, IGroupMessageApiLoader, IGroupApiLoader, IChannelMessageApiLoader, IChannelApiLoader,
        IRoomsApiLoader, IAccountProfileApiLoader, IStarredEntityApiLoader, IStickerApiLoader, ITeamApiLoader {

    JacksonConvertedAuthRestApiClient authRestApiClient = new JacksonConvertedAuthRestApiClient();

    JacksonConvertedSimpleRestApiClient SimpleRestApiClient = new JacksonConvertedSimpleRestApiClient();

    private RestApiLoader() {
    }

    public static RestApiLoader getInstance() {
        return new RestApiLoader();
    }

    @Override
    public IExecutor loadRegisterNotificationTokenByAccountDeviceApi(ReqNotificationRegister reqNotificationRegister) {
        return () -> authRestApiClient.registerNotificationTokenByAccountDeviceApi(reqNotificationRegister);
    }

    @Override
    public IExecutor loadDeleteNotificationTokenByAccountDeviceApi(ReqDeviceToken reqDeviceToken) {
        return () -> authRestApiClient.deleteNotificationTokenByAccountDeviceApi(reqDeviceToken);
    }

    @Override
    public IExecutor loadSubscribeStateNotificationByAccountDeviceApi(ReqSubscibeToken reqDeviceToken) {
        return () -> authRestApiClient.subscribeStateNotificationByAccountDeviceApi(reqDeviceToken);
    }

    @Override
    public IExecutor loadGetNotificationBadgeByAccountDeviceApi(ReqNotificationTarget reqNotificationTarget) {
        return () -> authRestApiClient.getNotificationBadgeByAccountDeviceApi(reqNotificationTarget);
    }

    @Override
    public IExecutor loadRequestAddEmailByAccountEmailsApi(ReqAccountEmail reqAccountEmail) {
        return () -> authRestApiClient.requestAddEmailByAccountEmailApi(reqAccountEmail);
    }

    @Override
    public IExecutor loadConfirmEmailByAccountEmailsApi(ReqConfirmEmail reqConfirmEmail) {
        return () -> authRestApiClient.confirmEmailByAccountEmailApi(reqConfirmEmail);
    }

    @Override
    public IExecutor loadExecutorDeleteEmailByAccountEmailsApi(ReqAccountEmail reqConfirmEmail) {
        return () -> authRestApiClient.deleteEmailByAccountEmailApi(reqConfirmEmail);
    }

    @Override
    public IExecutor loadChangePasswordByAccountEmailsApi(ReqChangePassword reqConfirmEmail) {
        return () -> authRestApiClient.changePasswordByAccountPasswordApi(reqConfirmEmail);
    }

    @Override
    public IExecutor loadGetAccountInfoByMainRestApi() throws RetrofitError {
        return () -> authRestApiClient.getAccountInfoByMainRest();
    }

    @Override
    public IExecutor loadUpdatePrimaryEmailByMainRestApi(ReqUpdatePrimaryEmailInfo updatePrimaryEmailInfo) {
        return () -> authRestApiClient.updatePrimaryEmailByMainRest(updatePrimaryEmailInfo);
    }

    @Override
    public IExecutor loadGetInfosForSideMenuByMainRestApi(int teamId) {
        return () -> authRestApiClient.getInfosForSideMenuByMainRest(teamId);
    }

    @Override
    public IExecutor loadSetMarkerByMainRestApi(int entityId, ReqSetMarker reqSetMarker) {
        return () -> authRestApiClient.setMarkerByMainRest(entityId, reqSetMarker);
    }

    @Override
    public IExecutor loadSearchFileByMainRestApi(ReqSearchFile reqSearchFile) throws RetrofitError {
        return () -> authRestApiClient.searchFileByMainRest(reqSearchFile);
    }

    @Override
    public IExecutor loadResetPasswordByAccountPasswordApi(ReqAccountEmail reqAccountEmail) throws RetrofitError {
        return () -> SimpleRestApiClient.resetPasswordByAccountPasswordApi(reqAccountEmail);
    }

    @Override
    public IExecutor loadGetConfigByMainRestApi() throws RetrofitError {
        return () -> SimpleRestApiClient.getConfigByMainRest();
    }

    @Override
    public IExecutor loadGetTeamIdByMainRestApi(String userEmail) throws RetrofitError {
        return () -> SimpleRestApiClient.getTeamIdByMainRest(userEmail);
    }

    @Override
    public IExecutor loadGetAccessTokenByMainRestApi(ReqAccessToken login) throws RetrofitError {
        return () -> SimpleRestApiClient.getAccessTokenByMainRest(login);
    }

    @Override
    public IExecutor loadSignUpAccountByMainRestApi(ReqSignUpInfo signUpInfo) throws RetrofitError {
        return () -> SimpleRestApiClient.signUpAccountByMainRest(signUpInfo);
    }

    @Override
    public IExecutor loadActivateAccountByMainRestApi(ReqAccountActivate reqAccountActivate) throws RetrofitError {
        return () -> SimpleRestApiClient.activateAccountByMainRest(reqAccountActivate);
    }

    @Override
    public IExecutor loadAccountVerificationByMainRestApi(ReqAccountVerification reqAccountVerification) throws RetrofitError {
        return () -> SimpleRestApiClient.accountVerificationByMainRest(reqAccountVerification);
    }

}
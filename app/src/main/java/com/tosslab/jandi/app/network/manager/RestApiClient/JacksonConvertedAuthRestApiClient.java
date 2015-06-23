package com.tosslab.jandi.app.network.manager.RestApiClient;

import com.tosslab.jandi.app.network.client.account.devices.AccountDeviceApiV2Client;
import com.tosslab.jandi.app.network.client.account.devices.IAccountDeviceApiAuth;
import com.tosslab.jandi.app.network.client.account.emails.AccountEmailsApiV2Client;
import com.tosslab.jandi.app.network.client.account.emails.IAccountEmailApiAuth;
import com.tosslab.jandi.app.network.client.account.password.AccountPasswordApiV2Client;
import com.tosslab.jandi.app.network.client.account.password.IAccountPasswordApiAuth;
import com.tosslab.jandi.app.network.client.main.IMainRestApiAuth;
import com.tosslab.jandi.app.network.client.main.MainRestApiClient;
import com.tosslab.jandi.app.network.manager.RestApiClient.RestAdapterFactory.builder.RestAdapterBuilder;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ReqChangePassword;
import com.tosslab.jandi.app.network.models.ReqConfirmEmail;
import com.tosslab.jandi.app.network.models.ReqDeviceToken;
import com.tosslab.jandi.app.network.models.ReqNotificationRegister;
import com.tosslab.jandi.app.network.models.ReqNotificationTarget;
import com.tosslab.jandi.app.network.models.ReqSearchFile;
import com.tosslab.jandi.app.network.models.ReqSetMarker;
import com.tosslab.jandi.app.network.models.ReqSubscibeToken;
import com.tosslab.jandi.app.network.models.ReqUpdatePrimaryEmailInfo;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResSearchFile;

import retrofit.RetrofitError;

/**
 * Created by tee on 15. 6. 20..
 */
public class JacksonConvertedAuthRestApiClient implements IMainRestApiAuth, IAccountPasswordApiAuth,
        IAccountEmailApiAuth, IAccountDeviceApiAuth {

    @Override
    public ResAccountInfo registerNotificationTokenByAccountDeviceApi(ReqNotificationRegister reqNotificationRegister) throws RetrofitError {
        return RestAdapterBuilder.newInstance(AccountDeviceApiV2Client.class).create().registerNotificationToken(reqNotificationRegister);
    }

    @Override
    public ResAccountInfo deleteNotificationTokenByAccountDeviceApi(ReqDeviceToken reqDeviceToken) throws RetrofitError {
        return RestAdapterBuilder.newInstance(AccountDeviceApiV2Client.class).create().deleteNotificationToken(reqDeviceToken);
    }

    @Override
    public ResAccountInfo subscribeStateNotificationByAccountDeviceApi(ReqSubscibeToken reqDeviceToken) throws RetrofitError {
        return RestAdapterBuilder.newInstance(AccountDeviceApiV2Client.class).create().subscribeStateNotification(reqDeviceToken);
    }

    @Override
    public ResCommon getNotificationBadgeByAccountDeviceApi(ReqNotificationTarget reqNotificationTarget) throws RetrofitError {
        return RestAdapterBuilder.newInstance(AccountDeviceApiV2Client.class).create().getNotificationBadge(reqNotificationTarget);
    }

    @Override
    public ResAccountInfo requestAddEmailByAccountEmailApi(ReqAccountEmail reqAccountEmail) throws RetrofitError {
        return RestAdapterBuilder.newInstance(AccountEmailsApiV2Client.class).create().requestAddEmail(reqAccountEmail);
    }

    @Override
    public ResAccountInfo confirmEmailByAccountEmailApi(ReqConfirmEmail reqConfirmEmail) throws RetrofitError {
        return RestAdapterBuilder.newInstance(AccountEmailsApiV2Client.class).create().confirmEmail(reqConfirmEmail);
    }

    @Override
    public ResAccountInfo deleteEmailByAccountEmailApi(ReqAccountEmail reqConfirmEmail) throws RetrofitError {
        return RestAdapterBuilder.newInstance(AccountEmailsApiV2Client.class).create().deleteEmail(reqConfirmEmail);
    }

    @Override
    public ResCommon changePasswordByAccountPasswordApi(ReqChangePassword reqConfirmEmail) throws RetrofitError {
        return RestAdapterBuilder.newInstance(AccountPasswordApiV2Client.class).create().changePassword(reqConfirmEmail);
    }

    @Override
    public ResAccountInfo getAccountInfoByMainRest() throws RetrofitError {
        return RestAdapterBuilder.newInstance(MainRestApiClient.class).create().getAccountInfo();
    }

    @Override
    public ResAccountInfo updatePrimaryEmailByMainRest(ReqUpdatePrimaryEmailInfo updatePrimaryEmailInfo) throws RetrofitError {
        return RestAdapterBuilder.newInstance(MainRestApiClient.class).create().updatePrimaryEmail(updatePrimaryEmailInfo);
    }

    @Override
    public ResLeftSideMenu getInfosForSideMenuByMainRest(int teamId) throws RetrofitError {
        return RestAdapterBuilder.newInstance(MainRestApiClient.class).create().getInfosForSideMenu(teamId);
    }

    @Override
    public ResCommon setMarkerByMainRest(int entityId, ReqSetMarker reqSetMarker) throws RetrofitError {
        return RestAdapterBuilder.newInstance(MainRestApiClient.class).create().setMarker(entityId, reqSetMarker);
    }

    @Override
    public ResSearchFile searchFileByMainRest(ReqSearchFile reqSearchFile) throws RetrofitError {
        return RestAdapterBuilder.newInstance(MainRestApiClient.class).create().searchFile(reqSearchFile);
    }

}
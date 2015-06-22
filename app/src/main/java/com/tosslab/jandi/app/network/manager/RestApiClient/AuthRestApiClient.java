package com.tosslab.jandi.app.network.manager.RestApiClient;

import com.tosslab.jandi.app.network.client.account.devices.AccountDeviceApiV2Client;
import com.tosslab.jandi.app.network.client.account.devices.IAccountDeviceApiAuth;
import com.tosslab.jandi.app.network.client.account.emails.AccountEmailsApiV2Client;
import com.tosslab.jandi.app.network.client.account.emails.IAccountEmailApiAuth;
import com.tosslab.jandi.app.network.client.account.password.AccountPasswordApiV2Client;
import com.tosslab.jandi.app.network.client.account.password.IAccountPasswordApiAuth;
import com.tosslab.jandi.app.network.client.main.IMainRestApiAuth;
import com.tosslab.jandi.app.network.client.main.MainRestApiClient;
import com.tosslab.jandi.app.network.manager.RestApiClient.RestAdapterFactory.RestAdapterFactory;
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

import retrofit.RestAdapter;
import retrofit.RetrofitError;

/**
 * Created by tee on 15. 6. 20..
 */
public class AuthRestApiClient implements IMainRestApiAuth, IAccountPasswordApiAuth,
        IAccountEmailApiAuth, IAccountDeviceApiAuth {

    static RestAdapter restAdapter = RestAdapterFactory.getAuthRestAdapter();

    @Override
    public ResAccountInfo registerNotificationTokenByAccountDeviceApi(ReqNotificationRegister reqNotificationRegister) throws RetrofitError {
        return restAdapter.create(AccountDeviceApiV2Client.class).registerNotificationToken(reqNotificationRegister);
    }

    @Override
    public ResAccountInfo deleteNotificationTokenByAccountDeviceApi(ReqDeviceToken reqDeviceToken) throws RetrofitError {
        return restAdapter.create(AccountDeviceApiV2Client.class).deleteNotificationToken(reqDeviceToken);
    }

    @Override
    public ResAccountInfo subscribeStateNotificationByAccountDeviceApi(ReqSubscibeToken reqDeviceToken) throws RetrofitError {
        return restAdapter.create(AccountDeviceApiV2Client.class).subscribeStateNotification(reqDeviceToken);
    }

    @Override
    public ResCommon getNotificationBadgeByAccountDeviceApi(ReqNotificationTarget reqNotificationTarget) throws RetrofitError {
        return restAdapter.create(AccountDeviceApiV2Client.class).getNotificationBadge(reqNotificationTarget);
    }

    @Override
    public ResAccountInfo requestAddEmailByAccountEmailApi(ReqAccountEmail reqAccountEmail) throws RetrofitError {
        return restAdapter.create(AccountEmailsApiV2Client.class).requestAddEmail(reqAccountEmail);
    }

    @Override
    public ResAccountInfo confirmEmailByAccountEmailApi(ReqConfirmEmail reqConfirmEmail) throws RetrofitError {
        return restAdapter.create(AccountEmailsApiV2Client.class).confirmEmail(reqConfirmEmail);
    }

    @Override
    public ResAccountInfo deleteEmailByAccountEmailApi(ReqAccountEmail reqConfirmEmail) throws RetrofitError {
        return restAdapter.create(AccountEmailsApiV2Client.class).deleteEmail(reqConfirmEmail);
    }

    @Override
    public ResCommon changePasswordByAccountPasswordApi(ReqChangePassword reqConfirmEmail) throws RetrofitError {
        return restAdapter.create(AccountPasswordApiV2Client.class).changePassword(reqConfirmEmail);
    }

    @Override
    public ResAccountInfo getAccountInfoByMainRest() throws RetrofitError {
        return restAdapter.create(MainRestApiClient.class).getAccountInfo();
    }

    @Override
    public ResAccountInfo updatePrimaryEmailByMainRest(ReqUpdatePrimaryEmailInfo updatePrimaryEmailInfo) throws RetrofitError {
        return restAdapter.create(MainRestApiClient.class).updatePrimaryEmail(updatePrimaryEmailInfo);
    }

    @Override
    public ResLeftSideMenu getInfosForSideMenuByMainRest(int teamId) throws RetrofitError {
        return restAdapter.create(MainRestApiClient.class).getInfosForSideMenu(teamId);
    }

    @Override
    public ResCommon setMarkerByMainRest(int entityId, ReqSetMarker reqSetMarker) throws RetrofitError {
        return restAdapter.create(MainRestApiClient.class).setMarker(entityId, reqSetMarker);
    }

    @Override
    public ResSearchFile searchFileByMainRest(ReqSearchFile reqSearchFile) throws RetrofitError {
        return restAdapter.create(MainRestApiClient.class).searchFile(reqSearchFile);
    }

}
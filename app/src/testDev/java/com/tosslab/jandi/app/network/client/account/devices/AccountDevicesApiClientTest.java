package com.tosslab.jandi.app.network.client.account.devices;

import com.tosslab.jandi.app.network.client.JandiRestClient;
import com.tosslab.jandi.app.network.client.JandiRestClient_;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ReqDeviceToken;
import com.tosslab.jandi.app.network.models.ReqNotificationRegister;
import com.tosslab.jandi.app.network.models.ReqSubscibeToken;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.spring.JandiV2HttpAuthentication;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.shadows.ShadowLog;
import org.springframework.web.client.HttpStatusCodeException;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(RobolectricGradleTestRunner.class)
public class AccountDevicesApiClientTest {

    public static final String TEMP_TOKEN = "aaa";
    private JandiRestClient jandiRestClient_;
    private ResLeftSideMenu sideMenu;
    private AccountDevicesApiClient accountDevicesApiClient;

    @Before
    public void setUp() throws Exception {

        jandiRestClient_ = new JandiRestClient_(Robolectric.application);
        accountDevicesApiClient = new AccountDevicesApiClient_(Robolectric.application);
        ResAccessToken accessToken = getAccessToken();

        jandiRestClient_.setAuthentication(new JandiV2HttpAuthentication(accessToken.getTokenType(), accessToken.getAccessToken()));
        accountDevicesApiClient.setAuthentication(new JandiV2HttpAuthentication(accessToken.getTokenType(), accessToken.getAccessToken()));

        sideMenu = getSideMenu();

        Robolectric.getFakeHttpLayer().interceptHttpRequests(false);

        System.setProperty("robolectric.logging", "stdout");
        ShadowLog.stream = System.out;

    }

    private ResLeftSideMenu getSideMenu() {
        ResLeftSideMenu infosForSideMenu = jandiRestClient_.getInfosForSideMenu(279);

        return infosForSideMenu;
    }

    private ResAccessToken getAccessToken() {

        jandiRestClient_.setHeader("Content-Type", "application/json");

        ResAccessToken accessToken = jandiRestClient_.getAccessToken(ReqAccessToken.createPasswordReqToken(BaseInitUtil.TEST_ID, BaseInitUtil.TEST_PASSWORD));
        System.out.println("========= Get Access Token =========");
        return accessToken;
    }

    @Ignore
    @Test
    public void testRegisterNotificationToken() throws Exception {
        ReqNotificationRegister reqNotiricationRegister = new ReqNotificationRegister("android", TEMP_TOKEN);

        ResAccountInfo resCommon = null;
        try {
            resCommon = accountDevicesApiClient.registerNotificationToken(reqNotiricationRegister);
        } catch (HttpStatusCodeException e) {
            fail(e.getResponseBodyAsString());
        }

        assertThat(resCommon, is(notNullValue()));
    }

    @Ignore
    @Test
    public void testDeleteNotificationToken() throws Exception {

        ResAccountInfo resCommon = null;
        try {
            resCommon = accountDevicesApiClient.deleteNotificationToken(new ReqDeviceToken(TEMP_TOKEN));
        } catch (HttpStatusCodeException e) {
            fail(e.getResponseBodyAsString());
        }
        assertThat(resCommon, is(notNullValue()));

    }

    @Ignore
    @Test
    public void testSubscribeStateNotification() throws Exception {
        ResAccountInfo resCommon = null;
        try {
            resCommon = accountDevicesApiClient.subscribeStateNotification(new ReqSubscibeToken(TEMP_TOKEN, true));
        } catch (HttpStatusCodeException e) {
            fail(e.getResponseBodyAsString());
        }

        assertThat(resCommon, is(notNullValue()));

        ResAccountInfo.UserDevice tokenDevice = null;
        for (ResAccountInfo.UserDevice userDevice : resCommon.getDevices()) {
            if (userDevice.getToken().equals(TEMP_TOKEN)) {
                tokenDevice = userDevice;
                break;
            }
        }
        ;

        assertThat(tokenDevice.isSubscribe(), is(equalTo(true)));

    }

    @Ignore
    @Test
    public void testGetNotificationBadge() throws Exception {

    }
}
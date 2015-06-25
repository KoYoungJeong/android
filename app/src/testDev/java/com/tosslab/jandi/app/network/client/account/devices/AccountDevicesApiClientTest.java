package com.tosslab.jandi.app.network.client.account.devices;

import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqDeviceToken;
import com.tosslab.jandi.app.network.models.ReqNotificationRegister;
import com.tosslab.jandi.app.network.models.ReqSubscibeToken;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.shadows.ShadowLog;

import retrofit.RetrofitError;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(RobolectricGradleTestRunner.class)
public class AccountDevicesApiClientTest {

    public static final String TEMP_TOKEN = "aaa";
    private ResLeftSideMenu sideMenu;

    @Before
    public void setUp() throws Exception {

        sideMenu = getSideMenu();

        Robolectric.getFakeHttpLayer().interceptHttpRequests(false);

        System.setProperty("robolectric.logging", "stdout");
        ShadowLog.stream = System.out;

    }

    private ResLeftSideMenu getSideMenu() {
        ResLeftSideMenu infosForSideMenu = RequestApiManager.getInstance().getInfosForSideMenuByMainRest(279);
        return infosForSideMenu;
    }

    @Ignore
    @Test
    public void testRegisterNotificationToken() throws Exception {
        ReqNotificationRegister reqNotiricationRegister = new ReqNotificationRegister("android", TEMP_TOKEN);

        ResAccountInfo resCommon = null;
        try {
            resCommon = RequestApiManager.getInstance().registerNotificationTokenByAccountDeviceApi(reqNotiricationRegister);
        } catch (RetrofitError e) {
            fail(e.getResponse().getBody().toString());
        }

        assertThat(resCommon, is(notNullValue()));
    }

    @Ignore
    @Test
    public void testDeleteNotificationToken() throws Exception {

        ResAccountInfo resCommon = null;
        try {
            resCommon = RequestApiManager.getInstance().deleteNotificationTokenByAccountDeviceApi(new ReqDeviceToken(TEMP_TOKEN));
        } catch (RetrofitError e) {
            fail(e.getResponse().getBody().toString());
        }
        assertThat(resCommon, is(notNullValue()));

    }

    @Ignore
    @Test
    public void testSubscribeStateNotification() throws Exception {
        ResAccountInfo resCommon = null;
        try {
            resCommon = RequestApiManager.getInstance().subscribeStateNotificationByAccountDeviceApi(new ReqSubscibeToken(TEMP_TOKEN, true));
        } catch (RetrofitError e) {
            fail(e.getResponse().getBody().toString());
        }

        assertThat(resCommon, is(notNullValue()));

        ResAccountInfo.UserDevice tokenDevice = null;
        for (ResAccountInfo.UserDevice userDevice : resCommon.getDevices()) {
            if (userDevice.getToken().equals(TEMP_TOKEN)) {
                tokenDevice = userDevice;
                break;
            }
        }

        assertThat(tokenDevice.isSubscribe(), is(equalTo(true)));

    }

    @Ignore
    @Test
    public void testGetNotificationBadge() throws Exception {

    }
}
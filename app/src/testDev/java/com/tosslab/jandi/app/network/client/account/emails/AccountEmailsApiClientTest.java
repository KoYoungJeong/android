package com.tosslab.jandi.app.network.client.account.emails;

import android.text.TextUtils;

import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ReqConfirmEmail;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.JandiRobolectricGradleTestRunner;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.shadows.httpclient.FakeHttp;
import org.springframework.web.client.HttpStatusCodeException;

import retrofit.RetrofitError;

import static junit.framework.Assert.fail;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(JandiRobolectricGradleTestRunner.class)
public class AccountEmailsApiClientTest {

    public static final String TEMP_TOKEN = "aaa";
    public static final String SAMPLE_EMAIL = "jsuch2362@naver.com";
    private ResLeftSideMenu sideMenu;

    @Before
    public void setUp() throws Exception {

        sideMenu = getSideMenu();

        FakeHttp.getFakeHttpLayer().interceptHttpRequests(false);

        System.setProperty("robolectric.logging", "stdout");
        ShadowLog.stream = System.out;

    }

    private ResLeftSideMenu getSideMenu() {
        ResLeftSideMenu infosForSideMenu = RequestApiManager.getInstance().getInfosForSideMenuByMainRest(279);
        return infosForSideMenu;
    }

    @Ignore
    @Test
    public void testRequestAddEmail() throws Exception {
        ResAccountInfo resAccountInfo = null;
        try {
            resAccountInfo = RequestApiManager.getInstance().requestAddEmailByAccountEmailApi(new ReqAccountEmail(SAMPLE_EMAIL));

        } catch (HttpStatusCodeException e) {
            fail(e.getResponseBodyAsString());
        }

        assertThat(resAccountInfo, is(notNullValue()));
    }

    @Ignore
    @Test
    public void testConfirmEmail() throws Exception {

        ResAccountInfo resAccountInfo = RequestApiManager.getInstance().confirmEmailByAccountEmailApi(new ReqConfirmEmail(SAMPLE_EMAIL, "5aec5981-5aeb-4391-8981-36f4f5373175"));

        assertThat(resAccountInfo, is(notNullValue()));

        ResAccountInfo.UserEmail tempEmail = null;
        for (ResAccountInfo.UserEmail userEmail : resAccountInfo.getEmails()) {

            if (TextUtils.equals(userEmail.getId(), SAMPLE_EMAIL)) {
                tempEmail = userEmail;
            }
        }

        assertThat(tempEmail.getId(), is(equalTo(SAMPLE_EMAIL)));

    }

    @Ignore
    @Test
    public void testDeleteEmail() throws Exception {

        ResAccountInfo resAccountInfo = null;
        try {
            resAccountInfo = RequestApiManager.getInstance().deleteEmailByAccountEmailApi(new ReqAccountEmail(SAMPLE_EMAIL));
        } catch (RetrofitError e) {
            fail(e.getResponse().getBody().toString());
        }

        assertThat(resAccountInfo, is(notNullValue()));

    }
}
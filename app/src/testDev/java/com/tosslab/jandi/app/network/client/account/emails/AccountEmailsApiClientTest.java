package com.tosslab.jandi.app.network.client.account.emails;

import android.text.TextUtils;

import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ReqConfirmEmail;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.shadows.ShadowLog;
import org.springframework.web.client.HttpStatusCodeException;

import static junit.framework.Assert.fail;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
public class AccountEmailsApiClientTest {

    public static final String TEMP_TOKEN = "aaa";
    public static final String SAMPLE_EMAIL = "jsuch2362@naver.com";
    //    private JandiRestClient jandiRestClient_;
    private ResLeftSideMenu sideMenu;
//    private AccountEmailsApiClient accountEmailsApiClient;

    @Before
    public void setUp() throws Exception {

//        jandiRestClient_ = new JandiRestClient_(Robolectric.application);
//        accountEmailsApiClient = new AccountEmailsApiClient_(Robolectric.application);
        ResAccessToken accessToken = getAccessToken();

//        jandiRestClient_.setAuthentication(new JandiV2HttpAuthentication(accessToken.getTokenType(), accessToken.getAccessToken()));
//        accountEmailsApiClient.setAuthentication(new JandiV2HttpAuthentication(accessToken.getTokenType(), accessToken.getAccessToken()));

        sideMenu = getSideMenu();

        Robolectric.getFakeHttpLayer().interceptHttpRequests(false);

        System.setProperty("robolectric.logging", "stdout");
        ShadowLog.stream = System.out;

    }

    private ResLeftSideMenu getSideMenu() {
//        ResLeftSideMenu infosForSideMenu = jandiRestClient_.getInfosForSideMenu(279);
        ResLeftSideMenu infosForSideMenu = RequestApiManager.getInstance().getInfosForSideMenuByMainRest(279);
        return infosForSideMenu;
    }

    private ResAccessToken getAccessToken() {

//        jandiRestClient_.setHeader("Content-Type", "application/json");

        ResAccessToken accessToken = null;
        try {
//            accessToken = jandiRestClient_.getAccessToken(ReqAccessToken.createPasswordReqToken(BaseInitUtil.TEST_ID, BaseInitUtil.TEST_PASSWORD));
            accessToken = RequestApiManager.getInstance().getAccessTokenByMainRest(ReqAccessToken.createPasswordReqToken(BaseInitUtil.TEST_ID, BaseInitUtil.TEST_PASSWORD));
        } catch (HttpStatusCodeException e) {
            System.out.println(e.getResponseBodyAsString());
            e.printStackTrace();
        }
        System.out.println("========= Get Access Token =========");
        return accessToken;
    }


    @Ignore
    @Test
    public void testRequestAddEmail() throws Exception {
        ResAccountInfo resAccountInfo = null;
        try {
//            resAccountInfo = accountEmailsApiClient.requestAddEmail(new ReqAccountEmail(SAMPLE_EMAIL));
            resAccountInfo = RequestApiManager.getInstance().requestAddEmailByAccountEmailApi(new ReqAccountEmail(SAMPLE_EMAIL));

        } catch (HttpStatusCodeException e) {
            fail(e.getResponseBodyAsString());
        }

        assertThat(resAccountInfo, is(notNullValue()));
    }

    @Ignore
    @Test
    public void testConfirmEmail() throws Exception {

//        ResAccountInfo resAccountInfo = accountEmailsApiClient.confirmEmail(new ReqConfirmEmail(SAMPLE_EMAIL, "5aec5981-5aeb-4391-8981-36f4f5373175"));
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

//            accountEmailsApiClient.setHeader("Accept", JandiV2HttpMessageConverter.APPLICATION_VERSION_FULL_NAME + ", application/json");
//            resAccountInfo = accountEmailsApiClient.deleteEmail(new ReqAccountEmail(SAMPLE_EMAIL));
            resAccountInfo = RequestApiManager.getInstance().deleteEmailByAccountEmailApi(new ReqAccountEmail(SAMPLE_EMAIL));
        } catch (HttpStatusCodeException e) {
            fail(e.getResponseBodyAsString());
        }

        assertThat(resAccountInfo, is(notNullValue()));

    }
}
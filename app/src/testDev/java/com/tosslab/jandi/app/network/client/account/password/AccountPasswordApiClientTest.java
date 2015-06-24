package com.tosslab.jandi.app.network.client.account.password;

import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ReqChangePassword;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.utils.LanguageUtil;

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
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
public class AccountPasswordApiClientTest {

    public static final String TEMP_TOKEN = "aaa";
    public static final String SAMPLE_EMAIL = "jsuch2362@naver.com";
    //    private JandiRestClient jandiRestClient_;
    private ResLeftSideMenu sideMenu;
//    private AccountPasswordApiClient accountPasswordApiClient;

    @Before
    public void setUp() throws Exception {

//        jandiRestClient_ = new JandiRestClient_(Robolectric.application);
//        accountPasswordApiClient = new AccountPasswordApiClient_(Robolectric.application);
        ResAccessToken accessToken = getAccessToken();

//        jandiRestClient_.setAuthentication(new JandiV2HttpAuthentication(accessToken.getTokenType(), accessToken.getAccessToken()));
//        accountPasswordApiClient.setAuthentication(new JandiV2HttpAuthentication(accessToken.getTokenType(), accessToken.getAccessToken()));

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

//        ResAccessToken accessToken = jandiRestClient_.getAccessToken(ReqAccessToken.createPasswordReqToken(BaseInitUtil.TEST_ID, BaseInitUtil.TEST_PASSWORD));
        ResAccessToken accessToken = RequestApiManager.getInstance().getAccessTokenByMainRest(ReqAccessToken.createPasswordReqToken(BaseInitUtil.TEST_ID, BaseInitUtil.TEST_PASSWORD));
        System.out.println("========= Get Access Token =========");
        return accessToken;
    }


    @Ignore
    @Test
    public void testResetPassword() throws Exception {

        ResCommon resCommon = null;
        try {
            resCommon = RequestApiManager.getInstance().resetPasswordByAccountPasswordApi(new ReqAccountEmail(BaseInitUtil.TEST_ID, LanguageUtil.getLanguage(Robolectric.application)));
//            resCommon = accountPasswordApiClient.resetPassword(new ReqAccountEmail(BaseInitUtil.TEST_ID, LanguageUtil.getLanguage(Robolectric.application)));
        } catch (HttpStatusCodeException e) {
            fail(e.getResponseBodyAsString());
        }

        assertThat(resCommon, is(notNullValue()));

    }

    @Ignore
    @Test
    public void testChangePassword() throws Exception {
        ResCommon resCommon = null;
//        https://www.jandi.com/app/#/passwordReset?token=ffaa9b67-9d33-4b8c-99f1-40082ddbda0d
        try {
//            resCommon = accountPasswordApiClient.changePassword(new ReqChangePassword("ffaa9b67-9d33-4b8c-99f1-40082ddbda0d", "1234"));
            resCommon = RequestApiManager.getInstance().changePasswordByAccountPasswordApi(new ReqChangePassword("ffaa9b67-9d33-4b8c-99f1-40082ddbda0d", "1234"));
        } catch (HttpStatusCodeException e) {
            fail(e.getResponseBodyAsString());
        }

        assertThat(resCommon, is(notNullValue()));
    }
}
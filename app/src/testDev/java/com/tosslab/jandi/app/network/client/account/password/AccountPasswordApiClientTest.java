package com.tosslab.jandi.app.network.client.account.password;

import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ReqChangePassword;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.utils.LanguageUtil;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.JandiRobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.shadows.httpclient.FakeHttp;

import retrofit.RetrofitError;

import static junit.framework.Assert.fail;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(JandiRobolectricGradleTestRunner.class)
public class AccountPasswordApiClientTest {

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
    public void testResetPassword() throws Exception {

        ResCommon resCommon = null;
        try {
            resCommon = RequestApiManager.getInstance().resetPasswordByAccountPasswordApi(new ReqAccountEmail(BaseInitUtil.TEST_ID, LanguageUtil.getLanguage()));
        } catch (RetrofitError e) {
            fail(e.getResponse().getBody().toString());
        }

        assertThat(resCommon, is(notNullValue()));

    }

    @Ignore
    @Test
    public void testChangePassword() throws Exception {
        ResCommon resCommon = null;
        try {
            resCommon = RequestApiManager.getInstance().changePasswordByAccountPasswordApi(new ReqChangePassword("ffaa9b67-9d33-4b8c-99f1-40082ddbda0d", "1234"));
        } catch (RetrofitError e) {
            fail(e.getResponse().getBody().toString());
        }

        assertThat(resCommon, is(notNullValue()));
    }
}
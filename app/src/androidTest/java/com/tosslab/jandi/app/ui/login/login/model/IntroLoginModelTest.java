package com.tosslab.jandi.app.ui.login.login.model;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.repositories.AccessTokenRepository;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.utils.TokenUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import retrofit.RetrofitError;
import setup.BaseInitUtil;

import static junit.framework.Assert.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Created by jsuch2362 on 15. 11. 10..
 */
@RunWith(AndroidJUnit4.class)
public class IntroLoginModelTest {

    @Rule
    public ActivityTestRule<BaseAppCompatActivity> rule = new ActivityTestRule<BaseAppCompatActivity>(BaseAppCompatActivity.class);
    private IntroLoginModel introLoginModel;

    @Before
    public void setUp() throws Exception {
        introLoginModel = IntroLoginModel_.getInstance_(JandiApplication.getContext());
    }

    @After
    public void tearDown() throws Exception {
        BaseInitUtil.clear();
    }

    @Test
    public void testLogin_Wrong_ID() throws Exception {
        String wrongId = "steve1@tosslab.com";
        String wrongPw = "dnrl~12AB1";

        try {
            introLoginModel.login(wrongId, wrongPw);
            fail("성공하면 안되는 경우임 : 잘못된 ID 로 접근");

        } catch (RetrofitError retrofitError) {
            retrofitError.printStackTrace();
            assertThat(retrofitError.getKind(), is(equalTo(RetrofitError.Kind.HTTP)));
        }
    }

    @Test
    public void testLogin_Right_ID_Wrong_PW() throws Exception {
        String rightId = BaseInitUtil.TEST_EMAIL;
        String wrongPw = BaseInitUtil.TEST_PASSWORD + "dnrl~12AB1";

        try {
            introLoginModel.login(rightId, wrongPw);
            fail("성공하면 안되는 경우임 : 잘못된 PW 로 접근");
        } catch (RetrofitError retrofitError) {
            retrofitError.printStackTrace();
            assertThat(retrofitError.getKind(), is(equalTo(RetrofitError.Kind.HTTP)));
        }
    }

    @Test
    public void testLogin_Right_Id_Right_Pw() throws Exception {
        String rightId = BaseInitUtil.TEST_EMAIL;
        String rightPw = BaseInitUtil.TEST_PASSWORD;
        try {
            ResAccessToken login = introLoginModel.login(rightId, rightPw);
            assertThat(login, is(notNullValue()));
            assertThat(login.getAccessToken(), is(notNullValue()));
            assertThat(login.getExpireTime(), is(notNullValue()));
            assertThat(login.getRefreshToken(), is(notNullValue()));
            assertThat(login.getTokenType(), is(notNullValue()));
            assertThat(login.getTokenType(), is(equalTo("bearer")));

        } catch (RetrofitError retrofitError) {
            retrofitError.printStackTrace();
            fail("실패할리가.... : 올바른 ID, PW 사용");
        }
    }

    @Test
    public void testSaveTokenInfo() throws Exception {

        ResAccessToken accessToken = introLoginModel.login(BaseInitUtil.TEST_EMAIL, BaseInitUtil.TEST_PASSWORD);

        boolean isSaved = introLoginModel.saveTokenInfo(accessToken);
        assertThat(isSaved, is(true));

        ResAccessToken savedToken = AccessTokenRepository.getRepository().getAccessToken();

        assertThat(savedToken, is(notNullValue()));
        assertThat(accessToken.getAccessToken(), is(equalTo(savedToken.getAccessToken())));
        assertThat(accessToken.getExpireTime(), is(equalTo(savedToken.getExpireTime())));
        assertThat(accessToken.getRefreshToken(), is(equalTo(savedToken.getRefreshToken())));
        assertThat(accessToken.getTokenType(), is(equalTo(savedToken.getTokenType())));
    }

    @Test
    public void testSaveAccountInfo() throws Exception {
        ResAccessToken accessToken = RequestApiManager.getInstance().getAccessTokenByMainRest(
                ReqAccessToken.createPasswordReqToken(BaseInitUtil.TEST_EMAIL, BaseInitUtil.TEST_PASSWORD));

        TokenUtil.saveTokenInfoByPassword(accessToken);
        ResAccountInfo accountInfo = RequestApiManager.getInstance().getAccountInfoByMainRest();
        boolean isSaved = introLoginModel.saveAccountInfo(accountInfo);
        assertThat(isSaved, is(true));


        isSaved = introLoginModel.saveAccountInfo(null);
        assertThat(isSaved, is(false));
    }

    @Test
    public void testGetAccountInfo_No_Token() throws Exception {
        try {
            ResAccountInfo accountInfo = introLoginModel.getAccountInfo();
            fail("토큰 정보 없는 상태이므로 성공하면 안됨");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetAccountInfo_Has_Token() throws Exception {

        ResAccessToken accessToken = RequestApiManager.getInstance().getAccessTokenByMainRest(
                ReqAccessToken.createPasswordReqToken(BaseInitUtil.TEST_EMAIL, BaseInitUtil.TEST_PASSWORD));

        TokenUtil.saveTokenInfoByPassword(accessToken);

        ResAccountInfo accountInfo = introLoginModel.getAccountInfo();
        assertThat(accountInfo, is(notNullValue()));

    }

    @Test
    public void testIsValidEmailFormat() throws Exception {

        {
            boolean valid = introLoginModel.isValidEmailFormat("steve");
            assertThat(valid, is(false));
        }
        {
            boolean valid = introLoginModel.isValidEmailFormat("steve@");
            assertThat(valid, is(false));
        }
        {
            boolean valid = introLoginModel.isValidEmailFormat("steve@a");
            assertThat(valid, is(false));
        }
        {
            boolean valid = introLoginModel.isValidEmailFormat("steve@a");
            assertThat(valid, is(false));
        }
        {
            boolean valid = introLoginModel.isValidEmailFormat("1steve");
            assertThat(valid, is(false));
        }
        {
            boolean valid = introLoginModel.isValidEmailFormat("1steve@");
            assertThat(valid, is(false));
        }
        {
            boolean valid = introLoginModel.isValidEmailFormat("1steve@a");
            assertThat(valid, is(false));
        }
        {
            boolean valid = introLoginModel.isValidEmailFormat("1steve@a.com");
            assertThat(valid, is(true));
        }
        {
            boolean valid = introLoginModel.isValidEmailFormat("steve@a.com");
            assertThat(valid, is(true));
        }
    }

    @Test
    public void testRequestPasswordReset_Wrong_Id() throws Exception {
        try {
            introLoginModel.requestPasswordReset("steve1@tosslab.com");
            fail("없는 아이디는 정상 요청이 되면 안됩니다");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testRequestPasswordReset() throws Exception {
        ResCommon resCommon = introLoginModel.requestPasswordReset(BaseInitUtil.TEST_EMAIL);
        assertThat(resCommon, is(notNullValue()));
    }
}
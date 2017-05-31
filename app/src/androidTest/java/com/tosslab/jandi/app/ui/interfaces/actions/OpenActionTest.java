package com.tosslab.jandi.app.ui.interfaces.actions;

import android.net.Uri;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.network.client.main.LoginApi;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.InnerApiRetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.TokenUtil;

import org.assertj.core.api.Assertions;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import setup.BaseInitUtil;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Ignore
@RunWith(AndroidJUnit4.class)
public class OpenActionTest {

    private static final String ACCESS_1 = "access_1";
    private static final String REFRESH_1 = "refresh_1";
    private static final String TOKEN_TYPE_1 = "tokenType_1";
    @Rule
    public IntentsTestRule<BaseAppCompatActivity> rule = new IntentsTestRule<>(BaseAppCompatActivity.class);
    private OpenAction action;

    @AfterClass
    public static void tearDownClass() throws Exception {
        BaseInitUtil.releaseDatabase();
    }

    @Before
    public void setUp() throws Throwable {
        BaseInitUtil.clear();
        rule.runOnUiThread(() -> action = new OpenAction(rule.getActivity()));
        action.progressWheel = mock(ProgressWheel.class);
        when(action.progressWheel.isShowing()).thenReturn(true, true, true, true);

        ResAccessToken accessToken = new ResAccessToken();
        accessToken.setAccessToken(ACCESS_1);
        accessToken.setRefreshToken(REFRESH_1);
        accessToken.setTokenType(TOKEN_TYPE_1);

        TokenUtil.saveTokenInfoByPassword(accessToken);

    }

    private boolean[] getFinished() {
        final boolean[] finish = {false};
        doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(action.progressWheel).dismiss();

        return finish;
    }

    @Test
    public void testRemoveSpecialChar() throws Exception {
        String origin = "akljdfhlkajshdflk";
        String replace = action.removeSpecialChar(origin + "_)(*&^%$#@!~`[]{}';:\"/.,<>?_=+\\|");

        Assertions.assertThat(replace)
                .isNotNull()
                .isNotEmpty()
                .isEqualTo(origin);
        String origin2 = "1!2@3#4$5%6^7&8*9(0)_";
        String replace2 = action.removeSpecialChar(origin2);
        Assertions.assertThat(replace2)
                .isNotNull()
                .isNotEmpty()
                .isEqualTo("1234567890");
    }

    @Test
    public void testExecute_No_QueryParams() throws Exception {

        boolean[] finished = getFinished();

        action.execute(Uri.parse("tosslab://open?"));

        await().until(() -> finished[0]);

        ResAccessToken accessToken = TokenUtil.getTokenObject();
        assertThat(accessToken, is(notNullValue()));
        assertThat(accessToken.getAccessToken(), is(ACCESS_1));
        assertThat(accessToken.getRefreshToken(), is(REFRESH_1));
        assertThat(accessToken.getTokenType(), is(TOKEN_TYPE_1));
    }

    @Test
    public void testExecute_Null_Uri() throws Exception {

        action.execute(null);

        ResAccessToken accessToken = TokenUtil.getTokenObject();
        assertThat(accessToken, is(notNullValue()));
        assertThat(accessToken.getAccessToken(), is(ACCESS_1));
        assertThat(accessToken.getRefreshToken(), is(REFRESH_1));
        assertThat(accessToken.getTokenType(), is(TOKEN_TYPE_1));
    }

    @Test
    public void testExecute_Wrong_QueryParams() throws Exception {


        boolean[] finished = getFinished();

        String newAccessToken = "abc";
        String newRefreshToken = "def";
        action.execute(Uri.parse(String.format("tosslab://open?access_token=%1s&refresh_token=%2s", newAccessToken, newRefreshToken)));

        await().until(() -> finished[0]);

        ResAccessToken accessToken = TokenUtil.getTokenObject();
        assertThat(accessToken, is(notNullValue()));
        assertThat(accessToken.getAccessToken().length(), is(equalTo(0)));
        assertThat(accessToken.getRefreshToken().length(), is(equalTo(0)));
        assertThat(accessToken.getTokenType().length(), is(equalTo(0)));

    }

    @Test
    public void testExecute_Right_QueryParams() throws Exception {

        boolean[] finished = getFinished();

        ResAccessToken accessToken = new LoginApi(InnerApiRetrofitBuilder.getInstance()).getAccessToken(
                ReqAccessToken.createPasswordReqToken(BaseInitUtil.TEST1_EMAIL, BaseInitUtil.TEST_PASSWORD));


        String newAccessToken = accessToken.getAccessToken();
        String newRefreshToken = accessToken.getRefreshToken();
        action.execute(Uri.parse(String.format("tosslab://open?access_token=%1s&refresh_token=%2s", newAccessToken, newRefreshToken)));
        await().until(() -> finished[0]);

        ResAccessToken newToken = TokenUtil.getTokenObject();
        assertThat(newToken, is(notNullValue()));
        assertThat(newToken.getAccessToken(), is(equalTo(accessToken.getAccessToken())));
        assertThat(newToken.getRefreshToken(), is(equalTo(accessToken.getRefreshToken())));
        assertThat(newToken.getTokenType(), is("bearer"));
    }

}

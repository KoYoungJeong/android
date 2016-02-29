package com.tosslab.jandi.app.ui.interfaces.actions;

import android.net.Uri;
import android.support.test.espresso.intent.Intents;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.local.orm.repositories.AccessTokenRepository;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.utils.ProgressWheel;

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

@RunWith(AndroidJUnit4.class)
public class OpenActionTest {

    private static final String ACCESS_1 = "access_1";
    private static final String REFRESH_1 = "refresh_1";
    private static final String TOKEN_TYPE_1 = "tokenType_1";
    @Rule
    public ActivityTestRule<BaseAppCompatActivity> rule = new ActivityTestRule<>(BaseAppCompatActivity.class);
    private OpenAction action;

    @Before
    public void setUp() throws Throwable {
        BaseInitUtil.clear();
        rule.runOnUiThread(() -> action = OpenAction_.getInstance_(rule.getActivity()));
        action.progressWheel = mock(ProgressWheel.class);
        when(action.progressWheel.isShowing()).thenReturn(true, true, true, true);

        ResAccessToken accessToken = new ResAccessToken();
        accessToken.setAccessToken(ACCESS_1);
        accessToken.setRefreshToken(REFRESH_1);
        accessToken.setTokenType(TOKEN_TYPE_1);

        AccessTokenRepository.getRepository().upsertAccessToken(accessToken);

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
    public void testExecute_No_QueryParams() throws Exception {

        boolean[] finished = getFinished();

        action.execute(Uri.parse("tosslab://open?"));

        await().until(() -> finished[0]);

        ResAccessToken accessToken = AccessTokenRepository.getRepository().getAccessToken();
        assertThat(accessToken, is(notNullValue()));
        assertThat(accessToken.getAccessToken(), is(ACCESS_1));
        assertThat(accessToken.getRefreshToken(), is(REFRESH_1));
        assertThat(accessToken.getTokenType(), is(TOKEN_TYPE_1));
    }

    @Test
    public void testExecute_Null_Uri() throws Exception {

        action.execute(null);

        ResAccessToken accessToken = AccessTokenRepository.getRepository().getAccessToken();
        assertThat(accessToken, is(notNullValue()));
        assertThat(accessToken.getAccessToken(), is(ACCESS_1));
        assertThat(accessToken.getRefreshToken(), is(REFRESH_1));
        assertThat(accessToken.getTokenType(), is(TOKEN_TYPE_1));
    }

    @Ignore
    @Test
    public void testExecute_Wrong_QueryParams() throws Exception {

        Intents.init();

        boolean[] finished = getFinished();

        String newAccessToken = "abc";
        String newRefreshToken = "def";
        action.execute(Uri.parse(String.format("tosslab://open?access_token=%1s&refresh_token=%2s", newAccessToken, newRefreshToken)));

        await().until(() -> finished[0]);

        ResAccessToken accessToken = AccessTokenRepository.getRepository().getAccessToken();
        assertThat(accessToken, is(notNullValue()));
        assertThat(accessToken.getAccessToken().length(), is(equalTo(0)));
        assertThat(accessToken.getRefreshToken().length(), is(equalTo(0)));
        assertThat(accessToken.getTokenType().length(), is(equalTo(0)));

        Intents.release();
    }

    @Test
    public void testExecute_Right_QueryParams() throws Exception {

        boolean[] finished = getFinished();

        ResAccessToken accessToken = RequestApiManager.getInstance().getAccessTokenByMainRest(
                ReqAccessToken.createPasswordReqToken(BaseInitUtil.TEST1_EMAIL, BaseInitUtil.TEST_PASSWORD));


        String newAccessToken = accessToken.getAccessToken();
        String newRefreshToken = accessToken.getRefreshToken();
        action.execute(Uri.parse(String.format("tosslab://open?access_token=%1s&refresh_token=%2s", newAccessToken, newRefreshToken)));
        await().until(() -> finished[0]);

        ResAccessToken newToken = AccessTokenRepository.getRepository().getAccessToken();
        assertThat(newToken, is(notNullValue()));
        assertThat(newToken.getAccessToken(), is(equalTo(accessToken.getAccessToken())));
        assertThat(newToken.getRefreshToken(), is(equalTo(accessToken.getRefreshToken())));
        assertThat(newToken.getTokenType(), is("bearer"));
    }

}
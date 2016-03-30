package com.tosslab.jandi.app.network.client.main;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.local.orm.repositories.AccessTokenRepository;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitAdapterBuilder;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ResAccessToken;

import org.junit.Test;
import org.junit.runner.RunWith;

import setup.BaseInitUtil;

import static android.support.test.espresso.core.deps.guava.collect.Range.greaterThan;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class LoginApiTest {


    @Test
    public void testGetAccessToken() throws Exception {
        LoginApi loginApi = new LoginApi(RetrofitAdapterBuilder.newInstance());
        {
            ResAccessToken accessToken = loginApi
                    .getAccessToken(ReqAccessToken.createPasswordReqToken(BaseInitUtil.TEST1_EMAIL, BaseInitUtil.TEST_PASSWORD));

            assertThat(accessToken, is(notNullValue()));
            assertThat(accessToken.getRefreshToken().length(), is(greaterThan(0)));
            assertThat(accessToken.getAccessToken().length(), is(greaterThan(0)));

            AccessTokenRepository.getRepository().upsertAccessToken(accessToken);
        }

        {
            ResAccessToken oldAccessToken = AccessTokenRepository.getRepository().getAccessToken();
            ResAccessToken newAccessToken = loginApi.getAccessToken(ReqAccessToken.createRefreshReqToken(oldAccessToken.getRefreshToken()));

            assertThat(oldAccessToken.getRefreshToken(), is(equalTo(newAccessToken.getRefreshToken())));
        }


    }

}
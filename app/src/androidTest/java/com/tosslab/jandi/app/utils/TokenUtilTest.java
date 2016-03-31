package com.tosslab.jandi.app.utils;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.local.orm.repositories.AccessTokenRepository;
import com.tosslab.jandi.app.network.models.ResAccessToken;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

@RunWith(AndroidJUnit4.class)
public class TokenUtilTest {

    @Before
    public void setUp() throws Exception {
        AccessTokenRepository.getRepository().deleteAccessToken();
        TokenUtil.refresh();

    }

    @Test
    public void testToken() throws Exception {

        {
            ResAccessToken newToken = new ResAccessToken();
            newToken.setRefreshToken("refresh_token");
            newToken.setAccessToken("access_token");
            newToken.setTokenType("token_type");
            boolean result = TokenUtil.saveTokenInfoByPassword(newToken);
            assertThat(result, is(true));

            ResAccessToken savedToken = TokenUtil.getTokenObject();
            assertThat(savedToken.getRefreshToken(), is(equalTo("refresh_token")));
            assertThat(savedToken.getAccessToken(), is(equalTo("access_token")));
            assertThat(savedToken.getTokenType(), is(equalTo("token_type")));
        }


        {
            ResAccessToken newToken = new ResAccessToken();
            newToken.setRefreshToken("refresh_token1");
            newToken.setAccessToken("access_token1");
            newToken.setTokenType("token_type1");
            boolean result = TokenUtil.saveTokenInfoByRefresh(newToken);
            assertThat(result, is(true));

            ResAccessToken savedToken = TokenUtil.getTokenObject();
            assertThat(savedToken.getRefreshToken(), is(equalTo("refresh_token1")));
            assertThat(savedToken.getAccessToken(), is(equalTo("access_token1")));
            assertThat(savedToken.getTokenType(), is(equalTo("token_type1")));
        }

        {
            assertThat(TokenUtil.getRefreshToken(), is(equalTo("refresh_token1")));
            assertThat(TokenUtil.getAccessToken(), is(equalTo("access_token1")));
            assertThat(TokenUtil.getRequestAuthentication(), is(equalTo("token_type1 access_token1")));
        }

        {
            ResAccessToken newToken = new ResAccessToken();
            newToken.setRefreshToken("refresh_token2");
            newToken.setAccessToken("access_token2");
            newToken.setTokenType("token_type2");
            AccessTokenRepository.getRepository().upsertAccessToken(newToken);

            TokenUtil.refresh();

            assertThat(TokenUtil.getRefreshToken(), is(equalTo("refresh_token2")));
            assertThat(TokenUtil.getAccessToken(), is(equalTo("access_token2")));
            assertThat(TokenUtil.getRequestAuthentication(), is(equalTo("token_type2 access_token2")));

        }

        {
            TokenUtil.clearTokenInfo();
            assertThat(TokenUtil.getRefreshToken().length(), is(equalTo(0)));
            assertThat(TokenUtil.getAccessToken().length(), is(equalTo(0)));
            assertThat(TokenUtil.getRequestAuthentication(), is(equalTo(" ")));

            ResAccessToken savedToken = AccessTokenRepository.getRepository().getAccessToken();
            assertThat(savedToken.getRefreshToken().length(), is(equalTo(0)));
            assertThat(savedToken.getAccessToken().length(), is(equalTo(0)));
            assertThat(savedToken.getTokenType().length(), is(equalTo(0)));

        }
    }

}
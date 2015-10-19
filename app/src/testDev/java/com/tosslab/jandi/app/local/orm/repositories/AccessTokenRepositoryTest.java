package com.tosslab.jandi.app.local.orm.repositories;

import com.tosslab.jandi.app.network.models.ResAccessToken;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.JandiRobolectricGradleTestRunner;

import java.util.Date;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Created by jsuch2362 on 2015. 10. 7..
 */
@RunWith(JandiRobolectricGradleTestRunner.class)
public class AccessTokenRepositoryTest {
    @After
    public void tearDown() throws Exception {
        BaseInitUtil.releaseDatabase();
    }

    @Test
    public void testRepository() throws Exception {

        AccessTokenRepository repository = AccessTokenRepository.getRepository();

        {
            ResAccessToken accessToken = repository.getAccessToken();
            assertThat(accessToken, is(notNullValue()));
            assertThat(accessToken.getExpireTime(), is(equalTo("")));
            assertThat(accessToken.getRefreshToken(), is(equalTo("")));
            assertThat(accessToken.getAccessToken(), is(equalTo("")));
            assertThat(accessToken.getTokenType(), is(equalTo("")));
        }

        {
            ResAccessToken accessToken = new ResAccessToken();
            accessToken.setTokenType("type1");
            accessToken.setAccessToken("access1");
            accessToken.setRefreshToken("refresh1");
            accessToken.setExpireTime(new Date().toString());
            boolean upserted = repository.upsertAccessToken(accessToken);

            if (!upserted) {
                fail("Cannot be fail!!!!");
            }
        }

        {
            ResAccessToken savedAccess = repository.getAccessToken();
            assertThat(savedAccess.get_id(), is(greaterThan(0l)));
            assertThat(savedAccess.getAccessToken(), is(equalTo("access1")));
            assertThat(savedAccess.getTokenType(), is(equalTo("type1")));
            assertThat(savedAccess.getRefreshToken(), is(equalTo("refresh1")));
            assertThat(savedAccess.getExpireTime().length(), is(greaterThan(0)));
        }

        {
            ResAccessToken accessToken = new ResAccessToken();
            accessToken.setAccessToken("access2");
            accessToken.setExpireTime(new Date().toString());
            boolean upserted = repository.upsertAccessToken(accessToken);

            if (!upserted) {
                fail("Cannot be fail!!!!");
            }
        }

        {
            ResAccessToken savedAccess = repository.getAccessToken();
            assertThat(savedAccess.get_id(), is(greaterThan(0l)));
            assertThat(savedAccess.getAccessToken(), is(equalTo("access2")));
            assertThat(savedAccess.getTokenType(), is(equalTo("type1")));
            assertThat(savedAccess.getRefreshToken(), is(equalTo("refresh1")));
            assertThat(savedAccess.getExpireTime().length(), is(greaterThan(0)));
        }
    }
}
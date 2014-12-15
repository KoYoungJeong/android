package com.tosslab.jandi.app.network.client.settings.starred;

import com.tosslab.jandi.app.network.JandiRestClient;
import com.tosslab.jandi.app.network.JandiRestClient_;
import com.tosslab.jandi.app.network.spring.JandiV2HttpAuthentication;
import com.tosslab.jandi.app.network.client.publictopic.ChannelApiClient;
import com.tosslab.jandi.app.network.client.publictopic.ChannelApiClient_;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ReqTeam;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.shadows.ShadowLog;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(RobolectricGradleTestRunner.class)
public class StarredEntityApiClientTest {

    private JandiRestClient jandiRestClient_;
    private ChannelApiClient channelApiClient;
    private StarredEntityApiClient starredEntityApiClient;
    private ResLeftSideMenu sideMenu;

    @Before
    public void setUp() throws Exception {

        jandiRestClient_ = new JandiRestClient_(Robolectric.application);
        channelApiClient = new ChannelApiClient_(Robolectric.application);
        starredEntityApiClient = new StarredEntityApiClient_(Robolectric.application);
        ResAccessToken accessToken = getAccessToken();

        jandiRestClient_.setAuthentication(new JandiV2HttpAuthentication(accessToken.getTokenType(), accessToken.getAccessToken()));
        channelApiClient.setAuthentication(new JandiV2HttpAuthentication(accessToken.getTokenType(), accessToken.getAccessToken()));
        starredEntityApiClient.setAuthentication(new JandiV2HttpAuthentication(accessToken.getTokenType(), accessToken.getAccessToken()));

        sideMenu = getSideMenu();

        Robolectric.getFakeHttpLayer().interceptHttpRequests(false);

        System.setProperty("robolectric.logging", "stdout");
        ShadowLog.stream = System.out;

    }

    private ResLeftSideMenu getSideMenu() {
        ResLeftSideMenu infosForSideMenu = jandiRestClient_.getInfosForSideMenu(279);

        return infosForSideMenu;
    }

    private ResAccessToken getAccessToken() {

        jandiRestClient_.setHeader("Content-Type", "application/json");

        ResAccessToken accessToken = jandiRestClient_.getAccessToken(ReqAccessToken.createPasswordReqToken("mk@tosslab.com", "1234"));
        System.out.println("========= Get Access Token =========");
        return accessToken;
    }

    private ResLeftSideMenu.Channel getDefaultChannel() {
        ResLeftSideMenu.Channel entity = null;
        for (ResLeftSideMenu.Entity entity1 : sideMenu.joinEntities) {
            if (entity1 instanceof ResLeftSideMenu.Channel && entity1.id == sideMenu.team.t_defaultChannelId) {
                ResLeftSideMenu.Channel channel = (ResLeftSideMenu.Channel) entity1;
                entity = channel;
                break;
            }
        }
        return entity;
    }

    @Test
    public void testEnableFavorite() throws Exception {

        ResLeftSideMenu.Channel defaultChannel = getDefaultChannel();

        ResCommon resCommon = starredEntityApiClient.enableFavorite(new ReqTeam(sideMenu.team.id), defaultChannel.id);

        assertThat(resCommon, is(notNullValue()));

        ResLeftSideMenu sideMenu1 = getSideMenu();

        for (Integer u_starredEntity : sideMenu1.user.u_starredEntities) {
            if (u_starredEntity == defaultChannel.id) {
                return;
            }
        }

        fail("Cannot Find Favorite Entity");

    }

    @Test
    public void testDisableFavorite() throws Exception {
        ResLeftSideMenu.Channel defaultChannel = getDefaultChannel();

        ResCommon resCommon = starredEntityApiClient.disableFavorite(sideMenu.team.id, defaultChannel.id);

        assertThat(resCommon, is(notNullValue()));

        ResLeftSideMenu sideMenu1 = getSideMenu();

        for (Integer u_starredEntity : sideMenu1.user.u_starredEntities) {
            if (u_starredEntity == defaultChannel.id) {
                fail("Must be not Find Favorite Entity");
                return;
            }
        }

    }
}
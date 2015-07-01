package com.tosslab.jandi.app.network.client.settings.starred;

import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqTeam;
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


    private ResLeftSideMenu sideMenu;

    @Before
    public void setUp() throws Exception {

        sideMenu = getSideMenu();

        Robolectric.getFakeHttpLayer().interceptHttpRequests(false);

        System.setProperty("robolectric.logging", "stdout");
        ShadowLog.stream = System.out;

    }

    private ResLeftSideMenu getSideMenu() {
        ResLeftSideMenu infosForSideMenu = RequestApiManager.getInstance().getInfosForSideMenuByMainRest(279);

        return infosForSideMenu;
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

        ResCommon resCommon = RequestApiManager.getInstance().enableFavoriteByStarredEntityApi(new ReqTeam(sideMenu.team.id), defaultChannel.id);

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

        ResCommon resCommon = RequestApiManager.getInstance().disableFavoriteByStarredEntityApi(sideMenu.team.id, defaultChannel.id);

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
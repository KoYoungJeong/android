package com.tosslab.jandi.app.network.client.sticker;

import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.client.JandiEntityClient;
import com.tosslab.jandi.app.network.client.JandiEntityClient_;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.sticker.SendSticker;
import com.tosslab.jandi.app.utils.TokenUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;

import java.util.List;

import static org.junit.Assert.assertNotNull;

/**
 * Created by Steve SeongUg Jung on 15. 6. 8..
 */
@RunWith(RobolectricGradleTestRunner.class)
public class StickerApiClientTest {

    private StickerApiClient stickerApiClient;

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData(Robolectric.application);
        stickerApiClient = new StickerApiClient_(Robolectric.application);

        stickerApiClient.setAuthentication(TokenUtil.getRequestAuthentication(Robolectric.application));
    }

    @Test
    public void testSendSticker() throws Exception {

        List<ResAccountInfo.UserTeam> userTeams = JandiAccountDatabaseManager.getInstance(Robolectric.application).getUserTeams();
        int teamId = userTeams.get(0).getTeamId();
        JandiAccountDatabaseManager.getInstance(Robolectric.application).updateSelectedTeam(teamId);

        JandiEntityClient jandiEntityClient = JandiEntityClient_.getInstance_(Robolectric.application);
        ResLeftSideMenu totalEntitiesInfo = jandiEntityClient.getTotalEntitiesInfo();

        ResLeftSideMenu.Entity entity = totalEntitiesInfo.entities.get(0);

        String type;

        if (entity instanceof ResLeftSideMenu.Channel) {
            type = "channels";
        } else if (entity instanceof ResLeftSideMenu.PrivateGroup) {
            type = "privateGroups";
        } else {
            type = "users";
        }

        ResCommon resCommon = stickerApiClient.sendSticker(SendSticker.create("1", 100, teamId, entity.id, type, ""));

        assertNotNull(resCommon);
        resCommon = stickerApiClient.sendSticker(SendSticker.create("1", 100, teamId, entity.id, type, "test sticker with message"));
    }
}
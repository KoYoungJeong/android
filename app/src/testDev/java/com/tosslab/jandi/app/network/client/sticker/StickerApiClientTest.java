package com.tosslab.jandi.app.network.client.sticker;

import com.tosslab.jandi.app.local.database.sticker.JandiStickerDatabaseManager;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.client.EntityClientManager_;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqSearchFile;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResSearchFile;
import com.tosslab.jandi.app.network.models.sticker.ReqSendSticker;
import com.tosslab.jandi.app.network.models.sticker.ResSticker;

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

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData(Robolectric.application);
    }

    @Test
    public void testSendSticker() throws Exception {

        List<ResAccountInfo.UserTeam> userTeams = AccountRepository.getRepository().getAccountTeams();
        int teamId = userTeams.get(0).getTeamId();
        AccountRepository.getRepository().updateSelectedTeamInfo(teamId);

        EntityClientManager entityClientManager = EntityClientManager_.getInstance_(Robolectric.application);
        ResLeftSideMenu totalEntitiesInfo = entityClientManager.getTotalEntitiesInfo();

        ResLeftSideMenu.Entity entity = totalEntitiesInfo.entities.get(0);

        String type;

        if (entity instanceof ResLeftSideMenu.Channel) {
            type = "channels";
        } else if (entity instanceof ResLeftSideMenu.PrivateGroup) {
            type = "privateGroups";
        } else {
            type = "users";
        }

        List<ResSticker> stickers = JandiStickerDatabaseManager.getInstance(Robolectric.application).getStickers(100);
        ResSticker resSticker = stickers.get((int) (Math.random() * stickers.size()));

        ResCommon resCommon = RequestApiManager.getInstance().sendStickerByStickerApi(ReqSendSticker.create(resSticker.getGroupId(), resSticker.getId(), teamId, entity.id, type, ""));
        assertNotNull(resCommon);

        resSticker = stickers.get((int) (Math.random() * stickers.size()));
        resCommon = RequestApiManager.getInstance().sendStickerByStickerApi(ReqSendSticker.create(resSticker.getGroupId(), resSticker.getId(), teamId, entity.id, type, "test sticker with message"));
        assertNotNull(resCommon);
    }

    @Test
    public void testSendStickerForComment() throws Exception {

        List<ResAccountInfo.UserTeam> userTeams = AccountRepository.getRepository().getAccountTeams();
        int teamId = userTeams.get(0).getTeamId();
        AccountRepository.getRepository().updateSelectedTeamInfo(teamId);


        ReqSearchFile reqSearchFile = new ReqSearchFile();
        reqSearchFile.teamId = teamId;
        reqSearchFile.searchType = ReqSearchFile.SEARCH_TYPE_FILE;
        reqSearchFile.listCount = 1;

        reqSearchFile.fileType = "all";
        reqSearchFile.writerId = "all";
        reqSearchFile.sharedEntityId = -1;

        reqSearchFile.startMessageId = -1;
        reqSearchFile.keyword = "";


        ResSearchFile resSearchFile = RequestApiManager.getInstance().searchFileByMainRest(reqSearchFile);

        ResMessages.FileMessage fileMessage = ((ResMessages.FileMessage) resSearchFile.files.get(0));

        List<ResSticker> stickers = JandiStickerDatabaseManager.getInstance(Robolectric.application).getStickers(100);
        ResSticker resSticker = stickers.get((int) (Math.random() * stickers.size()));

        RequestApiManager.getInstance().sendStickerCommentByStickerApi(ReqSendSticker.create(100, resSticker.getId(), teamId, fileMessage.id, "", "asdasd"));

    }
}
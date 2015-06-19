package com.tosslab.jandi.app.local.database.sticker;

import com.tosslab.jandi.app.network.models.sticker.ResSticker;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created by Steve SeongUg Jung on 15. 6. 4..
 */
@RunWith(RobolectricGradleTestRunner.class)
public class JandiStickerDatabaseManagerTest {

    @Test
    @Ignore
    public void testUpsertStickerKeyword() throws Exception {
        List<ResSticker> stickerList = createStickers();

        JandiStickerDatabaseManager databaseManager = JandiStickerDatabaseManager.getInstance(Robolectric.application);
        databaseManager.upsertStickers(stickerList);

        List<ResSticker> stickers = databaseManager.getStickers(1);

        assertThat(stickers.size(), is(stickerList.size()));

        String id = stickerList.get(0).getId();

        boolean has = false;
        for (ResSticker sticker : stickers) {
            if (sticker.getId().equals(id)) {
                has = true;
                break;
            }
        }

        assertThat(has, is(true));
    }

    @Test
    public void testUpsertRecentSticker() throws Exception {

        JandiStickerDatabaseManager databaseManager = JandiStickerDatabaseManager.getInstance(Robolectric.application);

        int RECENT_COUNT = 3;
        for (int i = 0; i < RECENT_COUNT; i++) {
            databaseManager.upsertRecentSticker(JandiStickerDatabaseManager.DEFAULT_GROUP_ID_MOZZI, String.valueOf(i));
        }

        List<ResSticker> recentStickers = databaseManager.getRecentStickers();
        assertThat(recentStickers.size(), is(RECENT_COUNT));

        for (int i = 0; i < RECENT_COUNT; i++) {
            String recentId = recentStickers.get(i).getId();
            String originId = String.valueOf(RECENT_COUNT - i - 1);
            assertThat(recentId, is(originId));
        }

        // 중복되는 ID 추가
        int INSIDE_INDEX = 1;
        databaseManager.upsertRecentSticker(JandiStickerDatabaseManager.DEFAULT_GROUP_ID_MOZZI, String.valueOf(INSIDE_INDEX));

        recentStickers = databaseManager.getRecentStickers();

        assertThat(recentStickers.get(0).getId(), is(String.valueOf(INSIDE_INDEX)));

        assertThat(recentStickers.size(), is(RECENT_COUNT));

        int OUTSIDE_INDEX = 4;
        databaseManager.upsertRecentSticker(JandiStickerDatabaseManager.DEFAULT_GROUP_ID_MOZZI, String.valueOf(OUTSIDE_INDEX));
        recentStickers = databaseManager.getRecentStickers();

        assertThat(recentStickers.get(0).getId(), is(String.valueOf(OUTSIDE_INDEX)));
        assertThat(recentStickers.size(), is(RECENT_COUNT + 1));
    }

    private List<ResSticker> createStickers() {
        List<ResSticker> stickerList = new ArrayList<ResSticker>();

        for (int i = 0; i < 10; i++) {
            ResSticker sticker = new ResSticker();
            sticker.setWeb("Web : " + i);
            sticker.setMobile("Mobile : " + i);
            sticker.setGroupId(1);
            sticker.setId("Id : " + i);

            stickerList.add(sticker);

        }
        return stickerList;
    }
}
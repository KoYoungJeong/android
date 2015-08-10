package com.tosslab.jandi.app.local.orm.repositories;

import com.tosslab.jandi.app.network.models.ResMessages;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by Steve SeongUg Jung on 15. 7. 23..
 */
@RunWith(RobolectricGradleTestRunner.class)
public class StickerRepositoryTest {

    @Test
    public void testSticker() throws Exception {

        StickerRepository repository = StickerRepository.getRepository();
        List<ResMessages.StickerContent> stickers = repository.getStickers();
        assertThat(stickers.size(), is(equalTo(26)));

        stickers = repository.getStickers(100);
        assertThat(stickers.size(), is(equalTo(26)));


        ResMessages.StickerContent sampleSticker = stickers.get(0);
        ResMessages.StickerContent sampleSticker2 = stickers.get(1);
        ResMessages.StickerContent sampleSticker3 = stickers.get(2);


        repository.upsertRecentSticker(sampleSticker);

        List<ResMessages.StickerContent> recentStickers = repository.getRecentStickers();

        assertThat(recentStickers.size(), is(equalTo(1)));
        assertThat(recentStickers.get(0).stickerId, is(equalTo(sampleSticker.stickerId)));


        repository.upsertRecentSticker(sampleSticker2);
        repository.upsertRecentSticker(sampleSticker);
        repository.upsertRecentSticker(sampleSticker3.groupId, sampleSticker3.stickerId);

        recentStickers = repository.getRecentStickers();

        assertThat(recentStickers.size(), is(equalTo(3)));
        assertThat(recentStickers.get(0).stickerId, is(equalTo(sampleSticker3.stickerId)));
        assertThat(recentStickers.get(1).stickerId, is(equalTo(sampleSticker.stickerId)));
        assertThat(recentStickers.get(2).stickerId, is(equalTo(sampleSticker2.stickerId)));

    }
}
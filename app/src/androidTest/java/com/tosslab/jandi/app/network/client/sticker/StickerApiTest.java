package com.tosslab.jandi.app.network.client.sticker;

import com.tosslab.jandi.app.network.client.teams.search.SearchApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.InnerApiRetrofitBuilder;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.search.ReqSearch;
import com.tosslab.jandi.app.network.models.search.ResSearch;
import com.tosslab.jandi.app.network.models.sticker.ReqSendSticker;
import com.tosslab.jandi.app.team.TeamInfoLoader;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import setup.BaseInitUtil;

import static org.assertj.core.api.Assertions.assertThat;

@org.junit.runner.RunWith(android.support.test.runner.AndroidJUnit4.class)
public class StickerApiTest {

    private StickerApi stickerApi;
    private ResSearch.File fileInfo;
    private long teamId;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
    }

    @Before
    public void setUp() throws Exception {
        stickerApi = new StickerApi(InnerApiRetrofitBuilder.getInstance());
        fileInfo = getFileInfo();

        teamId = TeamInfoLoader.getInstance().getTeamId();
    }

    private ResSearch.File getFileInfo() throws RetrofitException {
        ReqSearch reqSearch = new ReqSearch.Builder()
                .setRoomId(-1)
                .setWriterId(-1)
                .setType("file")
                .setFileType("all")
                .setPage(1)
                .setCount(1)
                .setKeyword("").build();
        ResSearch search = new SearchApi(InnerApiRetrofitBuilder.getInstance()).getSearch(TeamInfoLoader.getInstance().getTeamId(), reqSearch);
        return search.getRecords().get(0).getFile();
    }

    @Test
    public void sendStickerComment() throws Exception {
        List<ResMessages.Link> result = stickerApi.sendStickerComment(ReqSendSticker.create(103, "1", teamId, fileInfo.getId(), "", "asd", new ArrayList<>()));

        assertThat(result).hasSize(2);

        Observable.from(result)
                .subscribe(link -> {
                    assertThat(link.messageId).isGreaterThan(0);
                });

    }


}
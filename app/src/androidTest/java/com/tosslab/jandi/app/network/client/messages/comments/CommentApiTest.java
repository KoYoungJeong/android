package com.tosslab.jandi.app.network.client.messages.comments;

import com.tosslab.jandi.app.network.client.file.FileApi;
import com.tosslab.jandi.app.network.client.teams.search.SearchApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqSendComment;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.network.models.search.ReqSearch;
import com.tosslab.jandi.app.network.models.search.ResSearch;
import com.tosslab.jandi.app.team.TeamInfoLoader;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import setup.BaseInitUtil;

import static org.assertj.core.api.Assertions.assertThat;

@org.junit.runner.RunWith(android.support.test.runner.AndroidJUnit4.class)
public class CommentApiTest {

    private CommentApi commentApi;
    private ResSearch.File fileMessage;
    private long teamId;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
    }

    @Before
    public void setUp() throws Exception {
        commentApi = new CommentApi(RetrofitBuilder.getInstance());
        FileApi fileApi = new FileApi(RetrofitBuilder.getInstance());
        teamId = TeamInfoLoader.getInstance().getTeamId();
        long defaultTopicId = TeamInfoLoader.getInstance().getDefaultTopicId();
        fileMessage = getDownloadInfo();
    }

    private ResSearch.File getDownloadInfo() throws RetrofitException {
        ReqSearch reqSearch = new ReqSearch.Builder()
                .setRoomId(-1)
                .setWriterId(-1)
                .setType("file")
                .setFileType("all")
                .setPage(1)
                .setCount(1)
                .setKeyword("").build();
        ResSearch search = new SearchApi(RetrofitBuilder.getInstance()).getSearch(TeamInfoLoader.getInstance().getTeamId(), reqSearch);
        return search.getRecords().get(0).getFile();
    }

    @Test
    public void sendMessageComment() throws Exception {
        ResCommon asd = commentApi.sendMessageComment(fileMessage.getId(), teamId, new ReqSendComment("asd", new ArrayList<MentionObject>()));

        assertThat(asd).isNotNull();
        assertThat(asd.id).isGreaterThan(0);

    }


}
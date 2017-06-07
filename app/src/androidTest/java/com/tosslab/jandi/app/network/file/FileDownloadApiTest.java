package com.tosslab.jandi.app.network.file;

import android.support.annotation.NonNull;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.network.client.teams.search.SearchApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.InnerApiRetrofitBuilder;
import com.tosslab.jandi.app.network.models.search.ReqSearch;
import com.tosslab.jandi.app.network.models.search.ResSearch;
import com.tosslab.jandi.app.team.TeamInfoLoader;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

import setup.BaseInitUtil;

import static com.jayway.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;

@org.junit.runner.RunWith(android.support.test.runner.AndroidJUnit4.class)
public class FileDownloadApiTest {
    private boolean finish;
    private boolean success;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
    }

    @Before
    public void setUp() throws Exception {
        finish = false;
        success = false;
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
        ResSearch search = new SearchApi(InnerApiRetrofitBuilder.getInstance()).getSearch(TeamInfoLoader.getInstance().getTeamId(), reqSearch);
        return search.getRecords().get(0).getFile();
    }

    @NonNull
    protected String getSavePath(ResSearch.File downloadUrl) {
        File cacheDir = JandiApplication.getContext().getCacheDir();
        return cacheDir.getAbsoluteFile() + "/" + downloadUrl.getTitle();
    }

    @Test
    public void download() throws Exception {
        ResSearch.File downloadInfo = getDownloadInfo();
        String savePath = getSavePath(downloadInfo);
        new FileDownloadApi().download(downloadInfo.getFileUrl(), savePath, callback -> callback
                .subscribe(it -> {}, t -> finish = true, () -> {
                    finish = true;
                    success = true;
                }));

        await().until(() -> finish);

        assertThat(success).isTrue();
    }

    @Test
    public void downloadImmediatly() throws Exception {
        ResSearch.File downloadInfo = getDownloadInfo();
        String savePath = getSavePath(downloadInfo);
        new FileDownloadApi().downloadImmediatly(downloadInfo.getFileUrl(), savePath, callback -> callback.subscribe(it -> {}, t -> finish = true, () -> {
            finish = true;
            success = true;
        }));

        await().until(() -> finish);

        assertThat(success).isTrue();
    }
}
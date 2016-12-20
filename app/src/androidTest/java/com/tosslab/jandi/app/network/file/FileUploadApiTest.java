package com.tosslab.jandi.app.network.file;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.network.models.ResUploadedFile;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.album.imagealbum.model.ImageAlbumModel_;
import com.tosslab.jandi.app.ui.album.imagealbum.vo.ImagePicture;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import setup.BaseInitUtil;

import static org.assertj.core.api.Assertions.assertThat;

@org.junit.runner.RunWith(android.support.test.runner.AndroidJUnit4.class)
public class FileUploadApiTest {
    boolean progressed;
    boolean finished;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
    }

    @Before
    public void setUp() throws Exception {
        progressed = false;
        finished = false;

    }

    protected List<ImagePicture> getImageList() {
        return ImageAlbumModel_.getInstance_(JandiApplication.getContext()).getAllPhotoList(
                JandiApplication.getContext(), 0);
    }

    @Test
    public void uploadFile() throws Exception {
        FileUploadApi api = new FileUploadApi();

        ResUploadedFile resFileUpload = api.uploadFile("test", TeamInfoLoader.getInstance().getDefaultTopicId(),
                TeamInfoLoader.getInstance().getTeamId(), "hahaha", new ArrayList<>(),
                new File(getImageList().get(0).getImagePath()), callback -> callback.subscribe(
                        integer -> progressed = true,
                        t -> {},
                        () -> finished = true)).execute().body();

        assertThat(progressed).isTrue();
        assertThat(finished).isTrue();
        assertThat(resFileUpload).isNotNull();
        assertThat(resFileUpload.getMessageId()).isGreaterThan(0);
        assertThat(resFileUpload.getCommentId()).isGreaterThan(0);

    }


}
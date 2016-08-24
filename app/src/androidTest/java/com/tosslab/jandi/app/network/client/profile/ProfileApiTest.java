package com.tosslab.jandi.app.network.client.profile;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.start.Human;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.album.imagealbum.model.ImageAlbumModel_;
import com.tosslab.jandi.app.ui.album.imagealbum.vo.ImagePicture;

import org.junit.Test;

import java.io.File;
import java.util.List;

import setup.BaseInitUtil;

import static org.assertj.core.api.Assertions.assertThat;

@org.junit.runner.RunWith(android.support.test.runner.AndroidJUnit4.class)
public class ProfileApiTest {
    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
    }

    protected List<ImagePicture> getImageList() {
        return ImageAlbumModel_.getInstance_(JandiApplication.getContext()).getAllPhotoList(
                JandiApplication.getContext(), 0);
    }

    @Test
    public void uploadProfilePhoto() throws Exception {
        String imagePicture = getImageList().get(0).getImagePath();
        Human resCommon = new ProfileApi(RetrofitBuilder.getInstance()).uploadProfilePhoto(TeamInfoLoader.getInstance().getMyId(), new File(imagePicture));

        assertThat(resCommon).isNotNull();
    }


}
package com.tosslab.jandi.app.ui.album.imagealbum.presenter;

import com.jayway.awaitility.Awaitility;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.ui.album.imagealbum.model.ImageAlbumModel;
import com.tosslab.jandi.app.ui.album.imagealbum.vo.ImageAlbum;
import com.tosslab.jandi.app.ui.album.imagealbum.vo.ImagePicture;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import setup.BaseInitUtil;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by tee on 16. 3. 23..
 */
public class ImageAlbumPresenterImplTest {

    private ImageAlbumPresenterImpl imageAlbumPresenter;
    private ImageAlbumPresenter.View viewMock;
    private ImageAlbumModel modelMock;

    @BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        BaseInitUtil.releaseDatabase();
    }
    @Before
    public void setUp() throws Exception {
        imageAlbumPresenter = ImageAlbumPresenterImpl_.getInstance_(JandiApplication.getContext());
        viewMock = mock(ImageAlbumPresenter.View.class);
        imageAlbumPresenter.setView(viewMock);
        modelMock = mock(ImageAlbumModel.class);
    }



    @Test
    public void testOnLoadImageAlbum() {
        {
            imageAlbumPresenter.imageAlbumModel = modelMock;
            final boolean[] finish = {false};
            doAnswer(invocationOnMock -> {
                finish[0] = true;
                return invocationOnMock;
            }).when(viewMock).hideProgress();

            when(modelMock.isFirstAlbumPage(Mockito.anyInt())).thenReturn(true);
            when(modelMock.isAllAlbum(Mockito.anyInt())).thenReturn(false);

            imageAlbumPresenter.onLoadImageAlbum(0);

            Awaitility.await().until(() -> finish[0]);

            verify(viewMock).showDefaultAlbumList(anyList());
        }

        {
            imageAlbumPresenter.imageAlbumModel = modelMock;
            final boolean[] finish = {false, false};
            doAnswer(invocationOnMock -> {
                finish[0] = true;
                return invocationOnMock;
            }).when(viewMock).hideProgress();

            when(modelMock.isFirstAlbumPage(Mockito.anyInt())).thenReturn(false);
            when(modelMock.isAllAlbum(Mockito.anyInt())).thenReturn(true);

            imageAlbumPresenter.onLoadImageAlbum(0);

            Awaitility.await().until(() -> finish[0]);

            finish[1] = false;
            doAnswer(invocationOnMock -> {
                finish[1] = true;
                return invocationOnMock;
            }).when(viewMock).hideProgress();

            when(modelMock.isFirstAlbumPage(Mockito.anyInt())).thenReturn(false);
            when(modelMock.isAllAlbum(Mockito.anyInt())).thenReturn(false);

            imageAlbumPresenter.onLoadImageAlbum(0);

            Awaitility.await().until(() -> finish[1]);

            verify(viewMock, times(2)).showPhotoList(anyList());
        }

    }

    @Test
    public void testOnLoadMorePhotos() {
        imageAlbumPresenter.imageAlbumModel = modelMock;

        List<ImagePicture> mockList = new ArrayList<>();
        mockList.add(new ImagePicture.ImagePictureBuilder().createImagePicture());
        mockList.add(new ImagePicture.ImagePictureBuilder().createImagePicture());

        {
            when(modelMock.isAllAlbum(Mockito.anyInt())).thenReturn(true);
            when(modelMock.getAllPhotoList(anyObject(), anyInt())).thenReturn(mockList);

            imageAlbumPresenter.onLoadMorePhotos(0, 0);

            verify(modelMock, timeout(1000)).getAllPhotoList(anyObject(), anyInt());

            when(modelMock.isAllAlbum(Mockito.anyInt())).thenReturn(false);
            when(modelMock.getPhotoList(anyObject(), anyInt(), anyInt())).thenReturn(mockList);

            imageAlbumPresenter.onLoadMorePhotos(1, 1);

            verify(modelMock, timeout(1000)).getPhotoList(anyObject(), anyInt(), anyInt());

            verify(viewMock, times(2)).addPhotoList(anyList());
        }
    }

    @Test
    public void testOnSelectPicture() {
        imageAlbumPresenter.imageAlbumModel = modelMock;
        when(modelMock.isSelectedPicture(anyObject())).thenReturn(true);

        ImagePicture imagePicture =
                new ImagePicture.ImagePictureBuilder().createImagePicture();

        final boolean[] finish = {false, false};

        doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(viewMock).notifyItemOptionMenus();

        imageAlbumPresenter.onSelectPicture(imagePicture, 0);

        Awaitility.await().until(() -> finish[0]);

        verify(modelMock).removeSelectedPicture(anyObject());

        when(modelMock.isSelectedPicture(anyObject())).thenReturn(false);

        doAnswer(invocationOnMock -> {
            finish[1] = true;
            return invocationOnMock;
        }).when(viewMock).notifyItemOptionMenus();

        imageAlbumPresenter.onSelectPicture(imagePicture, 0);

        Awaitility.await().until(() -> finish[1]);

        verify(modelMock).addSelectedPicture(anyObject());
    }

    @Test
    public void testOnSelectAlbum() {
        imageAlbumPresenter.onSelectAlbum(new ImageAlbum.ImageAlbumBuilder().createImageAlbum());
        verify(viewMock).moveImagePicture(anyInt());
    }


}
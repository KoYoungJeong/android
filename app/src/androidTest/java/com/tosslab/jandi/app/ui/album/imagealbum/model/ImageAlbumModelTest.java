package com.tosslab.jandi.app.ui.album.imagealbum.model;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.ui.album.imagealbum.ImageAlbumFragment;
import com.tosslab.jandi.app.ui.album.imagealbum.vo.ImageAlbum;
import com.tosslab.jandi.app.ui.album.imagealbum.vo.ImagePicture;
import com.tosslab.jandi.app.ui.album.imagealbum.vo.SelectPictures;

import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import setup.BaseInitUtil;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by tee on 16. 3. 23..
 */
public class ImageAlbumModelTest {

    private ImageAlbumModel imageAlbumModel;

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
        imageAlbumModel = new ImageAlbumModel();
    }


    @Test
    public void testGetDefaultAlbumList() {
        List<ImageAlbum> imageAlbums =
                imageAlbumModel.getDefaultAlbumList(JandiApplication.getContext());
        assertNotNull(imageAlbums);
        if (imageAlbums.size() > 0) {
            assertThat(imageAlbums.size(), Matchers.greaterThan(0));
        }
    }

    @Test
    public void testGetPhotoList() {
        List<ImageAlbum> imageAlbums =
                imageAlbumModel.getDefaultAlbumList(JandiApplication.getContext());
        if (imageAlbums.size() > 0) {
            List<ImagePicture> imagePictures =
                    imageAlbumModel.getPhotoList(JandiApplication.getContext(), imageAlbums.get(0).getBucketId(), 0);
            assertNotNull(imagePictures);
            assertThat(imagePictures.size(), Matchers.greaterThan(0));
        }
    }

    @Test
    public void testIsSelectedPicture() {
        SelectPictures.getSelectPictures().clear();
        SelectPictures imagePicture = SelectPictures.getSelectPictures();
        ImagePicture imagePicture1 = new ImagePicture.ImagePictureBuilder().imagePath("videoPath").createImagePicture();
        imagePicture.addPicture(imagePicture1);
        if (imageAlbumModel.isSelectedPicture(imagePicture1)) {
            assertTrue(true);
        } else {
            assertTrue(false);
        }
    }

    @Test
    public void testGetSelectedImages() {
        SelectPictures.getSelectPictures().clear();
        SelectPictures imagePicture = SelectPictures.getSelectPictures();
        ImagePicture imagePicture1 = new ImagePicture.ImagePictureBuilder().imagePath("videoPath").createImagePicture();
        imagePicture.addPicture(imagePicture1);

        ImagePicture imagePicture2 = new ImagePicture.ImagePictureBuilder().imagePath("videoPath").createImagePicture();
        imagePicture.addPicture(imagePicture2);

        assertThat(imageAlbumModel.getSelectedImages(), is(2));
    }

    @Test
    public void testCreateViewAllAlbum() {
        ImageAlbum imageAlbum
                = imageAlbumModel.createViewAllAlbum(JandiApplication.getContext());
        assertNotNull(imageAlbum);
    }

    @Test
    public void testIsAllAlbum() {
        boolean result = imageAlbumModel.isAllAlbum(ImageAlbumFragment.BUCKET_ALL_IMAGE_ALBUM);
        assertTrue(result);
    }

    @Test
    public void testGetAllPhotoList() {
        List<ImageAlbum> imageAlbums =
                imageAlbumModel.getDefaultAlbumList(JandiApplication.getContext());
        if (imageAlbums.size() > 0) {
            List<ImagePicture> imagePictures = imageAlbumModel.getAllPhotoList(
                    JandiApplication.getContext(), 0);
            assertThat(imagePictures.size(), greaterThan(0));
        }
    }

    @Test
    public void testRemoveSelectedPicture() {
        SelectPictures selectedPictures = SelectPictures.getSelectPictures();
        ImagePicture imagePicture1 = new ImagePicture.ImagePictureBuilder().imagePath("videoPath").createImagePicture();
        selectedPictures.addPicture(imagePicture1);

        ImagePicture imagePicture2 = new ImagePicture.ImagePictureBuilder().imagePath("videoPath").createImagePicture();
        selectedPictures.addPicture(imagePicture2);

        imageAlbumModel.removeSelectedPicture(imagePicture2);
        assertThat(selectedPictures.getPictures().size(), is(1));
    }

    @Test
    public void testAddSelectedPicture() {
        SelectPictures.getSelectPictures().clear();
        SelectPictures selectedPictures = SelectPictures.getSelectPictures();
        ImagePicture imagePicture1 = new ImagePicture.ImagePictureBuilder().imagePath("videoPath").createImagePicture();
        selectedPictures.addPicture(imagePicture1);

        ImagePicture imagePicture2 = new ImagePicture.ImagePictureBuilder().imagePath("videoPath").createImagePicture();
        imageAlbumModel.addSelectedPicture(imagePicture2);
        assertThat(selectedPictures.getPictures().size(), is(2));
    }


}
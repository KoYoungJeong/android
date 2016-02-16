package com.tosslab.jandi.app.ui.album.imagealbum.presenter;

import android.content.Context;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.album.imagealbum.model.ImageAlbumModel;
import com.tosslab.jandi.app.ui.album.imagealbum.vo.ImageAlbum;
import com.tosslab.jandi.app.ui.album.imagealbum.vo.ImagePicture;
import com.tosslab.jandi.app.ui.album.imagealbum.vo.SelectPictures;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.List;

@EBean
public class ImageAlbumPresenterImpl implements ImageAlbumPresenter {

    @RootContext
    Context context;

    @Bean
    ImageAlbumModel imageAlbumModel;

    ImageAlbumPresenter.View view;

    @Override
    @Background
    public void onLoadImageAlbum(int buckerId) {
        view.showProgress();
        if (imageAlbumModel.isFirstAlbumPage(buckerId)) {
            List<ImageAlbum> defaultAlbumList = imageAlbumModel.getDefaultAlbumList(context);
            ImageAlbum viewAllAlbum = imageAlbumModel.createViewAllAlbum(context);
            defaultAlbumList.add(0, viewAllAlbum);
            view.showDefaultAlbumList(defaultAlbumList);
        } else if (imageAlbumModel.isAllAlbum(buckerId)) {
            List<ImagePicture> photoList = imageAlbumModel.getAllPhotoList(context, 0);
            view.showPhotoList(photoList);
        } else {
            List<ImagePicture> photoList = imageAlbumModel.getPhotoList(context, buckerId, 0);
            view.showPhotoList(photoList);
        }
        view.hideProgress();
    }

    @Background
    @Override
    public void onLoadMorePhotos(int bucketId, int imageId) {
        List<ImagePicture> photoList;
        if (imageAlbumModel.isAllAlbum(bucketId)) {
            photoList = imageAlbumModel.getAllPhotoList(context, imageId);
        } else {
            photoList = imageAlbumModel.getPhotoList(context, bucketId, imageId);
        }
        if (photoList != null && !photoList.isEmpty()) {
            view.addPhotoList(photoList);
        }
    }

    @Override
    public void setView(ImageAlbumPresenter.View view) {
        this.view = view;
    }

    @Override
    public void onSetupActionbar(int buckerId) {
        String title = context.getString(R.string.jandi_select_gallery);
        int selectedCount = imageAlbumModel.getSelectedImages();

        setupActionbarTitle(selectedCount, title);
    }

    @Override
    public void onSelectPicture(ImagePicture item, int position) {
        boolean isSelectedPicture = imageAlbumModel.isSelectedPicture(item);

        if (isSelectedPicture) {
            imageAlbumModel.removeSelectedPicture(item);
        } else {
            boolean isAdded = imageAlbumModel.addSelectedPicture(item);
            if (!isAdded && imageAlbumModel.getSelectedImages() >= SelectPictures.MAX_PICKER_COUNT) {
                view.showWarningToast(context.getString(R.string.jandi_select_maximum_10));
            }
        }

        view.notifyItemRangeChanged(position, 1);
        view.notifyItemOptionMenus();

    }

    private void setupActionbarTitle(int size, String bucketTitle) {
        if (size > 0) {
            view.setActinbarTitle(String.format("%s (%d/%d)", bucketTitle, size, SelectPictures.MAX_PICKER_COUNT));
        } else {
            view.setActinbarTitle(bucketTitle);
        }
    }

    @Override
    public void onSelectAlbum(ImageAlbum item) {
        int bucketId = item.getBucketId();
        view.moveImagePicture(bucketId);
    }

}
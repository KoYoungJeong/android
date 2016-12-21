package com.tosslab.jandi.app.ui.album.imagealbum.presenter;

import android.content.Context;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.album.imagealbum.model.ImageAlbumModel;
import com.tosslab.jandi.app.ui.album.imagealbum.vo.ImageAlbum;
import com.tosslab.jandi.app.ui.album.imagealbum.vo.ImagePicture;
import com.tosslab.jandi.app.ui.album.imagealbum.vo.SelectPictures;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ImageAlbumPresenterImpl implements ImageAlbumPresenter {

    Context context;

    ImageAlbumModel imageAlbumModel;
    ImageAlbumPresenter.View view;

    @Inject
    public ImageAlbumPresenterImpl(View view, ImageAlbumModel imageAlbumModel) {
        this.imageAlbumModel = imageAlbumModel;
        this.view = view;
        context = JandiApplication.getContext();
    }

    @Override
    public void onLoadImageAlbum(int buckerId) {
        view.showProgress();

        if (imageAlbumModel.isFirstAlbumPage(buckerId)) {
            Observable.fromCallable(() -> {
                List<ImageAlbum> defaultAlbumList = imageAlbumModel.getDefaultAlbumList(context);
                ImageAlbum viewAllAlbum = imageAlbumModel.createViewAllAlbum(context);
                defaultAlbumList.add(0, viewAllAlbum);
                return defaultAlbumList;
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnUnsubscribe(() -> view.hideProgress())
                    .subscribe(defaultAlbumList -> view.showDefaultAlbumList(defaultAlbumList));
        } else if (imageAlbumModel.isAllAlbum(buckerId)) {
            Observable.fromCallable(() -> imageAlbumModel.getAllPhotoList(context, 0))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnUnsubscribe(() -> view.hideProgress())
                    .subscribe(photoList -> {
                        view.showPhotoList(photoList);
                    });
        } else {
            Observable.fromCallable(() -> imageAlbumModel.getPhotoList(context, buckerId, 0))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnUnsubscribe(() -> view.hideProgress())
                    .subscribe(photoList -> view.showPhotoList(photoList));

        }

    }

    @Override
    public void onLoadMorePhotos(int bucketId, int imageId) {
        Observable.fromCallable(() -> {
            if (imageAlbumModel.isAllAlbum(bucketId)) {
                return imageAlbumModel.getAllPhotoList(context, imageId);
            } else {
                return imageAlbumModel.getPhotoList(context, bucketId, imageId);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(photoList -> photoList != null && !photoList.isEmpty())
                .subscribe(photoList -> view.addPhotoList(photoList));
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

package com.tosslab.jandi.app.ui.album.fragment.presenter;

import android.content.Context;
import android.graphics.Rect;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.album.fragment.model.ImageAlbumModel;
import com.tosslab.jandi.app.ui.album.fragment.vo.ImageAlbum;
import com.tosslab.jandi.app.ui.album.fragment.vo.ImagePicture;

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
    public void onLoadImageAlbum(int buckerId) {
        if (imageAlbumModel.isFirstAlbumPage(buckerId)) {
            List<ImageAlbum> defaultAlbumList = imageAlbumModel.getDefaultAlbumList(context);
            ImageAlbum viewAllAlbum = imageAlbumModel.createViewAllAlbum(context);
            defaultAlbumList.add(0, viewAllAlbum);
            view.showDefaultAlbumList(defaultAlbumList);
        } else if (imageAlbumModel.isAllAlbum(buckerId)) {
            List<ImagePicture> photoList = imageAlbumModel.getAllPhotoList(context);
            view.showPhotoList(photoList);
        } else {
            List<ImagePicture> photoList = imageAlbumModel.getPhotoList(context, buckerId);
            view.showPhotoList(photoList);
        }
    }

    @Override
    public void setView(ImageAlbumPresenter.View view) {
        this.view = view;
    }

    @Override
    public void onSetupActionbar(int buckerId) {

        int selectedCount;

        String title;
        if (imageAlbumModel.isFirstAlbumPage(buckerId)) {
            title = context.getString(R.string.jandI_gallery);
            selectedCount = imageAlbumModel.getSelectedImages();
        } else if (imageAlbumModel.isAllAlbum(buckerId)) {
            title = context.getString(R.string.jandi_view_all);
            selectedCount = imageAlbumModel.getSelectedImages();
        } else {
            title = imageAlbumModel.getBucketTitle(context, buckerId);
            selectedCount = imageAlbumModel.getSelectedBucketImages(buckerId);
        }

        setupActionbarTitle(selectedCount, title);
    }

    @Override
    public void onSelectPicture(ImagePicture item, int position) {
        imageAlbumModel.toggleImagePath(item);

        view.notifyItemRangeChanged(position, 1);

    }

    private void setupActionbarTitle(int size, String bucketTitle) {
        if (size > 0) {
            view.setActinbarTitle(String.format("%s (%d)", bucketTitle, size));
        } else {
            view.setActinbarTitle(bucketTitle);
        }
    }

    @Override
    public void onSelectAlbum(ImageAlbum item) {
        int bucketId = item.getBucketId();
        view.moveImagePicture(bucketId);
    }

    @Override
    public void onPreviewImage(android.view.View childView, String imagePath) {
        Rect rect = new Rect();
        rect.set(0, 0, childView.getWidth(), childView.getHeight());
        Rect viewOffset = imageAlbumModel.getAbsoluteOffset(childView, rect);

        this.view.showPreview(childView, imagePath);
    }
}

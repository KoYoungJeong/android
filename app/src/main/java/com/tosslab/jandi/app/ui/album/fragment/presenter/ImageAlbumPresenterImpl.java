package com.tosslab.jandi.app.ui.album.fragment.presenter;

import android.content.Context;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.album.fragment.model.ImageAlbumModel;
import com.tosslab.jandi.app.ui.album.fragment.vo.ImageAlbum;
import com.tosslab.jandi.app.ui.album.fragment.vo.ImagePicture;
import com.tosslab.jandi.app.ui.album.fragment.vo.SelectPictures;

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

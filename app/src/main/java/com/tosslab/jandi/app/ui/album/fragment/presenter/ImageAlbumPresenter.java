package com.tosslab.jandi.app.ui.album.fragment.presenter;

import com.tosslab.jandi.app.ui.album.fragment.vo.ImageAlbum;
import com.tosslab.jandi.app.ui.album.fragment.vo.ImagePicture;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 6. 15..
 */
public interface ImageAlbumPresenter {
    void onLoadImageAlbum(int buckerId);

    void setView(View view);

    void onSetupActionbar(int buckerId);

    void onSelectPicture(ImagePicture item, int position);

    void onSelectAlbum(ImageAlbum item);

    interface View {

        void showDefaultAlbumList(List<ImageAlbum> defaultAlbumList);

        void showPhotoList(List<ImagePicture> photoList);

        void setActinbarTitle(String bucketTitle);

        void notifyItemRangeChanged(int position, int range);

        void moveImagePicture(int bucketId);

        void showWarningToast(String message);

        void notifyItemOptionMenus();
    }
}

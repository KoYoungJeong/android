package com.tosslab.jandi.app.ui.album.videoalbum.presenter;

import com.tosslab.jandi.app.ui.album.videoalbum.vo.VideoAlbum;
import com.tosslab.jandi.app.ui.album.videoalbum.vo.VideoItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 6. 15..
 */
public interface VideoAlbumPresenter {
    void onLoadVideoAlbum(int buckerId);

    void onLoadMoreVideos(int bucketId, int videoId);

    void onSelectVideo(VideoItem item);

    void onSelectAlbum(VideoAlbum item);

    interface View {
        void showProgress();

        void hideProgress();

        void showDefaultAlbumList(List<VideoAlbum> defaultAlbumList);

        void showVideoList(List<VideoItem> videoList);

        void addVideoList(List<VideoItem> videoList);

        void setActinbarTitle(String bucketTitle);

        void notifyItemRangeChanged(int position, int range);

        void moveVideoItem(int bucketId);

        void showWarningToast(String message);

        void notifyItemOptionMenus();

        void moveFileUploadActivity(ArrayList<String> filePaths);
    }
}

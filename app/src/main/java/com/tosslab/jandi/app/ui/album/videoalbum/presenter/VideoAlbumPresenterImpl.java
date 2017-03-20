package com.tosslab.jandi.app.ui.album.videoalbum.presenter;

import android.content.Context;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.ui.album.videoalbum.model.VideoAlbumModel;
import com.tosslab.jandi.app.ui.album.videoalbum.vo.SelectVideos;
import com.tosslab.jandi.app.ui.album.videoalbum.vo.VideoAlbum;
import com.tosslab.jandi.app.ui.album.videoalbum.vo.VideoItem;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class VideoAlbumPresenterImpl implements VideoAlbumPresenter {

    Context context;

    VideoAlbumModel videoAlbumModel;
    View view;

    @Inject
    public VideoAlbumPresenterImpl(View view, VideoAlbumModel videoAlbumModel) {
        this.videoAlbumModel = videoAlbumModel;
        this.view = view;
        context = JandiApplication.getContext();
    }

    @Override
    public void onLoadVideoAlbum(int buckerId) {
        view.showProgress();

        if (videoAlbumModel.isFirstAlbumPage(buckerId)) {
            Observable.fromCallable(() -> {
                List<VideoAlbum> defaultAlbumList = videoAlbumModel.getDefaultVideoAlbumList(context);
                VideoAlbum viewAllAlbum = videoAlbumModel.createViewAllAlbum(context);
                defaultAlbumList.add(0, viewAllAlbum);
                return defaultAlbumList;
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnUnsubscribe(() -> view.hideProgress())
                    .subscribe(defaultAlbumList -> view.showDefaultAlbumList(defaultAlbumList));
        } else if (videoAlbumModel.isAllAlbum(buckerId)) {
            Observable.fromCallable(() -> videoAlbumModel.getAllVideoList(context, 0))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnUnsubscribe(() -> view.hideProgress())
                    .subscribe(photoList -> {
                        view.showVideoList(photoList);
                    });
        } else {
            Observable.fromCallable(() -> videoAlbumModel.getVideoList(context, buckerId, 0))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnUnsubscribe(() -> view.hideProgress())
                    .subscribe(photoList -> view.showVideoList(photoList));

        }

    }

    @Override
    public void onLoadMoreVideos(int bucketId, int videoId) {
        Observable.fromCallable(() -> {
            if (videoAlbumModel.isAllAlbum(bucketId)) {
                return videoAlbumModel.getAllVideoList(context, videoId);
            } else {
                return videoAlbumModel.getVideoList(context, bucketId, videoId);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(photoList -> photoList != null && !photoList.isEmpty())
                .subscribe(photoList -> view.addVideoList(photoList));
    }

    @Override
    public void onSelectVideo(VideoItem item) {
        videoAlbumModel.putSelectedVideo(item);
        view.moveFileUploadActivity(getSelectedVideosPathList());
    }

    private ArrayList<String> getSelectedVideosPathList() {
        ArrayList<String> value = new ArrayList<>();
        List<VideoItem> videos = SelectVideos.getSelectVideos().getVideos();

        for (VideoItem video : videos) {
            value.add(video.getVideoPath());
        }
        return value;
    }

    @Override
    public void onSelectAlbum(VideoAlbum item) {
        int bucketId = item.getBucketId();
        view.moveVideoItem(bucketId);
    }

}

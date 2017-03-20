package com.tosslab.jandi.app.ui.album.videoalbum.vo;

import android.text.TextUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import rx.Observable;

/**
 * Created by Steve SeongUg Jung on 15. 6. 15..
 */
public class SelectVideos {

    public static final int MAX_PICKER_COUNT = 10;
    private static final VideoItem NULL_OBJECT =
            new VideoItem.VideoItemBuilder().createVideoItem();

    private static SelectVideos selectVideos;

    private List<VideoItem> videos;


    private SelectVideos() {
        videos = new CopyOnWriteArrayList<>();
    }

    public static SelectVideos getSelectVideos() {
        if (selectVideos == null) {
            selectVideos = new SelectVideos();
        }

        return selectVideos;
    }

    public boolean addVideo(VideoItem path) {
        if (videos != null && videos.size() < MAX_PICKER_COUNT) {
            videos.add(path);
            return true;
        } else {
            return false;
        }
    }

    public boolean removeVideos(VideoItem path) {
        for (int idx = videos.size() - 1; idx >= 0; idx--) {
            if (TextUtils.equals(videos.get(idx).getVideoPath(), path.getVideoPath())) {
                videos.remove(idx);
                return true;
            }
        }
        return false;
    }

    public boolean contains(String path) {

        VideoItem first = Observable.from(videos)
                .filter(videoPath -> TextUtils.equals(videoPath.getVideoPath(), path))
                .firstOrDefault(NULL_OBJECT)
                .toBlocking()
                .first();

        return first != NULL_OBJECT;
    }

    public int getCountOfBucket(int buckerId) {
        int count = Observable.from(videos)
                .filter(imagePicture -> imagePicture.getBuckerId() == buckerId)
                .count()
                .toBlocking()
                .first();

        return count;

    }

    public void clear() {
        videos.clear();
    }

    public List<VideoItem> getVideos() {
        return Collections.unmodifiableList(videos);
    }

}

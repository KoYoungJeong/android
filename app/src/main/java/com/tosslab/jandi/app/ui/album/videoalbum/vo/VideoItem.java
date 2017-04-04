package com.tosslab.jandi.app.ui.album.videoalbum.vo;

/**
 * Created by Steve SeongUg Jung on 15. 6. 15..
 */
public class VideoItem {

    private final int _id;
    private final int buckerId;
    private final String videoPath;
    private final String thumbnailPath;

    private VideoItem(int _id, int buckerId, String videoPath, String thumbnailPath) {
        this._id = _id;
        this.buckerId = buckerId;
        this.videoPath = videoPath;
        this.thumbnailPath = thumbnailPath;
    }

    public int get_id() {
        return _id;
    }

    public int getBuckerId() {
        return buckerId;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public static class VideoItemBuilder {
        private int id;
        private int buckerId;
        private String videoPath;
        private String thumbNailPath;

        public VideoItemBuilder _id(int id) {
            this.id = id;
            return this;
        }

        public VideoItemBuilder buckerId(int buckerId) {
            this.buckerId = buckerId;
            return this;
        }

        public VideoItemBuilder videoPath(String videoPath) {
            this.videoPath = videoPath;
            return this;
        }

        public VideoItemBuilder thumbNailPath(String thumbNailPath) {
            this.thumbNailPath = thumbNailPath;
            return this;
        }

        public VideoItem createVideoItem() {
            return new VideoItem(id, buckerId, videoPath, thumbNailPath);
        }
    }
}

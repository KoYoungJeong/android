package com.tosslab.jandi.app.ui.album.videoalbum.vo;

/**
 * Created by Steve SeongUg Jung on 15. 6. 15..
 */
public class VideoAlbum {
    private final int _id;
    private final int bucketId;
    private final String buckerName;
    private final String videoPath;
    private final String thumbnailPath;
    private final int count;

    private VideoAlbum(int _id, int bucketId, String buckerName, String videoPath, String thumbnailPath, int count) {
        this._id = _id;
        this.bucketId = bucketId;
        this.buckerName = buckerName;
        this.videoPath = videoPath;
        this.thumbnailPath = thumbnailPath;
        this.count = count;
    }

    public int get_id() {
        return _id;
    }

    public int getCount() {
        return count;
    }

    public int getBucketId() {
        return bucketId;
    }

    public String getBuckerName() {
        return buckerName;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public static class VideoAlbumBuilder {
        private int _id;
        private int bucketId;
        private String buckerName;
        private String videoPath;
        private int count;
        private String thumbnailPath;

        public VideoAlbumBuilder _id(int _id) {
            this._id = _id;
            return this;
        }

        public VideoAlbumBuilder count(int count) {
            this.count = count;
            return this;
        }

        public VideoAlbumBuilder bucketId(int bucketId) {
            this.bucketId = bucketId;
            return this;
        }

        public VideoAlbumBuilder buckerName(String buckerName) {
            this.buckerName = buckerName;
            return this;
        }

        public VideoAlbumBuilder videoPath(String imagePath) {
            this.videoPath = imagePath;
            return this;
        }

        public VideoAlbumBuilder thumbnailPath(String thumbnailPath) {
            this.thumbnailPath = thumbnailPath;
            return this;
        }

        public VideoAlbum createVideoAlbum() {
            return new VideoAlbum(_id, bucketId, buckerName, videoPath, thumbnailPath, count);
        }
    }
}

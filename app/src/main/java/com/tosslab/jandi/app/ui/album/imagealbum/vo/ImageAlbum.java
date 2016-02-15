package com.tosslab.jandi.app.ui.album.imagealbum.vo;

/**
 * Created by Steve SeongUg Jung on 15. 6. 15..
 */
public class ImageAlbum {
    private final int _id;
    private final int bucketId;
    private final String buckerName;
    private final String imagePath;
    private final int count;

    private ImageAlbum(int _id, int bucketId, String buckerName, String imagePath, int count) {
        this._id = _id;
        this.bucketId = bucketId;
        this.buckerName = buckerName;
        this.imagePath = imagePath;
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

    public String getImagePath() {
        return imagePath;
    }

    public static class ImageAlbumBuilder {
        private int _id;
        private int bucketId;
        private String buckerName;
        private String imagePath;
        private int count;

        public ImageAlbumBuilder _id(int _id) {
            this._id = _id;
            return this;
        }

        public ImageAlbumBuilder count(int count) {
            this.count = count;
            return this;
        }

        public ImageAlbumBuilder bucketId(int bucketId) {
            this.bucketId = bucketId;
            return this;
        }

        public ImageAlbumBuilder buckerName(String buckerName) {
            this.buckerName = buckerName;
            return this;
        }

        public ImageAlbumBuilder imagePath(String imagePath) {
            this.imagePath = imagePath;
            return this;
        }

        public ImageAlbum createImageAlbum() {
            return new ImageAlbum(_id, bucketId, buckerName, imagePath, count);
        }
    }
}

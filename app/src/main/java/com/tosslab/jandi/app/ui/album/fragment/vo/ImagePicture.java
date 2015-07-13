package com.tosslab.jandi.app.ui.album.fragment.vo;

/**
 * Created by Steve SeongUg Jung on 15. 6. 15..
 */
public class ImagePicture {

    private final int _id;
    private final int buckerId;
    private final String imagePath;

    private ImagePicture(int _id, int buckerId, String imagePath) {
        this._id = _id;
        this.buckerId = buckerId;
        this.imagePath = imagePath;
    }

    public int get_id() {
        return _id;
    }

    public int getBuckerId() {
        return buckerId;
    }

    public String getImagePath() {
        return imagePath;
    }

    public static class ImagePictureBuilder {
        private int id;
        private int buckerId;
        private String imagePath;

        public ImagePictureBuilder _id(int id) {
            this.id = id;
            return this;
        }

        public ImagePictureBuilder buckerId(int buckerId) {
            this.buckerId = buckerId;
            return this;
        }

        public ImagePictureBuilder imagePath(String imagePath) {
            this.imagePath = imagePath;
            return this;
        }

        public ImagePicture createImagePicture() {
            return new ImagePicture(id, buckerId, imagePath);
        }
    }
}

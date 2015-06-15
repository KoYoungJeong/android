package com.tosslab.jandi.app.ui.album.fragment.vo;

import android.text.TextUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import rx.Observable;

/**
 * Created by Steve SeongUg Jung on 15. 6. 15..
 */
public class SelectPictures {

    private static final ImagePicture NULL_OBJECT = new ImagePicture.ImagePictureBuilder().createImagePicture();

    private static SelectPictures selectPictures;

    private List<ImagePicture> pictures;


    private SelectPictures() {
        pictures = new CopyOnWriteArrayList<ImagePicture>();
    }

    public static SelectPictures getSelectPictures() {
        if (selectPictures == null) {
            selectPictures = new SelectPictures();
        }

        return selectPictures;
    }

    public void addPicture(ImagePicture path) {
        pictures.add(path);
    }

    public void removePicture(ImagePicture path) {
        for (int idx = pictures.size() - 1; idx >= 0; idx--) {
            if (TextUtils.equals(pictures.get(idx).getImagePath(), path.getImagePath())) {
                pictures.remove(idx);
                break;
            }
        }
    }

    public boolean contains(String path) {

        ImagePicture first = Observable.from(pictures)
                .filter(imagePicture -> TextUtils.equals(imagePicture.getImagePath(), path))
                .firstOrDefault(NULL_OBJECT)
                .toBlocking()
                .first();

        return first != NULL_OBJECT;
    }

    public int getCountOfBucket(int buckerId) {
        int count = Observable.from(pictures)
                .filter(imagePicture -> imagePicture.getBuckerId() == buckerId)
                .count()
                .toBlocking()
                .first();

        return count;

    }

    public void clear() {
        pictures.clear();
    }

    public List<ImagePicture> getPictures() {
        return Collections.unmodifiableList(pictures);
    }

}

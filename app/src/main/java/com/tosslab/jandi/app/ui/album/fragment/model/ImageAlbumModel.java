package com.tosslab.jandi.app.ui.album.fragment.model;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewParent;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.album.fragment.ImageAlbumFragment;
import com.tosslab.jandi.app.ui.album.fragment.vo.ImageAlbum;
import com.tosslab.jandi.app.ui.album.fragment.vo.ImagePicture;
import com.tosslab.jandi.app.ui.album.fragment.vo.SelectPictures;

import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.List;

@EBean
public class ImageAlbumModel {

    public List<ImageAlbum> getDefaultAlbumList(Context context) {
        // which image properties are we querying
        String COUNT_COLUMN = "count";
        String[] projection = {
                MediaStore.Images.ImageColumns.BUCKET_ID,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Images.ImageColumns.DATA,
                String.format("COUNT(%s) as %s", MediaStore.Images.ImageColumns.BUCKET_ID, COUNT_COLUMN)};

        String groupBy = "1) GROUP BY (" + MediaStore.Images.ImageColumns.BUCKET_ID;
        String orderBy = String.format("%s DESC, MAX(%s) DESC", MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, MediaStore.Images.ImageColumns.DATE_TAKEN);

        // Get the base URI for the People table in the Contacts content provider.
        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        // Make the query.
        Cursor cursor = context.getContentResolver().query(images,
                projection, // Which columns to return
                groupBy,       // Which rows to return (all rows)
                null,       // Selection arguments (none)
                orderBy        // Ordering
        );

        List<ImageAlbum> albumName = new ArrayList<ImageAlbum>();

        if (cursor == null || cursor.getCount() <= 0) {
            return albumName;
        }

        int idxBucketName = cursor.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME);
        int idxBucketId = cursor.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_ID);
        int idxData = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        int idxCount = cursor.getColumnIndex(COUNT_COLUMN);


        while (cursor.moveToNext()) {
            albumName.add(
                    new ImageAlbum.ImageAlbumBuilder()
                            .buckerName(cursor.getString(idxBucketName))
                            .bucketId(cursor.getInt(idxBucketId))
                            .imagePath(cursor.getString(idxData))
                            .count(cursor.getInt(idxCount))
                            .createImageAlbum()
            );
        }

        cursor.close();

        return albumName;
    }

    public List<ImagePicture> getPhotoList(Context context, int buckerId) {
        // which image properties are we querying
        String[] projection = {
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.BUCKET_ID,
                MediaStore.Images.ImageColumns.DATA};

        String orderBy = String.format("%s DESC", MediaStore.Images.ImageColumns.DATE_TAKEN);

        // Get the base URI for the People table in the Contacts content provider.
        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        // Make the query.
        String selection = String.format("%s = ?", MediaStore.Images.ImageColumns.BUCKET_ID);
        String[] selectionArgs = {String.valueOf(buckerId)};

        Cursor cursor = context.getContentResolver().query(images,
                projection, // Which columns to return
                selection,       // Which rows to return (all rows)
                selectionArgs,       // Selection arguments (none)
                orderBy        // Ordering
        );

        List<ImagePicture> imagePictures = new ArrayList<ImagePicture>();

        if (cursor == null || cursor.getCount() <= 0) {
            return imagePictures;
        }

        int idxId = cursor.getColumnIndex(
                MediaStore.Images.ImageColumns._ID);

        int idxBucketId = cursor.getColumnIndex(
                MediaStore.Images.ImageColumns.BUCKET_ID);

        int idxData = cursor.getColumnIndex(
                MediaStore.Images.ImageColumns.DATA);


        while (cursor.moveToNext()) {
            imagePictures.add(
                    new ImagePicture.ImagePictureBuilder()
                            ._id(cursor.getInt(idxId))
                            .buckerId(cursor.getInt(idxBucketId))
                            .imagePath(cursor.getString(idxData))
                            .createImagePicture()
            );
        }
        cursor.close();

        return imagePictures;

    }

    public boolean isFirstAlbumPage(int buckerId) {
        return buckerId == -1;
    }

    public String getBucketTitle(Context context, int buckerId) {

        String[] projection = {MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME};

        // Get the base URI for the People table in the Contacts content provider.
        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        // Make the query.
        String selection = String.format("%s = ?", MediaStore.Images.ImageColumns.BUCKET_ID);
        String[] selectionArgs = {String.valueOf(buckerId)};
        Cursor cursor = context.getContentResolver().query(images,
                projection, // Which columns to return
                selection,       // Which rows to return (all rows)
                selectionArgs,       // Selection arguments (none)
                null        // Ordering
        );

        if (cursor == null || cursor.getCount() <= 0) {
            return "Unknown";
        }

        cursor.moveToFirst();

        String title = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME));
        cursor.close();

        return title;

    }

    public void toggleImagePath(ImagePicture imagePicture) {
        SelectPictures selectPictures = SelectPictures.getSelectPictures();
        if (selectPictures.contains(imagePicture.getImagePath())) {
            selectPictures.removePicture(imagePicture);
        } else {
            selectPictures.addPicture(imagePicture);
        }
    }


    public int getSelectedImages() {

        return SelectPictures.getSelectPictures().getPictures().size();

    }

    public ImageAlbum createViewAllAlbum(Context context) {


        int albumCount = 0;

        // which image properties are we querying
        String COUNT_COLUMN = "count";
        String[] projection = {String.format("COUNT(%s) as %s", MediaStore.Images.ImageColumns.DATA, COUNT_COLUMN)};

        // Get the base URI for the People table in the Contacts content provider.
        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        // Make the query.
        Cursor cursor = context.getContentResolver().query(images,
                projection, // Which columns to return
                null,       // Which rows to return (all rows)
                null,       // Selection arguments (none)
                null        // Ordering
        );

        if (cursor == null || cursor.getCount() <= 0) {
            return new ImageAlbum.ImageAlbumBuilder()
                    .bucketId(-1)
                    .count(albumCount)
                    .createImageAlbum();
        }

        int idxCount = cursor.getColumnIndex(COUNT_COLUMN);

        cursor.moveToFirst();
        albumCount = cursor.getInt(idxCount);

        cursor.close();

        return new ImageAlbum.ImageAlbumBuilder()
                .bucketId(ImageAlbumFragment.BUCKET_ALL_IMAGE_ALBUM)
                .buckerName(context.getString(R.string.jandi_view_all))
                .count(albumCount)
                .createImageAlbum();
    }

    public boolean isAllAlbum(int buckerId) {
        return buckerId == ImageAlbumFragment.BUCKET_ALL_IMAGE_ALBUM;
    }

    public List<ImagePicture> getAllPhotoList(Context context) {
        // which image properties are we querying
        String[] projection = {
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.BUCKET_ID,
                MediaStore.Images.ImageColumns.DATA};

        String orderBy = String.format("%s DESC", MediaStore.Images.ImageColumns.DATE_TAKEN);

        // Get the base URI for the People table in the Contacts content provider.
        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        Cursor cursor = context.getContentResolver().query(images,
                projection, // Which columns to return
                null,       // Which rows to return (all rows)
                null,       // Selection arguments (none)
                orderBy        // Ordering
        );

        List<ImagePicture> imagePictures = new ArrayList<ImagePicture>();

        if (cursor == null || cursor.getCount() <= 0) {
            return imagePictures;
        }

        int idxId = cursor.getColumnIndex(
                MediaStore.Images.ImageColumns._ID);

        int idxBucketId = cursor.getColumnIndex(
                MediaStore.Images.ImageColumns.BUCKET_ID);

        int idxData = cursor.getColumnIndex(
                MediaStore.Images.ImageColumns.DATA);


        while (cursor.moveToNext()) {
            imagePictures.add(
                    new ImagePicture.ImagePictureBuilder()
                            ._id(cursor.getInt(idxId))
                            .buckerId(cursor.getInt(idxBucketId))
                            .imagePath(cursor.getString(idxData))
                            .createImagePicture()
            );
        }
        cursor.close();

        return imagePictures;

    }

    public int getSelectedBucketImages(int buckerId) {
        return SelectPictures.getSelectPictures().getCountOfBucket(buckerId);
    }

    public Rect getAbsoluteOffset(View childView, Rect rect) {
        ViewParent parent = childView.getParent();
        if (parent != null && parent instanceof View) {
            View parentView = (View) parent;
            rect.set(rect.left + (int) childView.getX(), rect.top + (int) childView.getY(), rect.right + (int) childView.getX(), rect.bottom + (int) childView.getY());
            return getAbsoluteOffset(parentView, rect);
        } else {
            return rect;
        }
    }
}

package com.tosslab.jandi.app.ui.album.imagealbum.model;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.album.imagealbum.ImageAlbumFragment;
import com.tosslab.jandi.app.ui.album.imagealbum.vo.ImageAlbum;
import com.tosslab.jandi.app.ui.album.imagealbum.vo.ImagePicture;
import com.tosslab.jandi.app.ui.album.imagealbum.vo.SelectPictures;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class ImageAlbumModel {

    public static final int LIMIT = 60;

    @Inject
    public ImageAlbumModel() {
    }

    public List<ImageAlbum> getDefaultAlbumList(Context context) {
        // which image properties are we querying
        String COUNT_COLUMN = "count";
        String[] projection = {
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.BUCKET_ID,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Images.ImageColumns.DATA,
                String.format("COUNT(%s) as %s", MediaStore.Images.ImageColumns.BUCKET_ID, COUNT_COLUMN)};

        String groupBy = "1) GROUP BY (" + MediaStore.Images.ImageColumns.BUCKET_ID;
        String orderBy = String.format("%s DESC, MAX(%s) DESC",
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Images.ImageColumns.DATE_TAKEN);

        // Get the base URI for the People table in the Contacts content provider.
        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        // Make the query.
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(images,
                projection, // Which columns to return
                groupBy,       // Which rows to return (all rows)
                null,       // Selection arguments (none)
                orderBy        // Ordering
        );

        List<ImageAlbum> albumName = new ArrayList<ImageAlbum>();

        if (cursor == null || cursor.getCount() <= 0) {
            return albumName;
        }

        int idxId = cursor.getColumnIndex(MediaStore.Images.ImageColumns._ID);
        int idxBucketName = cursor.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME);
        int idxBucketId = cursor.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_ID);
        int idxData = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        int idxCount = cursor.getColumnIndex(COUNT_COLUMN);

        while (cursor.moveToNext()) {
            albumName.add(
                    new ImageAlbum.ImageAlbumBuilder()
                            ._id(cursor.getInt(idxId))
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

    public List<ImagePicture> getPhotoList(Context context, int buckerId, int fromImageId) {
        // which image properties are we querying
        String[] projection = {
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.BUCKET_ID,
                MediaStore.Images.ImageColumns.DATA};

        String orderBy = String.format("%s DESC", MediaStore.Images.ImageColumns._ID);

        // Get the base URI for the People table in the Contacts content provider.
        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                .buildUpon()
                .appendQueryParameter("limit", String.valueOf(LIMIT))
                .build();

        // Make the query.
        String bucketIdSelection = String.format("%s = ?", MediaStore.Images.ImageColumns.BUCKET_ID);
        StringBuilder sb = new StringBuilder(bucketIdSelection);
        if (fromImageId > 0) {
            sb.append(String.format(" and %s < ?", MediaStore.Images.ImageColumns._ID));
        }

        String selection = sb.toString();
        String[] selectionArgs = fromImageId > 0
                ? new String[]{String.valueOf(buckerId), String.valueOf(fromImageId)}
                : new String[]{String.valueOf(buckerId)};

        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(images,
                projection, // Which columns to return
                selection,       // Which rows to return (all rows)
                selectionArgs,       // Selection arguments (none)
                orderBy        // Ordering
        );

        List<ImagePicture> imagePictures = new ArrayList<ImagePicture>();

        if (cursor == null || cursor.getCount() <= 0) {
            return imagePictures;
        }


        int idxId = cursor.getColumnIndex(MediaStore.Images.ImageColumns._ID);

        int idxBucketId = cursor.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_ID);

        int idxData = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);

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

    public boolean isSelectedPicture(ImagePicture imagePicture) {
        SelectPictures selectPictures = SelectPictures.getSelectPictures();
        return selectPictures.contains(imagePicture.getImagePath());
    }

    public int getSelectedImages() {

        return SelectPictures.getSelectPictures().getPictures().size();

    }

    public ImageAlbum createViewAllAlbum(Context context) {
        int albumCount = 0;

        // which image properties are we querying
        String COUNT_COLUMN = "count";
        String[] projection = {
                String.format("COUNT(%s) as %s", MediaStore.Images.ImageColumns.DATA, COUNT_COLUMN),
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATA
        };

        String orderBy = String.format("MAX(%s) DESC", MediaStore.Images.ImageColumns.DATE_TAKEN);
        // Get the base URI for the People table in the Contacts content provider.
        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        // Make the query.
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(images,
                projection, // Which columns to return
                null,       // Which rows to return (all rows)
                null,       // Selection arguments (none)
                orderBy        // Ordering
        );

        if (cursor == null || cursor.getCount() <= 0) {
            return new ImageAlbum.ImageAlbumBuilder()
                    .bucketId(-1)
                    .count(albumCount)
                    .createImageAlbum();
        }


        int idxCount = cursor.getColumnIndex(COUNT_COLUMN);
        int idxId = cursor.getColumnIndex(MediaStore.Images.ImageColumns._ID);
        int idxData = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);

        cursor.moveToFirst();
        int _id = cursor.getInt(idxId);
        albumCount = cursor.getInt(idxCount);
        String firstImagePath = cursor.getString(idxData);

        cursor.close();

        return new ImageAlbum.ImageAlbumBuilder()
                ._id(_id)
                .bucketId(ImageAlbumFragment.BUCKET_ALL_IMAGE_ALBUM)
                .buckerName(context.getString(R.string.jandi_view_all))
                .count(albumCount)
                .imagePath(firstImagePath)
                .createImageAlbum();
    }

    public boolean isAllAlbum(int buckerId) {
        return buckerId == ImageAlbumFragment.BUCKET_ALL_IMAGE_ALBUM;
    }

    public List<ImagePicture> getAllPhotoList(Context context, int fromImageId) {
        // which image properties are we querying
        String[] projection = {
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.BUCKET_ID,
                MediaStore.Images.ImageColumns.DATA};

        String orderBy = String.format("%s DESC", MediaStore.Images.ImageColumns.DATE_TAKEN);

        // Get the base URI for the People table in the Contacts content provider.
        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                .buildUpon()
                .appendQueryParameter("limit", String.valueOf(LIMIT))
                .build();

        String selection = null;
        String[] selectionArgs = null;
        if (fromImageId > 0) {
            selection = String.format("%s < ?", MediaStore.Images.ImageColumns._ID);
            selectionArgs = new String[]{String.valueOf(fromImageId)};
        }

        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(images,
                projection, // Which columns to return
                selection,       // Which rows to return (all rows)
                selectionArgs,       // Selection arguments (none)
                orderBy        // Ordering
        );

        List<ImagePicture> imagePictures = new ArrayList<ImagePicture>();

        if (cursor == null || cursor.getCount() <= 0) {
            return imagePictures;
        }

        int idxId = cursor.getColumnIndex(MediaStore.Images.ImageColumns._ID);

        int idxBucketId = cursor.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_ID);

        int idxData = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);

        while (cursor.moveToNext()) {
            imagePictures.add(
                    new ImagePicture.ImagePictureBuilder()
                            ._id(cursor.getInt(idxId))
                            .buckerId(cursor.getInt(idxBucketId))
                            .imagePath(cursor.getString(idxData))
                            .createImagePicture());
        }
        cursor.close();
        return imagePictures;

    }

    public boolean removeSelectedPicture(ImagePicture item) {
        return SelectPictures.getSelectPictures().removePicture(item);
    }

    public boolean addSelectedPicture(ImagePicture item) {
        return SelectPictures.getSelectPictures().addPicture(item);
    }
}

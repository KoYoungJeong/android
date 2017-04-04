package com.tosslab.jandi.app.ui.album.videoalbum.model;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.album.videoalbum.VideoAlbumFragment;
import com.tosslab.jandi.app.ui.album.videoalbum.vo.SelectVideos;
import com.tosslab.jandi.app.ui.album.videoalbum.vo.VideoAlbum;
import com.tosslab.jandi.app.ui.album.videoalbum.vo.VideoItem;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class VideoAlbumModel {

    public static final int LIMIT = 60;

    @Inject
    public VideoAlbumModel() {
    }

    public List<VideoAlbum> getDefaultVideoAlbumList(Context context) {
        String COUNT_COLUMN = "count";
        String[] projection = {
                MediaStore.Video.VideoColumns._ID,
                MediaStore.Video.VideoColumns.BUCKET_ID,
                MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Video.VideoColumns.DATA,
                String.format("COUNT(%s) as %s", MediaStore.Video.VideoColumns.BUCKET_ID, COUNT_COLUMN)};

        String groupBy = "1) GROUP BY (" + MediaStore.Video.VideoColumns.BUCKET_ID;
        String orderBy = String.format("%s DESC, MAX(%s) DESC",
                MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Video.VideoColumns.DATE_TAKEN);

        // Get the base URI for the People table in the Contacts content provider.
        Uri videos = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        // Make the query.
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(videos,
                projection, // Which columns to return
                groupBy,       // Which rows to return (all rows)
                null,       // Selection arguments (none)
                orderBy        // Ordering
        );

        List<VideoAlbum> albumName = new ArrayList<>();

        if (cursor == null || cursor.getCount() <= 0) {
            return albumName;
        }

        int idxId = cursor.getColumnIndex(MediaStore.Video.VideoColumns._ID);
        int idxBucketName = cursor.getColumnIndex(MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME);
        int idxBucketId = cursor.getColumnIndex(MediaStore.Video.VideoColumns.BUCKET_ID);
        int idxData = cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA);
        int idxCount = cursor.getColumnIndex(COUNT_COLUMN);

        while (cursor.moveToNext()) {
            albumName.add(
                    new VideoAlbum.VideoAlbumBuilder()
                            ._id(cursor.getInt(idxId))
                            .buckerName(cursor.getString(idxBucketName))
                            .bucketId(cursor.getInt(idxBucketId))
                            .videoPath(cursor.getString(idxData))
                            .thumbnailPath(getThumbnailPath(context, cursor.getInt(idxId)))
                            .count(cursor.getInt(idxCount))
                            .createVideoAlbum()
            );
        }

        cursor.close();

        return albumName;
    }

    public List<VideoItem> getVideoList(Context context, int buckerId, int fromVideoId) {
        String[] projection = {
                MediaStore.Video.VideoColumns._ID,
                MediaStore.Video.VideoColumns.BUCKET_ID,
                MediaStore.Video.VideoColumns.DATA};

        String orderBy = String.format("%s DESC", MediaStore.Video.VideoColumns._ID);

        // Get the base URI for the People table in the Contacts content provider.
        Uri videos = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                .buildUpon()
                .appendQueryParameter("limit", String.valueOf(LIMIT))
                .build();

        // Make the query.
        String bucketIdSelection = String.format("%s = ?", MediaStore.Video.VideoColumns.BUCKET_ID);
        StringBuilder sb = new StringBuilder(bucketIdSelection);
        if (fromVideoId > 0) {
            sb.append(String.format(" and %s < ?", MediaStore.Video.VideoColumns._ID));
        }

        String selection = sb.toString();
        String[] selectionArgs = fromVideoId > 0
                ? new String[]{String.valueOf(buckerId), String.valueOf(fromVideoId)}
                : new String[]{String.valueOf(buckerId)};

        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(videos,
                projection, // Which columns to return
                selection,       // Which rows to return (all rows)
                selectionArgs,       // Selection arguments (none)
                orderBy        // Ordering
        );

        List<VideoItem> VideoItems = new ArrayList<>();

        if (cursor == null || cursor.getCount() <= 0) {
            return VideoItems;
        }

        int idxId = cursor.getColumnIndex(MediaStore.Video.VideoColumns._ID);

        int idxBucketId = cursor.getColumnIndex(MediaStore.Video.VideoColumns.BUCKET_ID);

        int idxData = cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA);

        while (cursor.moveToNext()) {
            VideoItems.add(
                    new VideoItem.VideoItemBuilder()
                            ._id(cursor.getInt(idxId))
                            .buckerId(cursor.getInt(idxBucketId))
                            .videoPath(cursor.getString(idxData))
                            .thumbNailPath(getThumbnailPath(context, cursor.getInt(idxId)))
                            .createVideoItem()
            );
        }
        cursor.close();
        return VideoItems;
    }

    public String getThumbnailPath(Context context, long videoId) {
        String[] thumbColumns = {MediaStore.Video.Thumbnails.DATA};

        MediaStore.Video.Thumbnails.getThumbnail(context.getContentResolver(),
                videoId, MediaStore.Video.Thumbnails.MICRO_KIND, null);

        Cursor thumbCursor = null;
        try {
            thumbCursor = context.getContentResolver().query(
                    MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI,
                    thumbColumns, MediaStore.Video.Thumbnails.VIDEO_ID + " = "
                            + videoId, null, null);

            if (thumbCursor.moveToFirst()) {
                String thumbPath = thumbCursor.getString(thumbCursor
                        .getColumnIndex(MediaStore.Video.Thumbnails.DATA));
                return thumbPath;
            }

        } finally {
        }

        return "";
    }

    public boolean isFirstAlbumPage(int buckerId) {
        return buckerId == -1;
    }

    public boolean isSelectedVideo(VideoItem videoItem) {
        SelectVideos selectVideos = SelectVideos.getSelectVideos();
        return selectVideos.contains(videoItem.getVideoPath());
    }

    public int getSelectedVideos() {
        return SelectVideos.getSelectVideos().getVideos().size();
    }

    public VideoAlbum createViewAllAlbum(Context context) {
        int albumCount = 0;

        String COUNT_COLUMN = "count";
        String[] projection = {
                String.format("COUNT(%s) as %s", MediaStore.Video.VideoColumns.DATA, COUNT_COLUMN),
                MediaStore.Video.VideoColumns._ID,
                MediaStore.Video.VideoColumns.DATA
        };

        String orderBy = String.format("MAX(%s) DESC", MediaStore.Video.VideoColumns.DATE_TAKEN);
        // Get the base URI for the People table in the Contacts content provider.
        Uri videos = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        // Make the query.
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(videos,
                projection, // Which columns to return
                null,       // Which rows to return (all rows)
                null,       // Selection arguments (none)
                orderBy        // Ordering
        );

        if (cursor == null || cursor.getCount() <= 0) {
            return new VideoAlbum.VideoAlbumBuilder()
                    .bucketId(-1)
                    .count(albumCount)
                    .createVideoAlbum();
        }


        int idxCount = cursor.getColumnIndex(COUNT_COLUMN);
        int idxId = cursor.getColumnIndex(MediaStore.Video.VideoColumns._ID);
        int idxData = cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA);

        cursor.moveToFirst();
        int _id = cursor.getInt(idxId);
        albumCount = cursor.getInt(idxCount);
        String firstVideoPath = cursor.getString(idxData);

        cursor.close();

        return new VideoAlbum.VideoAlbumBuilder()
                ._id(_id)
                .bucketId(VideoAlbumFragment.BUCKET_ALL_VIDEO_ALBUM)
                .buckerName(context.getString(R.string.jandi_view_all))
                .count(albumCount)
                .videoPath(firstVideoPath)
                .thumbnailPath(getThumbnailPath(context, idxData))
                .createVideoAlbum();
    }

    public boolean isAllAlbum(int buckerId) {
        return buckerId == VideoAlbumFragment.BUCKET_ALL_VIDEO_ALBUM;
    }

    public List<VideoItem> getAllVideoList(Context context, int fromVideoId) {
        String[] projection = {
                MediaStore.Video.VideoColumns._ID,
                MediaStore.Video.VideoColumns.BUCKET_ID,
                MediaStore.Video.VideoColumns.DATA};

        String orderBy = String.format("%s DESC", MediaStore.Video.VideoColumns.DATE_TAKEN);

        // Get the base URI for the People table in the Contacts content provider.
        Uri videos = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                .buildUpon()
                .appendQueryParameter("limit", String.valueOf(LIMIT))
                .build();

        String selection = null;
        String[] selectionArgs = null;
        if (fromVideoId > 0) {
            selection = String.format("%s < ?", MediaStore.Video.VideoColumns._ID);
            selectionArgs = new String[]{String.valueOf(fromVideoId)};
        }

        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(videos,
                projection, // Which columns to return
                selection,       // Which rows to return (all rows)
                selectionArgs,       // Selection arguments (none)
                orderBy        // Ordering
        );

        List<VideoItem> videoItems = new ArrayList<>();

        if (cursor == null || cursor.getCount() <= 0) {
            return videoItems;
        }

        int idxId = cursor.getColumnIndex(MediaStore.Video.VideoColumns._ID);

        int idxBucketId = cursor.getColumnIndex(MediaStore.Video.VideoColumns.BUCKET_ID);

        int idxData = cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA);

        while (cursor.moveToNext()) {
            videoItems.add(
                    new VideoItem.VideoItemBuilder()
                            ._id(cursor.getInt(idxId))
                            .buckerId(cursor.getInt(idxBucketId))
                            .videoPath(cursor.getString(idxData))
                            .thumbNailPath(getThumbnailPath(context, cursor.getInt(idxId)))
                            .createVideoItem());
        }
        cursor.close();
        return videoItems;
    }

    public boolean removeSelectedVideo(VideoItem item) {
        return SelectVideos.getSelectVideos().removeVideos(item);
    }

    public boolean putSelectedVideo(VideoItem item) {
        SelectVideos.getSelectVideos().clear();
        return SelectVideos.getSelectVideos().addVideo(item);
    }
}

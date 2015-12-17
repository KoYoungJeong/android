package com.tosslab.jandi.app.utils;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.MediaStore;

import com.facebook.common.util.UriUtil;

/**
 * Created by tonyjs on 15. 12. 8..
 */
public class UriFactory {

    public static Uri getFileUri(String filePath) {
        Uri uri = Uri.parse(filePath);

        if (!UriUtil.isLocalFileUri(uri)) {
            uri = new Uri.Builder()
                    .scheme(ContentResolver.SCHEME_FILE)
                    .path(filePath)
                    .build();
        }
        return uri;
    }

    public static Uri getContentUri(int imageId) {
        return Uri.withAppendedPath(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(imageId));
    }

    public static Uri getResourceUri(int resId) {
        return new Uri.Builder()
                .scheme(UriUtil.LOCAL_RESOURCE_SCHEME)
                .path(String.valueOf(resId))
                .build();
    }
}

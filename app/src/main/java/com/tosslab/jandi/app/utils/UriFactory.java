package com.tosslab.jandi.app.utils;

import android.net.Uri;

import com.facebook.common.util.UriUtil;

/**
 * Created by tonyjs on 15. 12. 8..
 */
public class UriFactory {

    public static Uri getFileUri(String filePath) {
        Uri uri = Uri.parse(filePath);

        if (!UriUtil.isLocalFileUri(uri)) {
            uri = new Uri.Builder()
                    .scheme(UriUtil.LOCAL_FILE_SCHEME)
                    .path(filePath)
                    .build();
        }
        return uri;
    }

    public static Uri getResourceUri(int resId) {
        return new Uri.Builder()
                .scheme(UriUtil.LOCAL_RESOURCE_SCHEME)
                .path(String.valueOf(resId))
                .build();
    }
}

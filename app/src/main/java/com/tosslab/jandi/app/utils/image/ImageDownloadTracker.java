package com.tosslab.jandi.app.utils.image;

import android.net.Uri;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tonyjs on 16. 1. 19..
 */
public class ImageDownloadTracker {
    public enum Status {
        PENDING, IN_PROGRESS, COMPLETED
    }

    private static ImageDownloadTracker sInstance;

    private Map<Uri, Status> statusMap;

    private ImageDownloadTracker() {
        statusMap = Collections.synchronizedMap(new HashMap<>());
    }

    public static ImageDownloadTracker getInstance() {
        if (sInstance == null) {
            sInstance = new ImageDownloadTracker();
        }
        return sInstance;
    }

    public void put(Uri uri, Status status) {
        statusMap.put(uri, status);
    }

    public Status getStatus(Uri uri) {
        if (!statusMap.containsKey(uri)) {
            Status status = Status.PENDING;
            put(uri, status);
            return status;
        }

        return statusMap.get(uri);
    }

}

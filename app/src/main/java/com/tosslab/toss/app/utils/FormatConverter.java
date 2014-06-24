package com.tosslab.toss.app.utils;

import com.tosslab.toss.app.network.models.ResMessages;

/**
 * Created by justinygchoi on 2014. 6. 24..
 */
public class FormatConverter {
    static final int MB = 1024 * 1024;
    static final int KB = 1024;

    public static String formatFileSize(int byteSize) {

        if (byteSize > MB) {
            float f = (float)byteSize / MB;
            return String.format("%.2f MB", f);
        } else if (byteSize > KB) {
            return (byteSize / KB) + " KB";
        } else {
            return byteSize + " bytes";
        }
    }
}

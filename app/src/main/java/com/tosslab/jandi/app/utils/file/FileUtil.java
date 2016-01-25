package com.tosslab.jandi.app.utils.file;

import android.os.Environment;
import android.text.TextUtils;

import java.text.DecimalFormat;

/**
 * Created by Bill MinWook Heo on 15. 6. 24..
 */
public class FileUtil {

    /**
     * @return /sdcard/DOWNLOAD/Jandi
     */
    public static String getDownloadPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Environment.DIRECTORY_DOWNLOADS + "/Jandi";
    }

    public static String getTempDownloadPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Environment.DIRECTORY_DOWNLOADS + "/Jandi/temp";
    }

    public static String fileSizeCalculation(long fileSize) {
        String retFormat = "0";
        long size = fileSize;
        String[] s = {"bytes", "KB", "MB", "GB", "TB", "PB"};

        if (fileSize != 0) {
            int idx = (int) Math.floor(Math.log(size) / Math.log(1024));
            DecimalFormat df = new DecimalFormat("#,###");
            double ret = ((size / Math.pow(1024, Math.floor(idx))));
            retFormat = df.format(ret) + " " + s[idx];
        } else {
            retFormat += " " + s[0];
        }
        return retFormat;
    }

    public static String getDownloadFileName(String fileName, String ext) {
        String downloadFileName;
        if (!hasFileExt(fileName) && !TextUtils.isEmpty(ext)) {
            downloadFileName = fileName + "." + ext;
        } else {
            downloadFileName = fileName;
        }
        return downloadFileName;
    }

    private static boolean hasFileExt(String fileName) {
        return !TextUtils.isEmpty(fileName) && fileName.lastIndexOf(".") > 0;
    }
}

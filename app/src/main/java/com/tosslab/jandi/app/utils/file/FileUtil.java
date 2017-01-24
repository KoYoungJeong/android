package com.tosslab.jandi.app.utils.file;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;

import java.io.File;

public class FileUtil {

    private static final double BYTE_UNIT = 1024;
    private static final String[] FILE_SIZE_UNIT = {"Bytes", "KB", "MB", "GB"};
    private static final String[] FILE_SIZE_FORMAT = {"####", "####", "####.#", "####.#"};


    public static String getDownloadPath() {
        String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Environment.DIRECTORY_DOWNLOADS;
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dirPath;
    }

    public static String getCacheDir() {
        File cacheDir = JandiApplication.getContext().getCacheDir();
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        return cacheDir.getAbsolutePath();
    }

    public static String getCacheDir(String childPath) {
        String cacheDir = getCacheDir();
        String childDir = cacheDir + "/" + childPath;
        File child = new File(childDir);
        if (!child.exists()) {
            child.mkdirs();
        }
        return childDir;
    }

    public static String formatFileSize(long fileSize) {
        double tempSize = fileSize;
        int divideCount = 0;
        while (tempSize >= BYTE_UNIT && divideCount < FILE_SIZE_FORMAT.length - 1) {
            tempSize = tempSize / BYTE_UNIT;
            divideCount++;
        }

//        return new DecimalFormat(FILE_SIZE_FORMAT[divideCount]).format(tempSize) + " " + FILE_SIZE_UNIT[divideCount];

        if (divideCount <= 1) {
            return String.format("%d %s", (long) tempSize, FILE_SIZE_UNIT[divideCount]);
        } else {
            return String.format("%.1f %s", tempSize, FILE_SIZE_UNIT[divideCount]);
        }
    }

    public static String getDownloadFileName(String fileName, String ext) {
        String downloadFileName;
        if (!hasFileExt(fileName) && !TextUtils.isEmpty(ext)) {
            downloadFileName = convertAvailableFileName(fileName) + "." + ext;
        } else {
            downloadFileName = convertAvailableFileName(fileName);
        }
        return downloadFileName;
    }

    public static String convertAvailableFileName(String fileName) {
        return fileName
                .replace("\\", "_")
                .replace("/", "_")
                .replace(":", "_")
                .replace("*", "_")
                .replace("?", "_")
                .replace("\"", "_")
                .replace("<", "_")
                .replace(">", "_")
                .replace("|", "_");
    }

    private static boolean hasFileExt(String fileName) {
        return !TextUtils.isEmpty(fileName) && fileName.lastIndexOf(".") > 0;
    }

    public static String getFileName(String fileName, String ext) {
        if (hasFileExt(fileName)) {
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        } else {
            return "";
        }
    }

    public static Intent createFileIntent(File file, String mimeType) {
        Context context = JandiApplication.getContext();
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri data = Uri.fromFile(file);
        intent.setDataAndType(data, mimeType)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        if (context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).size() <= 0) {
            Uri uri = Uri.fromFile(file);
            intent.setDataAndType(uri, mimeType);
            intent.setFlags(0);
        }

        return intent;

    }
}

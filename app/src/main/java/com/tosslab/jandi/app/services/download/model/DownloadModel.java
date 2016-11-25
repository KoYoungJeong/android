package com.tosslab.jandi.app.services.download.model;

import android.content.ContentValues;
import android.content.Intent;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.domain.DownloadInfo;
import com.tosslab.jandi.app.local.orm.repositories.DownloadRepository;
import com.tosslab.jandi.app.services.download.DownloadController;
import com.tosslab.jandi.app.services.download.DownloadService;
import com.tosslab.jandi.app.services.download.domain.DownloadFileInfo;
import com.tosslab.jandi.app.utils.file.FileUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import java.io.File;
import java.util.Random;

public class DownloadModel {

    public static DownloadFileInfo getDownloadInfo(Intent intent) {
        final long fileId = intent.getLongExtra(DownloadService.KEY_FILE_ID, DownloadService.NONE_FILE_ID);
        final String fileUrl = intent.getStringExtra(DownloadService.KEY_FILE_URL);
        final String fileName = intent.getStringExtra(DownloadService.KEY_FILE_NAME);
        final String fileExt = intent.getStringExtra(DownloadService.KEY_FILE_EXTENSIONS);
        final String fileType = intent.getStringExtra(DownloadService.KEY_FILE_TYPE);
        return new DownloadFileInfo(fileId, fileUrl, fileName, fileExt, fileType);
    }

    public static boolean upsertDownloadInfo(int notificationId, String fileName, int state) {
        DownloadInfo downloadInfo = new DownloadInfo();
        downloadInfo.setNotificationId(notificationId);
        downloadInfo.setState(state);
        downloadInfo.setFileName(fileName);
        return DownloadRepository.getInstance().upsertDownloadInfo(downloadInfo);
    }

    public static int deleteDownloadInfo(int notificationId) {
        return DownloadRepository.getInstance().deleteDownloadInfo(notificationId);
    }

    /**
     * 다운로드 중인 것이 있다는 의미는 다운로드 중 서비스가 종료 후 재시작됐다는 의미임
     */
    public static boolean isRestart() {
        return !DownloadRepository.getInstance().getDownloadInfosInProgress().isEmpty();
    }

    public static boolean isMediaFile(String fileType) {
        if (TextUtils.isEmpty(fileType)) {
            return false;
        }

        return fileType.startsWith("audio") || fileType.startsWith("video") || fileType.startsWith("image");
    }

    public static File getDownloadTargetFile(File dir, String fileName, String fileExt) {
        File file = new File(dir, FileUtil.getDownloadFileName(fileName, fileExt));
        if (file.exists()) {
            file = getDuplicatedFile(file);
        }
        return file;
    }

    /**
     * 같은 이름을 가진 파일이 이미 있는 경우 다음과 같이 만들어주기 위함
     * image.png > image(1).png
     */
    private static File getDuplicatedFile(File file) {
        String fileNameWithExt = file.getName();

        String[] split = fileNameWithExt.split("\\.");
        File dir = file.getParentFile();
        if (split.length <= 0) {
            return getDuplicatedFileWithoutExtensions(dir, fileNameWithExt);
        }

        StringBuilder sb = new StringBuilder();
        int untilFileNameLength = split.length - 1;
        for (int i = 0; i < untilFileNameLength; i++) {
            sb.append(split[i]);
            if (i < untilFileNameLength - 1) {
                sb.append("\\.");
            }
        }

        String fileName = sb.toString();
        String fileExtensions = split[untilFileNameLength];
        String newFileNameFormat = "%s(%d).%s";

        return getDuplicatedFile(dir, fileName, fileExtensions, newFileNameFormat);
    }

    private static File getDuplicatedFile(File dir,
                                          String fileName, String fileExtensions, String newFileNameFormat) {
        File newFile;
        int duplicatedId = 1;
        while (true) {
            String newFileName = String.format(newFileNameFormat, fileName, duplicatedId, fileExtensions);
            newFile = new File(dir, newFileName);
            if (!newFile.exists()) {
                break;
            }
            duplicatedId++;
        }
        return newFile;
    }

    private static File getDuplicatedFileWithoutExtensions(File dir, String fileName) {
        File newFile;
        int duplicatedId = 1;
        while (true) {
            String newFileName = String.format("%s(%d)", fileName, duplicatedId);
            newFile = new File(dir, newFileName);
            if (!newFile.exists()) {
                break;
            }
            duplicatedId++;
        }
        return newFile;
    }

    public static String getDownloadUrl(String fileUrl) {
        boolean hasDownloadPath = fileUrl.lastIndexOf("/download") > 0;
        return hasDownloadPath ? fileUrl : fileUrl + "/download";
    }

    public static boolean isNetworkConnected() {
        return NetworkCheckUtil.isConnected();
    }

    public static File makeDirIfNotExistsAndGet() {
        File dir =
                new File(FileUtil.getDownloadPath());
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static int getNotificationId() {
        return Math.abs(new Random().nextInt());
    }

    public static void logDownloadException(Exception e) {
        LogUtil.e(DownloadController.TAG, Log.getStackTraceString(e));
    }

    // Image정보를 Content Provider에 넣기 위한 코드
    public static void addToGalleryIfFileIsImage(File image, String fileType) {
        if (!isMediaFile(fileType)) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, image.getName());
        values.put(MediaStore.Images.Media.DISPLAY_NAME, image.getName());
        values.put(MediaStore.Images.Media.DESCRIPTION, "");
        values.put(MediaStore.Images.Media.MIME_TYPE, fileType);
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.DATA, image.getAbsolutePath());

        JandiApplication.getContext()
                .getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    public static void logUnknownException() {
        LogUtil.e(DownloadController.TAG, "intent is empty.");
    }

    public static void logNetworkIsNotConnected() {
        LogUtil.e(DownloadController.TAG, "Network is not connected.");
    }

    public static boolean isValidateArguments(DownloadFileInfo downloadFileInfo) {
        return downloadFileInfo.getFileId() != DownloadService.NONE_FILE_ID
                && !TextUtils.isEmpty(downloadFileInfo.getFileUrl()) && !TextUtils.isEmpty(downloadFileInfo.getFileName())
                && !TextUtils.isEmpty(downloadFileInfo.getFileExt()) && !TextUtils.isEmpty(downloadFileInfo.getFileType());
    }

    public static void logArgumentsException(DownloadFileInfo downloadFileInfo) {
        String invalidateArgs =
                "Check your arguments - " +
                        "fileId(%d), fileUrl(%s), fileName(%s), fileExt(%s), fileType(%s)";
        String format = String.format(invalidateArgs,
                downloadFileInfo.getFileId(),
                downloadFileInfo.getFileUrl(),
                downloadFileInfo.getFileName(),
                downloadFileInfo.getFileExt(),
                downloadFileInfo.getFileType());
        LogUtil.e(DownloadController.TAG, format);
    }
}

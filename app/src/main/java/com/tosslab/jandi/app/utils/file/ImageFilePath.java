package com.tosslab.jandi.app.utils.file;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.tosslab.jandi.app.services.download.model.DownloadModel;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageFilePath {
    private static final String TEMP_PHOTO_FILE = "temp.jpg";   // 임시 저장파일

    /**
     * 임시 저장 파일의 경로를 반환
     */
    public static Uri getTempUri(Context context) {
        return Uri.fromFile(getTempFile(context));
    }

    /**
     * 외장메모리에 임시 이미지 파일을 생성하여 그 파일의 경로를 반환
     */
    public static File getTempFile(Context context) {
        if (isSDCARDMOUNTED()) {
            String dirPath = getTempPath(context);

            File f = new File(dirPath);
            if (!f.getParentFile().exists()) {
                f.getParentFile().mkdirs();
            }

            try {
                f.delete();
                f.createNewFile();      // 외장메모리에 temp.png 파일 생성
            } catch (IOException e) {
            }

            return f;
        } else
            return null;
    }

    public static String getTempPath(Context context) {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/" + context.getPackageName() + "/temp/" + TEMP_PHOTO_FILE;
    }

    /**
     * SD카드가 마운트 되어 있는지 확
     */
    private static boolean isSDCARDMOUNTED() {
        String status = Environment.getExternalStorageState();
        return status.equals(Environment.MEDIA_MOUNTED);

    }

    /**
     * Method for return file path of Gallery image
     *
     * @param context
     * @param uri
     * @return path of the selected image file from gallery
     */
    @SuppressLint("NewApi")
    public static String getPath(final Context context, final Uri uri) {

        //check here to KITKAT or new version
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {

            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                } else {
                    return "";
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Return the remote address
            if (isGoogleOldPhotosUri(uri)) {
                return uri.getLastPathSegment();
            }

            String path = getDataColumn(context, uri, null, null);

            if (!TextUtils.isEmpty(path)) {
                return path;
            } else {
                String fileName = getFileNameFromFileInfo(context, uri);
                if (!TextUtils.isEmpty(fileName)) {
                    return copyFile(context, uri, fileName);
                } else {
                    fileName = getFileNameFromUri(uri);
                    if (uri != null && !TextUtils.isEmpty(fileName)) {
                        return copyFile(context, uri, fileName);
                    }
                }
            }
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    private static String getFileNameFromUri(Uri uri) {
        String fileName = "";
        int i = uri.toString().lastIndexOf('/');
        if (i > 0) {
            fileName = uri.toString().substring(i + 1);
        }
        return fileName;
    }

    private static String copyFile(Context context, Uri uri, String fileInfo) {

        String fileName = fileInfo;

        String filePath;
        InputStream inputStream = null;
        BufferedOutputStream outStream = null;
        try {
            inputStream = context.getContentResolver().openInputStream(uri);

            File dir = new File(FileUtil.getDownloadPath());
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File downloadFile = DownloadModel.getDownloadTargetFile(dir, fileName, "");
            downloadFile.createNewFile();
            filePath = downloadFile.getAbsolutePath();
            outStream = new BufferedOutputStream(new FileOutputStream(filePath));

            byte[] buf = new byte[2048];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                outStream.write(buf, 0, len);
            }

        } catch (Exception e) {
            e.printStackTrace();
            filePath = "";
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (outStream != null) {
                    outStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return filePath;
    }

    private static String getFileNameFromFileInfo(Context context, Uri uri) {

        Cursor cursor = null;
        String displayNameCol = OpenableColumns.DISPLAY_NAME;
        String mimeTypeCol = "mime_type";
        String[] projection = {displayNameCol, mimeTypeCol};

        try {
            cursor = context.getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                String fileName = cursor.getString(0);
                String mimeType = cursor.getString(1);
                String fileExt;
                String tempFileExt = getFileExtension(fileName);
                if (tempFileExt.length() == 3 || tempFileExt.length() == 4) {
                    fileExt = "";
                } else if ((!TextUtils.isEmpty(mimeType) && !TextUtils.equals("null", mimeType))) {
                    String extensionFromMimeType = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
                    if (!TextUtils.isEmpty(extensionFromMimeType)
                            && fileName.toLowerCase().lastIndexOf(extensionFromMimeType) <= 0) {
                        fileExt = "." + extensionFromMimeType;
                    } else {
                        fileExt = "";
                    }
                } else {
                    fileExt = "";
                }
                return fileName + fileExt;
            }
        } catch (Exception e) {
            return "";
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return "";
    }

    private static String getFileExtension(String fileName) {
        String extension = "";
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i + 1);
        }
        return extension;
    }

//    private static String copyFile(Context context, Uri uri) {
//        String filePath;
//        InputStream inputStream = null;
//        BufferedOutputStream outStream = null;
//        try {
//            inputStream = context.getContentResolver().openInputStream(uri);
//
//            filePath = FileUtil.getDownloadPath() + "/" + GoogleImagePickerUtil
//                    .getWebImageName();
//            outStream = new BufferedOutputStream(new FileOutputStream
//                    (filePath));
//
//            byte[] buf = new byte[2048];
//            int len;
//            while ((len = inputStream.read(buf)) > 0) {
//                outStream.write(buf, 0, len);
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            filePath = "";
//        } finally {
//            try {
//                if (inputStream != null) {
//                    inputStream.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            try {
//                if (outStream != null) {
//                    outStream.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        return filePath;
//    }
//
//    private static boolean isPicasaPhotoUri(Uri uri) {
//
//        return uri != null
//                && !TextUtils.isEmpty(uri.getAuthority())
//                && uri.getAuthority().contains("android.gallery3d");
//    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private static String getDataColumn(Context context, Uri uri, String selection,
                                        String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } catch (Exception e) {
            return "";
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return "";
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    private static boolean isGoogleOldPhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is New Google Photos.
     */
    public static boolean isGoogleNewPhotosUri(Uri uri) {
        return "com.google.android.apps.photos.contentprovider".equals(uri.getAuthority());
    }


}
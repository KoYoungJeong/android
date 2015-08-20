package com.tosslab.jandi.app.ui.message.model;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import com.koushikdutta.ion.builder.Builders;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.files.ConfirmFileUploadEvent;
import com.tosslab.jandi.app.files.upload.FilePickerViewModel;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.ui.message.to.ChattingInfomations;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.UserAgentUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URLConnection;

/**
 * Created by Steve SeongUg Jung on 14. 12. 10..
 */
@Deprecated
public class FileUploadUtil {

    public static void uploadStart(ConfirmFileUploadEvent event, Context context, ChattingInfomations chattingInfomations, final UploadCallback uploadCallback) {

        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage(context.getString(R.string.jandi_file_uploading) + " " + event.realFilePath);
        progressDialog.show();

        File uploadFile = new File(event.realFilePath);
        String requestURL = JandiConstantsForFlavors.SERVICE_INNER_API_URL + "/v2/file";
        String permissionCode = (chattingInfomations.isPublicTopic()) ? "744" : "740";
        Builders.Any.M ionBuilder
                = Ion
                .with(context)
                .load(requestURL)
                .uploadProgressDialog(progressDialog)
                .progress(new ProgressCallback() {
                    @Override
                    public void onProgress(long downloaded, long total) {

                        progressDialog.setProgress((int) (downloaded / total));
                    }
                })
                .setHeader(JandiConstants.AUTH_HEADER, TokenUtil.getRequestAuthentication().second)
                .setHeader("Accept", JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
                .setHeader("User-Agent", UserAgentUtil.getDefaultUserAgent(context))
                .setMultipartParameter("title", uploadFile.getName())
                .setMultipartParameter("share", "" + event.entityId)
                .setMultipartParameter("permission", permissionCode)
                .setMultipartParameter("teamId", String.valueOf(AccountRepository.getRepository().getSelectedTeamInfo().getTeamId()));

        // Comment가 함께 등록될 경우 추가
        if (event.comment != null && !event.comment.isEmpty()) {
            ionBuilder.setMultipartParameter("comment", event.comment);
        }

        ionBuilder.setMultipartFile("userFile", URLConnection.guessContentTypeFromName(uploadFile.getName()), uploadFile)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception exception, JsonObject result) {
                        progressDialog.dismiss();
                        if (exception != null) {
                            LogUtil.e("uploadFileDone: FAILED", exception);

                            if (uploadCallback != null) {
                                uploadCallback.onUploadFail();
                            }
                        } else if (result.get("code") != null) {
                            LogUtil.e("uploadFileDone: " + result.get("code").toString());
                            if (uploadCallback != null) {
                                uploadCallback.onUploadFail();
                            }
                        } else {
                            if (uploadCallback != null) {
                                uploadCallback.onUploadSuccess(result);
                            }
                        }
                    }
                });
    }

    public static String getUploadFilePathFromActivityResult(Context context, int requestCode, Intent data, Uri cameraRequestPath) {

        String realFilePath = "";
        switch (requestCode) {
            case FilePickerViewModel.TYPE_UPLOAD_GALLERY:
                Uri targetUri = data.getData();
                if (targetUri != null) {
                    realFilePath = getRealPathFromUri(context, targetUri);
                    LogUtil.d("onActivityResult : Photo URI : " + targetUri.toString()
                            + ", FilePath : " + realFilePath);
                }
                break;
            case FilePickerViewModel.TYPE_UPLOAD_EXPLORER:
                String path = data.getStringExtra("GetPath");
                realFilePath = path + File.separator + data.getStringExtra("GetFileName");
                LogUtil.d("onActivityResult : from Explorer : " + realFilePath);
                break;
            case FilePickerViewModel.TYPE_UPLOAD_TAKE_PHOTO:
                Uri imageUri = (cameraRequestPath != null)
                        ? cameraRequestPath
                        : data.getData();
                // 비트맵으로 리턴이 되는 경우
                if (imageUri == null) {
                    Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                    imageUri = createCacheFile(context);
                    bitmapSaveToFileCache(imageUri, bitmap, 100);
                }
                realFilePath = imageUri.getPath();
                break;
            default:
                break;
        }


        return realFilePath;

    }

    public static Uri createCacheFile(Context context) {
        String url = "tmp_" + String.valueOf(System.currentTimeMillis() + ".jpg");
        return Uri.fromFile(new File(context.getExternalCacheDir(), url));
    }

    public static File bitmapSaveToFileCache(Uri uri, Bitmap bitmap, int quality) {
        FileOutputStream fos = null;
        File file = null;
        try {
            file = new File(uri.getPath());
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (Exception e) {
                // DO NOTHING
            }
        }
        return file;
    }

    public static String getRealPathFromUri(Context mContext1, Uri contentUri) {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = mContext1.getContentResolver().query(
                contentUri,
                filePathColumn,   // Which columns to return
                null,   // WHERE clause; which rows to return (all rows)
                null,   // WHERE clause selection arguments (none)
                null);  // Order-by clause (ascending by name)
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();
        return picturePath;
    }

    public interface UploadCallback {
        void onUploadFail();

        void onUploadSuccess(JsonObject result);
    }

}

package com.tosslab.jandi.app.files.upload.model;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;

import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import com.koushikdutta.ion.builder.Builders;
import com.koushikdutta.ion.future.ResponseFuture;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.network.spring.JandiV2HttpMessageConverter;
import com.tosslab.jandi.app.ui.album.ImageAlbumActivity;
import com.tosslab.jandi.app.ui.fileexplorer.FileExplorerActivity;
import com.tosslab.jandi.app.utils.ImageFilePath;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.annotations.EBean;
import org.apache.http.client.methods.HttpPut;
import org.json.JSONException;

import java.io.File;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Steve SeongUg Jung on 15. 6. 12..
 */
@EBean
public class FilePickerModel {

    public static final long MAX_FILE_SIZE = 1024 * 1024 * 300;

    public String getFilePath(Context context, int requestCode, Intent intent, File filePath) {
        String realFilePath;
        switch (requestCode) {
            case JandiConstants.TYPE_UPLOAD_GALLERY:

                if (intent == null) {
                    return "";
                }
                Uri data = intent.getData();

                if (data != null) {
                    realFilePath = ImageFilePath.getPath(context, data);

                    return realFilePath;
                } else {
                    return "";
                }

            case JandiConstants.TYPE_UPLOAD_TAKE_PHOTO:
                if (filePath == null) {
                    LogUtil.e("filePath object is null...");
                    return "";
                }
                if (!filePath.exists()) {
                    LogUtil.e("filePath is not exists");
                    return "";
                }

                return filePath.getAbsolutePath();

            case JandiConstants.TYPE_UPLOAD_EXPLORER:

                realFilePath = intent.getStringExtra("GetPath") + File.separator + intent.getStringExtra("GetFileName");
                return realFilePath;
            default:
                return "";
        }
    }

    public void openExplorerForActivityResult(Fragment fragment) {
        Intent intent = new Intent(fragment.getActivity().getApplicationContext(), FileExplorerActivity.class);
        fragment.startActivityForResult(intent, JandiConstants.TYPE_UPLOAD_EXPLORER);
    }

    public void openExplorerForActivityResult(Activity activity) {
        Intent intent = new Intent(activity, FileExplorerActivity.class);
        activity.startActivityForResult(intent, JandiConstants.TYPE_UPLOAD_EXPLORER);
    }

    public void openCameraForActivityResult(Fragment fragment, Uri fileUri) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        fragment.startActivityForResult(intent, JandiConstants.TYPE_UPLOAD_TAKE_PHOTO);
    }

    public void openCameraForActivityResult(Activity activity, Uri fileUri) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        activity.startActivityForResult(intent, JandiConstants.TYPE_UPLOAD_TAKE_PHOTO);
    }

    public void openAlbumForActivityResult(Fragment fragment) {
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        fragment.startActivityForResult(intent, JandiConstants.TYPE_UPLOAD_GALLERY);
    }

    public void openAlbumForActivityResult(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        activity.startActivityForResult(intent, JandiConstants.TYPE_UPLOAD_GALLERY);
    }

    public boolean isOverSize(String... realFilePath) {

        File uploadFile;
        int totalSize = 0;
        for (String filePath : realFilePath) {
            uploadFile = new File(filePath);
            if (uploadFile.exists()) {
                totalSize += uploadFile.length();
                if (totalSize > MAX_FILE_SIZE) {
                    return true;
                }
            }

        }
        return false;
    }

    public boolean isOverSize(List<String> realFilePath) {

        File uploadFile;
        int totalSize = 0;
        for (String filePath : realFilePath) {
            uploadFile = new File(filePath);
            if (uploadFile.exists()) {
                totalSize += uploadFile.length();
                if (totalSize > MAX_FILE_SIZE) {
                    return true;
                }
            }

        }
        return false;
    }

    public boolean isPublicEntity(Context context, int entityId) {

        return EntityManager.getInstance(context).getEntityById(entityId).isPublicTopic();
    }

    public JsonObject uploadFile(Context context, ProgressDialog progressDialog, String realFilePath, boolean isPublicTopic, String title, int entityId, String comment) throws ExecutionException, InterruptedException {
        File uploadFile = new File(realFilePath);
        String requestURL = JandiConstantsForFlavors.SERVICE_ROOT_URL + "inner-api/v2/file";
        String permissionCode = (isPublicTopic) ? "744" : "740";
        Builders.Any.M ionBuilder
                = Ion
                .with(context)
                .load(requestURL)
                .uploadProgressDialog(progressDialog)
                .uploadProgress((downloaded, total) -> progressDialog.setProgress((int) (downloaded / total)))
                .setHeader(JandiConstants.AUTH_HEADER, TokenUtil.getRequestAuthentication().getHeaderValue())
                .setHeader("Accept", JandiV2HttpMessageConverter.APPLICATION_VERSION_FULL_NAME)
                .setMultipartParameter("title", title)
                .setMultipartParameter("share", String.valueOf(entityId))
                .setMultipartParameter("permission", permissionCode)
                .setMultipartParameter("teamId", String.valueOf(JandiAccountDatabaseManager.getInstance(context).getSelectedTeamInfo().getTeamId()));

        // Comment가 함께 등록될 경우 추가
        if (comment != null && !comment.isEmpty()) {
            ionBuilder.setMultipartParameter("comment", comment);
        }

        ResponseFuture<JsonObject> requestFuture = ionBuilder.setMultipartFile("userFile", URLConnection.guessContentTypeFromName(uploadFile.getName()), uploadFile)
                .asJsonObject();

        progressDialog.setOnCancelListener(dialog -> requestFuture.cancel());

        return requestFuture.get();

    }

    public JsonObject uploadFile(Context context, String realFilePath, boolean isPublicTopic, String title, int entityId, String comment, ProgressCallback progressCallback) throws ExecutionException, InterruptedException {
        File uploadFile = new File(realFilePath);
        String requestURL = JandiConstantsForFlavors.SERVICE_ROOT_URL + "inner-api/v2/file";
        String permissionCode = (isPublicTopic) ? "744" : "740";
        Builders.Any.M ionBuilder
                = Ion
                .with(context)
                .load(requestURL)
                .uploadProgress(progressCallback)
                .setHeader(JandiConstants.AUTH_HEADER, TokenUtil.getRequestAuthentication().getHeaderValue())
                .setHeader("Accept", JandiV2HttpMessageConverter.APPLICATION_VERSION_FULL_NAME)
                .setMultipartParameter("title", title)
                .setMultipartParameter("share", String.valueOf(entityId))
                .setMultipartParameter("permission", permissionCode)
                .setMultipartParameter("teamId", String.valueOf(JandiAccountDatabaseManager.getInstance(context).getSelectedTeamInfo().getTeamId()));

        // Comment가 함께 등록될 경우 추가
        if (comment != null && !comment.isEmpty()) {
            ionBuilder.setMultipartParameter("comment", comment);
        }

        ResponseFuture<JsonObject> requestFuture = ionBuilder.setMultipartFile("userFile", URLConnection.guessContentTypeFromName(uploadFile.getName()), uploadFile)
                .asJsonObject();

        return requestFuture.get();

    }

    public void trackUploadingFile(Context context, int entityId, JsonObject result) {

        FormattedEntity entity = EntityManager.getInstance(context).getEntityById(entityId);

        int entityType;
        if (entity.isPublicTopic()) {
            entityType = JandiConstants.TYPE_PUBLIC_TOPIC;
        } else {
            if (entity.isPrivateGroup()) {
                entityType = JandiConstants.TYPE_PRIVATE_TOPIC;
            } else {
                entityType = JandiConstants.TYPE_DIRECT_MESSAGE;
            }
        }

        try {
            MixpanelMemberAnalyticsClient
                    .getInstance(context, EntityManager.getInstance(context).getDistictId())
                    .trackUploadingFile(entityType, result);
        } catch (JSONException e) {
        }
    }

    public String uploadProfilePhoto(Context context, File file) throws ExecutionException, InterruptedException {

        EntityManager entityManager = EntityManager.getInstance(context);

        String requestURL
                = JandiConstantsForFlavors.SERVICE_ROOT_URL + "inner-api/members/" + entityManager.getMe().getId() + "/profile/photo";

        return Ion.with(context)
                .load(HttpPut.METHOD_NAME, requestURL)
                .setHeader(JandiConstants.AUTH_HEADER, TokenUtil.getRequestAuthentication().getHeaderValue())
                .setHeader("Accept", JandiV2HttpMessageConverter.APPLICATION_VERSION_FULL_NAME)
                .setMultipartFile("photo", URLConnection.guessContentTypeFromName(file.getName()), file)
                .asString()
                .get();
    }

    public ArrayList<String> getFilePathsFromInnerGallery(Intent intent) {
        return intent.getStringArrayListExtra(ImageAlbumActivity.EXTRA_DATAS);
    }
}

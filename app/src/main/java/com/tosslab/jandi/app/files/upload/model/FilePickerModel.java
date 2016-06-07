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
import com.koushikdutta.ion.builder.Builders;
import com.koushikdutta.ion.future.ResponseFuture;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.files.upload.FileUploadController;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.album.imagealbum.ImageAlbumActivity;
import com.tosslab.jandi.app.ui.fileexplorer.FileExplorerActivity;
import com.tosslab.jandi.app.ui.profile.defaultimage.ProfileImageSelectorActivity_;
import com.tosslab.jandi.app.ui.profile.modify.view.ModifyProfileActivity;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.UserAgentUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.file.ImageFilePath;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

import org.androidannotations.annotations.EBean;
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
            case FileUploadController.TYPE_UPLOAD_GALLERY:

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

            case FileUploadController.TYPE_UPLOAD_TAKE_PHOTO:
                if (filePath == null) {
                    LogUtil.e("filePath object is null...");
                    return "";
                }
                if (!filePath.exists()) {
                    LogUtil.e("filePath is not exists");
                    return "";
                }

                return filePath.getAbsolutePath();

            case FileUploadController.TYPE_UPLOAD_EXPLORER:

                realFilePath = intent.getStringExtra("GetPath") + File.separator + intent.getStringExtra("GetFileName");
                return realFilePath;
            default:
                return "";
        }
    }

    public void openExplorerForActivityResult(Fragment fragment) {
        Intent intent = new Intent(fragment.getActivity(), FileExplorerActivity.class);
        fragment.startActivityForResult(intent, FileUploadController.TYPE_UPLOAD_EXPLORER);
    }

    public void openExplorerForActivityResult(Activity activity) {
        Intent intent = new Intent(activity, FileExplorerActivity.class);
        activity.startActivityForResult(intent, FileUploadController.TYPE_UPLOAD_EXPLORER);
    }

    public void openCameraForActivityResult(Fragment fragment, Uri fileUri) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        fragment.startActivityForResult(intent, FileUploadController.TYPE_UPLOAD_TAKE_PHOTO);
    }

    public void openCameraForActivityResult(Activity activity, Uri fileUri) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        activity.startActivityForResult(intent, FileUploadController.TYPE_UPLOAD_TAKE_PHOTO);
    }

    public void openCharacterActivityForActivityResult(Activity activity, Uri fileUri) {
        Intent intent = new Intent(activity, ProfileImageSelectorActivity_.class);
        intent.putExtra("profile_image_file_uri", fileUri);
        activity.startActivityForResult(intent, ModifyProfileActivity.REQUEST_CHARACTER);
    }

    public void openCharacterActivityForActivityResult(Fragment fragment, Uri fileUri) {
        Intent intent = new Intent(fragment.getContext(), ProfileImageSelectorActivity_.class);
        intent.putExtra("profile_image_file_uri", fileUri);
        fragment.startActivityForResult(intent, ModifyProfileActivity.REQUEST_CHARACTER);
    }

    public void openAlbumForActivityResult(Fragment fragment) {
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        fragment.startActivityForResult(intent, FileUploadController.TYPE_UPLOAD_GALLERY);
    }

    public void openAlbumForActivityResult(Activity activity, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        activity.startActivityForResult(intent, requestCode);
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

    public boolean isPublicEntity(long entityId) {
        return TeamInfoLoader.getInstance().isPublicTopic(entityId);
    }

    public JsonObject uploadFile(Context context, ProgressDialog progressDialog, String realFilePath, boolean isPublicTopic, String title, long entityId, String comment) throws ExecutionException, InterruptedException {

        File uploadFile = new File(realFilePath);
        String requestURL = JandiConstantsForFlavors.SERVICE_ROOT_URL + "inner-api/file";
        String permissionCode = (isPublicTopic) ? "744" : "740";
        Builders.Any.M ionBuilder
                = Ion
                .with(context)
                .load(requestURL)
                .uploadProgressDialog(progressDialog)
                .setHeader(JandiConstants.AUTH_HEADER, TokenUtil.getRequestAuthentication())
                .setHeader("Accept", JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
                .setHeader("User-Agent", UserAgentUtil.getDefaultUserAgent())
                .setMultipartParameter("title", title)
                .setMultipartParameter("share", String.valueOf(entityId))
                .setMultipartParameter("permission", permissionCode)
                .setMultipartParameter("teamId", String.valueOf(AccountRepository.getRepository().getSelectedTeamInfo().getTeamId()));

        // Comment가 함께 등록될 경우 추가
        if (comment != null && !comment.isEmpty()) {
            ionBuilder.setMultipartParameter("comment", comment);
        }

        ResponseFuture<JsonObject> requestFuture = ionBuilder.setMultipartFile("userFile", URLConnection.guessContentTypeFromName(uploadFile.getName()), uploadFile)
                .asJsonObject();

        progressDialog.setOnCancelListener(dialog -> requestFuture.cancel());

        return requestFuture.get();

    }

    public void trackUploadingFile(Context context, long entityId, JsonObject result) {

        int entityType;
        if (TeamInfoLoader.getInstance().isPublicTopic(entityId)) {
            entityType = JandiConstants.TYPE_PUBLIC_TOPIC;
        } else {
            if (!TeamInfoLoader.getInstance().isUser(entityId)) {
                entityType = JandiConstants.TYPE_PRIVATE_TOPIC;
            } else {
                entityType = JandiConstants.TYPE_DIRECT_MESSAGE;
            }
        }

        try {
            String distictId = TeamInfoLoader.getInstance().getMyId() +
                    "-" +
                    TeamInfoLoader.getInstance().getTeamId();
            MixpanelMemberAnalyticsClient
                    .getInstance(context, distictId)
                    .trackUploadingFile(entityType, result);
        } catch (JSONException e) {
        }

        int fileId = result.get("messageId").getAsInt();

        AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                .event(Event.FileUpload)
                .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                .property(PropertyKey.ResponseSuccess, true)
                .property(PropertyKey.TopicId, entityId)
                .property(PropertyKey.FileId, fileId)
                .build());
    }

    public void trackUploadingFileFail(int errorCode) {
        AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                .event(Event.FileUpload)
                .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                .property(PropertyKey.ResponseSuccess, false)
                .property(PropertyKey.ErrorCode, errorCode)
                .build());
    }

    public JsonObject uploadProfilePhoto(Context context, File file) throws ExecutionException, InterruptedException {

        String requestURL
                = JandiConstantsForFlavors.SERVICE_ROOT_URL + "inner-api/members/" + TeamInfoLoader.getInstance().getMyId() + "/profile/photo";

        return Ion.with(context)
                .load("PUT", requestURL)
                .setHeader(JandiConstants.AUTH_HEADER, TokenUtil.getRequestAuthentication())
                .setHeader("Accept", JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
                .setHeader("User-Agent", UserAgentUtil.getDefaultUserAgent())
                .setMultipartFile("photo", URLConnection.guessContentTypeFromName(file.getName()), file)
                .asJsonObject()
                .get();
    }

    public ArrayList<String> getFilePathsFromInnerGallery(Intent intent) {
        return intent.getStringArrayListExtra(ImageAlbumActivity.EXTRA_DATAS);
    }


}

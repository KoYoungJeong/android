package com.tosslab.jandi.app.ui.share.model;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.builder.Builders;
import com.koushikdutta.ion.future.ResponseFuture;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.client.MessageManipulator;
import com.tosslab.jandi.app.network.client.MessageManipulator_;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.network.models.ResRoomInfo;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.utils.ImageFilePath;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.UserAgentUtil;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.json.JSONException;

import java.io.File;
import java.net.URLConnection;
import java.util.concurrent.ExecutionException;

import retrofit.RetrofitError;

/**
 * Created by Steve SeongUg Jung on 15. 2. 13..
 */
@EBean
public class ShareModel {

    @RootContext
    Context context;

    public ResRoomInfo getEntityById(int teamId, int roomId) {
        return RequestApiManager.getInstance().getRoomInfoByRoomsApi(teamId, roomId);
    }

    public ResTeamDetailInfo.InviteTeam getTeamInfoById(int teamId) {
        return RequestApiManager.getInstance().getTeamInfoByTeamApi(teamId);
    }

    public void sendMessage(int teamId, int entityId, int entityType, String messageText) throws RetrofitError {

        MessageManipulator messageManipulator = MessageManipulator_.getInstance_(context);

        messageManipulator.initEntity(entityType, entityId);

        messageManipulator.setTeamId(teamId);

        messageManipulator.sendMessage(messageText, null);

    }

    public String getImagePath(String uriString) {
        Uri uri = Uri.parse(uriString);
        return ImageFilePath.getPath(context, uri);
    }

    public JsonObject uploadFile(File imageFile, String titleText, String commentText,
                                 int teamId, int entityId, ProgressDialog progressDialog,
                                 boolean isPublicTopic) throws ExecutionException, InterruptedException {
        File uploadFile = new File(imageFile.getAbsolutePath());
        String requestURL = JandiConstantsForFlavors.SERVICE_INNER_API_URL + "/v2/file";
        String permissionCode = (isPublicTopic) ? "744" : "740";
        Builders.Any.M ionBuilder
                = Ion
                .with(context)
                .load(requestURL)
                .uploadProgressDialog(progressDialog)
                .setHeader(JandiConstants.AUTH_HEADER, TokenUtil.getRequestAuthentication())
                .setHeader("Accept", JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
                .setHeader("User-Agent", UserAgentUtil.getDefaultUserAgent(context))
                .setMultipartParameter("title", titleText)
                .setMultipartParameter("share", "" + entityId)
                .setMultipartParameter("permission", permissionCode)
                .setMultipartParameter("teamId", String.valueOf(teamId));

        // Comment가 함께 등록될 경우 추가
        if (!TextUtils.isEmpty(commentText)) {
            ionBuilder.setMultipartParameter("comment", commentText);
        }

        ResponseFuture<JsonObject> responseFuture = ionBuilder
                .setMultipartFile("userFile",
                        URLConnection.guessContentTypeFromName(uploadFile.getName()), uploadFile)
                .asJsonObject();

        progressDialog.setOnCancelListener(dialog -> responseFuture.cancel());
        JsonObject userFile = responseFuture.get();

        return userFile;
    }

    public void trackUploadingFile(int entityType, JsonObject result) {

        try {
            MixpanelMemberAnalyticsClient.getInstance(context, EntityManager.getInstance().getDistictId()).trackUploadingFile(entityType, result);
        } catch (JSONException e) {
        }
    }

    public String getFilePath(String uriString) {
        return Uri.parse(uriString).getPath();
    }

}

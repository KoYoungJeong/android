package com.tosslab.jandi.app.ui.share.type.model;

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
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.client.MessageManipulator;
import com.tosslab.jandi.app.network.client.MessageManipulator_;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.network.spring.JandiV2HttpMessageConverter;
import com.tosslab.jandi.app.ui.share.type.to.EntityInfo;
import com.tosslab.jandi.app.utils.ImageFilePath;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.TokenUtil;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.json.JSONException;

import java.io.File;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import rx.Observable;

/**
 * Created by Steve SeongUg Jung on 15. 2. 13..
 */
@EBean
public class ShareModel {

    @RootContext
    Context context;

    public List<EntityInfo> getEntityInfos() {

        List<FormattedEntity> entities = new ArrayList<FormattedEntity>();

        EntityManager entityManager = EntityManager.getInstance(context);
        entities.addAll(entityManager.getJoinedChannels());
        entities.addAll(entityManager.getGroups());
        entities.addAll(entityManager.getFormattedUsersWithoutMe());

        Iterator<EntityInfo> iterator = Observable.from(entities)
                .map(entity -> {

                    boolean publicTopic = entity.isPublicTopic();
                    boolean privateGroup = entity.isPrivateGroup();
                    boolean user = entity.isUser();

                    String userLargeProfileUrl;
                    if (user) {
                        userLargeProfileUrl = entity.getUserLargeProfileUrl();
                    } else {
                        userLargeProfileUrl = "";
                    }
                    return new EntityInfo(entity.getId(), entity.getName(), publicTopic, privateGroup, userLargeProfileUrl);

                })
                .toBlocking()
                .getIterator();

        List<EntityInfo> entityInfos = new ArrayList<EntityInfo>();

        while (iterator.hasNext()) {
            entityInfos.add(iterator.next());
        }

        return entityInfos;

    }

    public void sendMessage(EntityInfo entity, String messageText) throws JandiNetworkException {

        MessageManipulator messageManipulator = MessageManipulator_.getInstance_(context);

        int entityType;

        if (entity.isPublicTopic()) {
            entityType = JandiConstants.TYPE_PUBLIC_TOPIC;
        } else if (entity.isPrivateTopic()) {
            entityType = JandiConstants.TYPE_PRIVATE_TOPIC;
        } else {
            entityType = JandiConstants.TYPE_DIRECT_MESSAGE;
        }
        messageManipulator.initEntity(entityType, entity.getEntityId());

        messageManipulator.sendMessage(messageText);

    }

    public int getTeamId() {
        return JandiAccountDatabaseManager.getInstance(context).getSelectedTeamInfo().getTeamId();
    }

    public boolean isStarredEntity(int entityId) {
        return EntityManager.getInstance(context).getEntityById(entityId).isStarred;
    }

    public String getImagePath(String uriString) {

        Uri uri = Uri.parse(uriString);

        return ImageFilePath.getPath(context, uri);

    }

    public JsonObject uploadFile(File imageFile, String titleText, String commentText, EntityInfo entityInfo, ProgressDialog progressDialog, boolean isPublicTopic) throws ExecutionException, InterruptedException {
        File uploadFile = new File(imageFile.getAbsolutePath());
        String requestURL = JandiConstantsForFlavors.SERVICE_ROOT_URL + "inner-api/v2/file";
        String permissionCode = (isPublicTopic) ? "744" : "740";
        Builders.Any.M ionBuilder
                = Ion
                .with(context)
                .load(requestURL)
                .uploadProgressDialog(progressDialog)
                .progress((downloaded, total) -> progressDialog.setProgress((int) (downloaded / total)))
                .setHeader(JandiConstants.AUTH_HEADER, TokenUtil.getRequestAuthentication(context).getHeaderValue())
                .setHeader("Accept", JandiV2HttpMessageConverter.APPLICATION_VERSION_FULL_NAME)
                .setMultipartParameter("title", titleText)
                .setMultipartParameter("share", "" + entityInfo.getEntityId())
                .setMultipartParameter("permission", permissionCode)
                .setMultipartParameter("teamId", String.valueOf(JandiAccountDatabaseManager.getInstance(context).getSelectedTeamInfo().getTeamId()));

        // Comment가 함께 등록될 경우 추가
        if (!TextUtils.isEmpty(commentText)) {
            ionBuilder.setMultipartParameter("comment", commentText);
        }

        ResponseFuture<JsonObject> responseFuture = ionBuilder.setMultipartFile("userFile", URLConnection.guessContentTypeFromName(uploadFile.getName()), uploadFile)
                .asJsonObject();

        progressDialog.setOnCancelListener(dialog -> responseFuture.cancel());
        JsonObject userFile = responseFuture.get();

        return userFile;
    }

    public void trackUploadingFile(int entityType, JsonObject result) {

        try {
            MixpanelMemberAnalyticsClient.getInstance(context, EntityManager.getInstance(context).getDistictId()).trackUploadingFile(entityType, result);
        } catch (JSONException e) {
        }
    }
}

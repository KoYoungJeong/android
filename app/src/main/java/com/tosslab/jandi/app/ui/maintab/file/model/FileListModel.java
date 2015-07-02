package com.tosslab.jandi.app.ui.maintab.file.model;

/**
 * Created by Steve SeongUg Jung on 15. 1. 8..
 */

import android.app.ProgressDialog;
import android.content.Context;
import android.text.TextUtils;

import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.builder.Builders;
import com.koushikdutta.ion.future.ResponseFuture;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.events.files.ConfirmFileUploadEvent;
<<<<<<< HEAD
import com.tosslab.jandi.app.files.upload.model.FilePickerModel;
import com.tosslab.jandi.app.lists.entities.EntityManager;
=======
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
>>>>>>> origin/entitymanager_thread_safe
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.local.database.file.JandiFileDatabaseManager;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.network.models.ReqSearchFile;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResSearchFile;
import com.tosslab.jandi.app.network.spring.JandiV2HttpMessageConverter;
import com.tosslab.jandi.app.ui.message.v2.model.MessageListModel;
import com.tosslab.jandi.app.utils.TokenUtil;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.json.JSONException;

import java.io.File;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import retrofit.RetrofitError;

@EBean
public class FileListModel {

    @RootContext
    Context context;

    public ResSearchFile searchFileList(ReqSearchFile reqSearchFile) throws RetrofitError {
        ResSearchFile resSearchFile = RequestApiManager.getInstance().searchFileByMainRest(reqSearchFile);
        return resSearchFile;
    }

    public boolean isAllTypeFirstSearch(ReqSearchFile reqSearchFile) {

        return reqSearchFile.startMessageId == -1 &&
                reqSearchFile.sharedEntityId == -1 &&
                TextUtils.equals(reqSearchFile.fileType, "all") &&
                TextUtils.equals(reqSearchFile.writerId, "all") &&
                TextUtils.isEmpty(reqSearchFile.keyword);

    }

    public void saveOriginFirstItems(int teamId, ResSearchFile fileMessages) {
        JandiFileDatabaseManager.getInstance(context).upsertFiles(teamId, fileMessages);
    }

    public ResSearchFile getFiles(int teamId) {
        return JandiFileDatabaseManager.getInstance(context).getFiles(teamId);
    }

    public EntityManager retrieveEntityManager() {
        EntityManager entityManager = EntityManager.getInstance(context);

        if (entityManager != null) {
            return entityManager;
        }

        return null;
    }

    public List<ResMessages.OriginalMessage> descSortByCreateTime(List<ResMessages.OriginalMessage> links) {
        List<ResMessages.OriginalMessage> ret = new ArrayList<ResMessages.OriginalMessage>(links);

        Comparator<ResMessages.OriginalMessage> sort = new Comparator<ResMessages.OriginalMessage>() {
            @Override
            public int compare(ResMessages.OriginalMessage link, ResMessages.OriginalMessage link2) {
                if (link.createTime.getTime() > link2.createTime.getTime())
                    return -1;
                else if (link.createTime.getTime() == link2.createTime.getTime())
                    return 0;
                else
                    return 1;
            }
        };
        Collections.sort(ret, sort);
        return ret;
    }

    public boolean isOverSize(String realFilePath) {
        File uploadFile = new File(realFilePath);
        return uploadFile.exists() && uploadFile.length() > FilePickerModel.MAX_FILE_SIZE;
    }

    public boolean isDefaultSearchQuery(ReqSearchFile searchFile) {
        return searchFile.sharedEntityId == -1 &&
                searchFile.startMessageId == -1 &&
                TextUtils.isEmpty(searchFile.keyword) &&
                TextUtils.equals(searchFile.fileType, "all") &&
                TextUtils.equals(searchFile.writerId, "all");
    }

    public boolean isDefaultSearchQueryIgnoreMessageId(ReqSearchFile searchFile) {
        return searchFile.sharedEntityId == -1 &&
                TextUtils.isEmpty(searchFile.keyword) &&
                TextUtils.equals(searchFile.fileType, "all") &&
                TextUtils.equals(searchFile.writerId, "all");
    }

    public JsonObject uploadFile(ConfirmFileUploadEvent event, ProgressDialog progressDialog, boolean isPublicTopic) throws ExecutionException, InterruptedException {
        File uploadFile = new File(event.realFilePath);
        String requestURL = JandiConstantsForFlavors.SERVICE_INNER_API_URL + "/v2/file";
        String permissionCode = (isPublicTopic) ? "744" : "740";
        Builders.Any.M ionBuilder
                = Ion
                .with(context)
                .load(requestURL)
                .uploadProgressDialog(progressDialog)
                .progress((downloaded, total) -> progressDialog.setProgress((int) (downloaded / total)))
                .setHeader(JandiConstants.AUTH_HEADER, TokenUtil.getRequestAuthentication().getHeaderValue())
                .setHeader("Accept", JandiV2HttpMessageConverter.APPLICATION_VERSION_FULL_NAME)
                .setMultipartParameter("title", event.title)
                .setMultipartParameter("share", "" + event.entityId)
                .setMultipartParameter("permission", permissionCode)
                .setMultipartParameter("teamId", String.valueOf(JandiAccountDatabaseManager.getInstance(context).getSelectedTeamInfo().getTeamId()));

        // Comment가 함께 등록될 경우 추가
        if (event.comment != null && !event.comment.isEmpty()) {
            ionBuilder.setMultipartParameter("comment", event.comment);
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

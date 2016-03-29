package com.tosslab.jandi.app.ui.share.model;

import android.app.ProgressDialog;
import android.net.Uri;
import android.text.TextUtils;

import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.builder.Builders;
import com.koushikdutta.ion.future.ResponseFuture;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.LeftSideMenuRepository;
import com.tosslab.jandi.app.network.client.MessageManipulator;
import com.tosslab.jandi.app.network.client.MessageManipulator_;
import com.tosslab.jandi.app.network.client.main.LeftSideApi;
import com.tosslab.jandi.app.network.client.rooms.RoomsApi;
import com.tosslab.jandi.app.network.client.teams.TeamApi;
import com.tosslab.jandi.app.network.dagger.DaggerApiClientComponent;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.json.JacksonMapper;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResRoomInfo;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.ui.share.views.model.ShareSelectModel;
import com.tosslab.jandi.app.ui.share.views.model.ShareSelectModel_;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.UserAgentUtil;
import com.tosslab.jandi.app.utils.file.ImageFilePath;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import dagger.Lazy;

@EBean
public class ShareModel {

    @Inject
    Lazy<RoomsApi> roomsApi;
    @Inject
    Lazy<TeamApi> teamApi;
    @Inject
    Lazy<LeftSideApi> leftSideApi;

    @AfterInject
    void initObject() {
        DaggerApiClientComponent.create().inject(this);
    }

    public ResRoomInfo getEntityById(long teamId, long roomId) throws RetrofitException {
        return roomsApi.get().getRoomInfo(teamId, roomId);
    }

    public ResTeamDetailInfo.InviteTeam getTeamInfoById(long teamId) throws RetrofitException {
        return teamApi.get().getTeamInfo(teamId);
    }

    public ResCommon sendMessage(long teamId, long entityId, int entityType, String messageText, List<MentionObject> mention) throws RetrofitException {

        MessageManipulator messageManipulator = MessageManipulator_.getInstance_(JandiApplication.getContext());

        messageManipulator.initEntity(entityType, entityId);

        messageManipulator.setTeamId(teamId);

        return messageManipulator.sendMessage(messageText, mention);

    }

    public String getImagePath(String uriString) {
        Uri uri = Uri.parse(uriString);
        return ImageFilePath.getPath(JandiApplication.getContext(), uri);
    }

    public JsonObject uploadFile(File imageFile, String titleText, String commentText,
                                 long teamId, long entityId, ProgressDialog progressDialog,
                                 boolean isPublicTopic, List<MentionObject> mentions) throws ExecutionException, InterruptedException {
        File uploadFile = new File(imageFile.getAbsolutePath());
        String requestURL = JandiConstantsForFlavors.SERVICE_FILE_UPLOAD_URL + "inner-api/file";
        String permissionCode = (isPublicTopic) ? "744" : "740";
        Builders.Any.M ionBuilder
                = Ion
                .with(JandiApplication.getContext())
                .load(requestURL)
                .uploadProgressHandler((downloaded, total) -> {
                    progressDialog.setProgress((int) (downloaded * 100 / total));
                })
                .setHeader(JandiConstants.AUTH_HEADER, TokenUtil.getRequestAuthentication())
                .setHeader("Accept", JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
                .setHeader("User-Agent", UserAgentUtil.getDefaultUserAgent(JandiApplication.getContext()))
                .setMultipartParameter("title", titleText)
                .setMultipartParameter("share", "" + entityId)
                .setMultipartParameter("permission", permissionCode)
                .setMultipartParameter("teamId", String.valueOf(teamId));

        // Comment가 함께 등록될 경우 추가
        if (!TextUtils.isEmpty(commentText)) {
            ionBuilder.setMultipartParameter("comment", commentText);
            try {
                ionBuilder.setMultipartParameter("mentions", JacksonMapper.getInstance().getObjectMapper().writeValueAsString(mentions));
            } catch (IOException e) {
                e.printStackTrace();
            }
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
            MixpanelMemberAnalyticsClient.getInstance(JandiApplication.getContext(), EntityManager.getInstance().getDistictId()).trackUploadingFile(entityType, result);
        } catch (JSONException e) {
        }
    }

    public String getFilePath(String uriString) {
        return Uri.parse(uriString).getPath();
    }

    public boolean hasLeftSideMenu(long teamId) {
        return LeftSideMenuRepository.getRepository().findLeftSideMenuByTeamId(teamId) != null;
    }

    public ResLeftSideMenu getLeftSideMenu(long teamId) throws RetrofitException {
        return leftSideApi.get().getInfosForSideMenu(teamId);
    }

    public boolean updateLeftSideMenu(ResLeftSideMenu leftSideMenu) {
        return LeftSideMenuRepository.getRepository().upsertLeftSideMenu(leftSideMenu);
    }

    public ShareSelectModel getShareSelectModel(long teamId) {
        ResLeftSideMenu leftSideMenu = LeftSideMenuRepository.getRepository().findLeftSideMenuByTeamId(teamId);
        ShareSelectModel shareSelectModel = ShareSelectModel_.getInstance_(JandiApplication.getContext());
        shareSelectModel.initFormattedEntities(leftSideMenu);
        return shareSelectModel;
    }
}

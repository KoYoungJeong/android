package com.tosslab.jandi.app.ui.filedetail.model;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.local.database.entity.JandiEntityDatabaseManager;
import com.tosslab.jandi.app.network.client.JandiEntityClient;
import com.tosslab.jandi.app.network.client.MessageManipulator;
import com.tosslab.jandi.app.network.manager.RequestManager;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResFileDetail;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import rx.Observable;

/**
 * Created by Steve SeongUg Jung on 15. 1. 8..
 */
@EBean
public class FileDetailModel {

    @RootContext
    Context context;

    @Bean
    MessageManipulator messageManipulator;

    @Bean
    JandiEntityClient jandiEntityClient;

    public ResFileDetail getFileDetailInfo(int fileId) throws JandiNetworkException {

        return jandiEntityClient.getFileDetail(fileId);
    }

    public void shareMessage(int fileId, int entityIdToBeShared) throws JandiNetworkException {
        jandiEntityClient.shareMessage(fileId, entityIdToBeShared);
    }

    public void unshareMessage(int fileId, int entityIdToBeUnshared) throws JandiNetworkException {
        jandiEntityClient.unshareMessage(fileId, entityIdToBeUnshared);
    }

    public void deleteFile(int fileId) throws JandiNetworkException {
        jandiEntityClient.deleteFile(fileId);
    }

    public void sendMessageComment(int fileId, String message) throws JandiNetworkException {
        jandiEntityClient.sendMessageComment(fileId, message);
    }

    public ResLeftSideMenu.User getUserProfile(int userEntityId) throws JandiNetworkException {
        return jandiEntityClient.getUserProfile(userEntityId);
    }

    public File download(String url, String fileName, String fileType, ProgressDialog progressDialog) throws Exception {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/Jandi");
        dir.mkdirs();

        return Ion.with(context)
                .load(url)
                .progressDialog(progressDialog)
                .write(new File(dir, fileName))
                .get();
    }

    public boolean isMyComment(int writerId) {
        EntityManager entityManager = EntityManager.getInstance(context);

        if (entityManager == null) {
            return false;
        }
        FormattedEntity me = entityManager.getMe();
        return me != null && me.getId() == writerId;
    }

    public void deleteComment(int messageId, int feedbackId) throws JandiNetworkException {
        jandiEntityClient.deleteMessageComment(messageId, feedbackId);

    }

    public void deleteStickerComment(int messageId, int messageType) throws JandiNetworkException {
        messageManipulator.deleteSticker(messageId, messageType);

    }

    public boolean isMediaFile(String fileType) {

        if (TextUtils.isEmpty(fileType)) {
            return false;
        }

        return fileType.startsWith("audio") || fileType.startsWith("video") || fileType.startsWith("image");
    }

    public android.net.Uri addGallery(File result, String fileType) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, result.getName());
        values.put(MediaStore.Images.Media.DISPLAY_NAME, result.getName());
        values.put(MediaStore.Images.Media.DESCRIPTION, "");
        values.put(MediaStore.Images.Media.MIME_TYPE, fileType);
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.DATA, result.getAbsolutePath());

        return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    public List<FormattedEntity> getUnsharedEntities(List<Integer> shareEntities) {

        EntityManager entityManager = EntityManager.getInstance(context);

        boolean include = false;
        int myEntityId = entityManager.getMe().getId();
        for (int idx = shareEntities.size() - 1; idx >= 0; idx--) {
            if (shareEntities.get(idx) == myEntityId) {
                include = true;
                break;
            }
        }

        if (!include) {
            shareEntities.add(myEntityId);
        }

        List<FormattedEntity> entities = entityManager.retrieveExclusivedEntities(shareEntities);

        Iterator<FormattedEntity> enabledEntities = Observable.from(entities)
                .filter(entity -> !entity.isUser() || TextUtils.equals(entity.getUser().status, "enabled"))
                .toBlocking()
                .getIterator();

        List<FormattedEntity> formattedEntities = new ArrayList<>();

        while (enabledEntities.hasNext()) {
            formattedEntities.add(enabledEntities.next());
        }

        return formattedEntities;
    }

    public boolean isEnableUserFromUploder(ResFileDetail resFileDetail) {

        for (ResMessages.OriginalMessage fileDetail : resFileDetail.messageDetails) {
            if (fileDetail instanceof ResMessages.FileMessage) {
                final ResMessages.FileMessage fileMessage = (ResMessages.FileMessage) fileDetail;

                return TextUtils.equals(EntityManager.getInstance(context).getEntityById(fileMessage.writerId).getUser().status, "enabled");
            }
        }
        return false;
    }

    public ResCommon joinEntity(FormattedEntity entityId) throws JandiNetworkException {

        return jandiEntityClient.joinChannel(entityId.getChannel());

    }

    public boolean refreshEntity() {
        try {
            ResLeftSideMenu totalEntitiesInfo = jandiEntityClient.getTotalEntitiesInfo();
            JandiEntityDatabaseManager.getInstance(context).upsertLeftSideMenu(totalEntitiesInfo);
            int totalUnreadCount = BadgeUtils.getTotalUnreadCount(totalEntitiesInfo);
            JandiPreference.setBadgeCount(context, totalUnreadCount);
            BadgeUtils.setBadge(context, totalUnreadCount);
            EntityManager.getInstance(context).refreshEntity(totalEntitiesInfo);

            return true;
        } catch (JandiNetworkException e) {
            LogUtil.e("Get Entity Info Fail : " + e.getErrorInfo() + " : " + e.httpBody, e);
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public void sendMessageCommentWithSticker(int fileId, int stickerGroupId, String stickerId, String comment) throws JandiNetworkException {
        int teamId = JandiAccountDatabaseManager.getInstance(context).getSelectedTeamInfo().getTeamId();
        RequestManager.newInstance(context, StickerCommentRequest.create(context, stickerGroupId, stickerId, teamId, fileId, comment)).request();
    }
}

package com.tosslab.jandi.app.ui.message.v2.model;

import android.app.Activity;
import android.app.ProgressDialog;
import android.text.TextUtils;
import android.view.MenuItem;

import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import com.koushikdutta.ion.builder.Builders;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.events.files.ConfirmFileUploadEvent;
import com.tosslab.jandi.app.events.messages.RefreshNewMessageEvent;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.local.database.message.JandiMessageDatabaseManager;
import com.tosslab.jandi.app.network.client.JandiEntityClient;
import com.tosslab.jandi.app.network.client.MessageManipulator;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResUpdateMessages;
import com.tosslab.jandi.app.network.spring.JandiV2HttpMessageConverter;
import com.tosslab.jandi.app.ui.message.model.menus.MenuCommand;
import com.tosslab.jandi.app.ui.message.model.menus.MenuCommandBuilder;
import com.tosslab.jandi.app.ui.message.to.ChattingInfomations;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.TokenUtil;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.apache.log4j.Logger;

import java.io.File;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by Steve SeongUg Jung on 15. 1. 20..
 */
@EBean
public class MessageListModel {

    private static final Logger logger = Logger.getLogger(MessageListModel.class);

    private static final int MAX_FILE_SIZE = 100 * 1024 * 1024;
    @Bean
    MessageManipulator messageManipulator;
    @Bean
    JandiEntityClient jandiEntityClient;
    @Bean
    MessageListTimer messageListTimer;
    @RootContext
    Activity activity;
    private PublishSubject<String> publishSubject;


    @AfterInject
    void initObject() {


        publishSubject = PublishSubject.create();


        publishSubject.observeOn(Schedulers.io())
                .map(message -> {
                    boolean isSuccess;
                    try {
                        messageManipulator.sendMessage(message);
                        isSuccess = true;
                    } catch (JandiNetworkException e) {
                        logger.error("send Message Fail : " + e.getErrorInfo() + " : " + e.httpBody, e);
                        isSuccess = false;
                    }

                    return isSuccess;
                })
                .skip(500, TimeUnit.MILLISECONDS)
                .subscribe(isSuccess -> {
                    if (isSuccess) {
                        EventBus.getDefault().post(new RefreshNewMessageEvent());
                    }
                })
        ;
    }

    public void setEntityInfo(int entityType, int entityId) {
        messageManipulator.initEntity(entityType, entityId);
    }

    public ResMessages getOldMessage(int position) throws JandiNetworkException {
        return messageManipulator.getMessages(position);
    }

    public List<ResMessages.Link> sortById(List<ResMessages.Link> messages) {

        Collections.sort(messages, new Comparator<ResMessages.Link>() {
            @Override
            public int compare(ResMessages.Link lhs, ResMessages.Link rhs) {
                return lhs.id - rhs.id;
            }
        });
        return messages;
    }

    public boolean isEmpty(CharSequence text) {
        return TextUtils.isEmpty(text);
    }

    public ResUpdateMessages getNewMessage(int linkId) throws JandiNetworkException {
        return messageManipulator.updateMessages(linkId);
    }

    public void stopRefreshTimer() {
        messageListTimer.stop();
    }

    public void startRefreshTimer() {
        messageListTimer.start();
    }

    public void deleteMessage(int messageId) throws JandiNetworkException {
        messageManipulator.deleteMessage(messageId);
    }

    public boolean isFileType(ResMessages.OriginalMessage message) {
        return message instanceof ResMessages.FileMessage;
    }

    public boolean isCommentType(ResMessages.OriginalMessage message) {
        return message instanceof ResMessages.CommentMessage;
    }

    public boolean isPublicTopic(int entityType) {
        return (entityType == JandiConstants.TYPE_PUBLIC_TOPIC) ? true : false;
    }

    public boolean isPrivateTopic(int entityType) {
        return (entityType == JandiConstants.TYPE_PRIVATE_TOPIC) ? true : false;
    }

    public boolean isDirectMessage(int entityType) {
        return (entityType == JandiConstants.TYPE_DIRECT_MESSAGE) ? true : false;
    }

    public boolean isMyTopic(int entityId) {
        return EntityManager.getInstance(activity).isMyTopic(entityId);
    }

    public MenuCommand getMenuCommand(ChattingInfomations chattingInfomations, MenuItem item) {
        return MenuCommandBuilder.init(activity)
                .with(jandiEntityClient)
                .with(chattingInfomations)
                .build(item);
    }

    public boolean isOverSize(String realFilePath) {
        File uploadFile = new File(realFilePath);
        return uploadFile.exists() && uploadFile.length() > MAX_FILE_SIZE;
    }

    public void sendMessage(String message) {
        publishSubject.onNext(message);
    }

    public boolean isMyMessage(int writerId) {
        return EntityManager.getInstance(activity).getMe().getId() == writerId;
    }

    public JsonObject uploadFile(ConfirmFileUploadEvent event, ProgressDialog progressDialog, boolean isPublicTopic) throws ExecutionException, InterruptedException {
        File uploadFile = new File(event.realFilePath);
        String requestURL = JandiConstantsForFlavors.SERVICE_ROOT_URL + "inner-api/v2/file";
        String permissionCode = (isPublicTopic) ? "744" : "740";
        Builders.Any.M ionBuilder
                = Ion
                .with(activity)
                .load(requestURL)
                .uploadProgressDialog(progressDialog)
                .progress(new ProgressCallback() {
                    @Override
                    public void onProgress(long downloaded, long total) {

                        progressDialog.setProgress((int) (downloaded / total));
                    }
                })
                .setHeader(JandiConstants.AUTH_HEADER, TokenUtil.getRequestAuthentication(activity).getHeaderValue())
                .setHeader("Accept", JandiV2HttpMessageConverter.APPLICATION_VERSION_FULL_NAME)
                .setMultipartParameter("title", uploadFile.getName())
                .setMultipartParameter("share", "" + event.entityId)
                .setMultipartParameter("permission", permissionCode)
                .setMultipartParameter("teamId", String.valueOf(JandiAccountDatabaseManager.getInstance(activity).getSelectedTeamInfo().getTeamId()));

        // Comment가 함께 등록될 경우 추가
        if (event.comment != null && !event.comment.isEmpty()) {
            ionBuilder.setMultipartParameter("comment", event.comment);
        }

        JsonObject userFile = ionBuilder.setMultipartFile("userFile", URLConnection.guessContentTypeFromName(uploadFile.getName()), uploadFile)
                .asJsonObject()
                .get();

        return userFile;
    }

    public void updateMarker(int lastUpdateLinkId) throws JandiNetworkException {
        messageManipulator.setMarker(lastUpdateLinkId);
    }

    public void saveMessages(int teamId, int entityId, List<ResMessages.Link> lastItems) {
        JandiMessageDatabaseManager.getInstance(activity).upsertMessage(teamId, entityId, lastItems);
    }

    public void saveTempMessage(int teamId, int entityId, String sendEditText) {
        JandiMessageDatabaseManager.getInstance(activity).upsertTempMessage(teamId, entityId, sendEditText);
    }

    public void deleteTopic(int entityId, int entityType) throws JandiNetworkException {
        if (entityType == JandiConstants.TYPE_PUBLIC_TOPIC) {
            jandiEntityClient.deleteChannel(entityId);
        } else {
            jandiEntityClient.deletePrivateGroup(entityId);
        }
    }

    public void modifyTopicName(int entityType, int entityId, String inputName) throws JandiNetworkException {
        if (entityType == JandiConstants.TYPE_PUBLIC_TOPIC) {
            jandiEntityClient.modifyChannelName(entityId, inputName);
        } else if (entityType == JandiConstants.TYPE_PRIVATE_TOPIC) {
            jandiEntityClient.modifyPrivateGroupName(entityId, inputName);
        }
    }

    public List<ResMessages.Link> sortDescById(List<ResMessages.Link> messages) {
        Collections.sort(messages, new Comparator<ResMessages.Link>() {
            @Override
            public int compare(ResMessages.Link lhs, ResMessages.Link rhs) {
                return lhs.id - rhs.id;
            }
        });
        return messages;
    }
}

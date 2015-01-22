package com.tosslab.jandi.app.ui.message.v2.model;

import android.app.Activity;
import android.text.TextUtils;
import android.view.MenuItem;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.network.client.JandiEntityClient;
import com.tosslab.jandi.app.network.client.MessageManipulator;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResUpdateMessages;
import com.tosslab.jandi.app.ui.message.model.menus.MenuCommand;
import com.tosslab.jandi.app.ui.message.model.menus.MenuCommandBuilder;
import com.tosslab.jandi.app.ui.message.to.ChattingInfomations;
import com.tosslab.jandi.app.utils.JandiNetworkException;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 1. 20..
 */
@EBean
public class MessageListModel {

    private static final int MAX_FILE_SIZE = 100 * 1024 * 1024;
    @Bean
    MessageManipulator messageManipulator;
    @Bean
    JandiEntityClient jandiEntityClient;
    @Bean
    MessageListTimer messageListTimer;
    @RootContext
    Activity activity;

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

    public void sendMessage(String message) throws JandiNetworkException {
        messageManipulator.sendMessage(message);
    }

    public boolean isMyMessage(int writerId) {
        return EntityManager.getInstance(activity).getMe().getId() == writerId;
    }
}

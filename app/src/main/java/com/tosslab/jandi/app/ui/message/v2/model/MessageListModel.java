package com.tosslab.jandi.app.ui.message.v2.model;

import android.text.TextUtils;

import com.tosslab.jandi.app.network.client.MessageManipulator;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResUpdateMessages;
import com.tosslab.jandi.app.utils.JandiNetworkException;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 1. 20..
 */
@EBean
public class MessageListModel {

    @Bean
    MessageManipulator messageManipulator;


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
}

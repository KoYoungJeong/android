package com.tosslab.jandi.app.network.jackson.deserialize.start;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import com.tosslab.jandi.app.network.models.start.Bot;
import com.tosslab.jandi.app.network.models.start.Chat;
import com.tosslab.jandi.app.network.models.start.Folder;
import com.tosslab.jandi.app.network.models.start.Human;
import com.tosslab.jandi.app.network.models.start.InitializeInfo;
import com.tosslab.jandi.app.network.models.start.Marker;
import com.tosslab.jandi.app.network.models.start.Topic;

import java.util.Collection;

public class InitializeInfoConverter implements Converter<InitializeInfo, InitializeInfo> {
    @Override
    public InitializeInfo convert(InitializeInfo value) {
        value.setTeamId(value.getTeam().getId());
        Collection<Folder> folders = value.getFolders();
        if (folders != null && !folders.isEmpty()) {
            for (Folder folder : folders) {
                folder.setInitialInfo(value);
            }
        }

        Collection<Topic> topics = value.getTopics();
        if (topics != null && !topics.isEmpty()) {
            for (Topic topic : topics) {
                topic.setInitialInfo(value);
                Collection<Marker> markers = topic.getMarkers();
                if (markers != null && !markers.isEmpty()) {
                    for (Marker marker : markers) {
                        marker.setTopic(topic);
                    }
                }
            }
        }

        Collection<Chat> chats = value.getChats();
        if (chats != null && !chats.isEmpty()) {
            for (Chat chat : chats) {
                chat.setInitialInfo(value);
                Collection<Marker> markers = chat.getMarkers();
                if (markers != null && !markers.isEmpty()) {
                    for (Marker marker : markers) {
                        marker.setChat(chat);
                    }
                }
            }
        }

        Collection<Human> humans = value.getHumans();
        if (humans != null && !humans.isEmpty()) {
            for (Human human : humans) {
                human.setInitialInfo(value);
            }
        }

        Collection<Bot> bots = value.getBots();
        if (bots != null && !bots.isEmpty()) {
            for (Bot bot : bots) {
                bot.setInitialInfo(value);
            }
        }

        return value;
    }

    @Override
    public JavaType getInputType(TypeFactory typeFactory) {
        return typeFactory.constructType(InitializeInfo.class);
    }

    @Override
    public JavaType getOutputType(TypeFactory typeFactory) {
        return typeFactory.constructType(InitializeInfo.class);
    }
}

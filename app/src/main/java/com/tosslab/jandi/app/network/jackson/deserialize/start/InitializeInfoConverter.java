package com.tosslab.jandi.app.network.jackson.deserialize.start;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import com.tosslab.jandi.app.network.models.start.Bot;
import com.tosslab.jandi.app.network.models.start.Chat;
import com.tosslab.jandi.app.network.models.start.Folder;
import com.tosslab.jandi.app.network.models.start.Human;
import com.tosslab.jandi.app.network.models.start.InitialInfo;
import com.tosslab.jandi.app.network.models.start.Marker;
import com.tosslab.jandi.app.network.models.start.Topic;

import java.util.ArrayList;
import java.util.Collection;

public class InitializeInfoConverter implements Converter<InitialInfo, InitialInfo> {
    @Override
    public InitialInfo convert(InitialInfo value) {
        value.setTeamId(value.getTeam().getId());
        long myId = value.getSelf().getId();

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
                } else {
                    // marker 가 없으면 임의로 지정함
                    ArrayList<Marker> markers1 = new ArrayList<>();
                    for (Long id : chat.getMembers()) {
                        Marker marker = new Marker();
                        marker.setMemberId(id);
                        marker.setReadLinkId(chat.getLastLinkId());
                        marker.setChat(chat);
                        markers1.add(marker);
                    }
                    chat.setMarkers(markers1);
                }

                if (chat.getCompanionId() == 0) {
                    for (Long id : chat.getMembers()) {
                        if (id != value.getSelf().getId()) {
                            chat.setCompanionId(id);
                            break;
                        }
                    }
                }
            }
        }

        Collection<Human> humans = value.getMembers();
        if (humans != null && !humans.isEmpty()) {
            for (Human human : humans) {
                human.setInitialInfo(value);
                if (human.getProfile() != null) {
                    human.getProfile().set_id(human.getId());
                }
                if (human.getId() == myId) {
                    human.setIsStarred(false);
                }
            }

        }

        Collection<Bot> bots = value.getBots();
        if (bots != null && !bots.isEmpty()) {
            for (Bot bot : bots) {
                bot.setInitialInfo(value);
            }
        }

        InitialInfo.Poll poll = value.getPoll();
        if (poll != null) {
            poll.setId(value.getTeamId());
        }

        InitialInfo.Mention mention = value.getMention();
        if (mention != null) {
            mention.setId(value.getTeamId());
        }

        InitialInfo.TeamPlan teamPlan = value.getTeamPlan();
        if (teamPlan != null) {
            teamPlan.setId(value.getTeamId());
        }

        return value;
    }

    @Override
    public JavaType getInputType(TypeFactory typeFactory) {
        return typeFactory.constructType(InitialInfo.class);
    }

    @Override
    public JavaType getOutputType(TypeFactory typeFactory) {
        return typeFactory.constructType(InitialInfo.class);
    }
}

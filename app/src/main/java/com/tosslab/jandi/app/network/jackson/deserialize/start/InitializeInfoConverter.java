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
import com.tosslab.jandi.app.network.models.start.Mention;
import com.tosslab.jandi.app.network.models.start.Poll;
import com.tosslab.jandi.app.network.models.start.RealmLong;
import com.tosslab.jandi.app.network.models.start.Topic;

import java.util.List;

import io.realm.RealmList;

public class InitializeInfoConverter implements Converter<InitialInfo, InitialInfo> {
    @Override
    public InitialInfo convert(InitialInfo value) {
        value.setTeamId(value.getTeam().getId());
        long myId = value.getSelf().getId();

        List<Long> starredMessages = value.getStarredMessageIds();
        RealmList<RealmLong> starredMessageIds = new RealmList<>();
        longToRealm(starredMessageIds, starredMessages);
        value.setStarredMessages(starredMessageIds);

        RealmList<Folder> folders = value.getFolders();
        if (folders != null && !folders.isEmpty()) {
            for (Folder folder : folders) {
                RealmList<RealmLong> roomIds = new RealmList<>();
                List<Long> rooms = folder.getRooms();
                folder.set_id(value.getTeamId() + "_" + folder.getId());
                longToRealm(roomIds, rooms);
                folder.setRoomIds(roomIds);
                folder.setTeamId(value.getTeamId());
            }
        }

        RealmList<Topic> topics = value.getTopics();
        if (topics != null && !topics.isEmpty()) {
            for (Topic topic : topics) {
                topic.setTeamId(value.getTeamId());
                List<Long> members = topic.getMembers();
                RealmList<RealmLong> memberIds = new RealmList<>();
                longToRealm(memberIds, members);
                topic.setMemberIds(memberIds);
                RealmList<Marker> markers = topic.getMarkers();

                if (markers != null && !markers.isEmpty()) {
                    for (Marker marker : markers) {
                        marker.setRoomId(topic.getId());
                        marker.setId(topic.getId() + "_" + marker.getMemberId());
                    }
                }

                if (topic.getAnnouncement() != null) {
                    topic.getAnnouncement().setRoomId(topic.getId());
                }
            }
        }

        RealmList<Chat> chats = value.getChats();
        if (chats != null && !chats.isEmpty()) {
            for (Chat chat : chats) {

                chat.setTeamId(value.getTeamId());

                RealmList<Marker> markers = chat.getMarkers();

                RealmList<RealmLong> memberIds = new RealmList<>();
                longToRealm(memberIds, chat.getMembers());
                chat.setMemberIds(memberIds);

                if (markers != null && !markers.isEmpty()) {
                    for (Marker marker : markers) {
                        marker.setRoomId(chat.getId());
                        marker.setId(chat.getId() + "_" + marker.getMemberId());
                    }
                } else {
                    // marker 가 없으면 임의로 지정함
                    RealmList<Marker> markers1 = new RealmList<>();
                    for (Long id : chat.getMembers()) {
                        Marker marker = new Marker();
                        marker.setMemberId(id);
                        marker.setReadLinkId(chat.getLastLinkId() > 0 ? chat.getLastLinkId() : 0);
                        marker.setRoomId(chat.getId());
                        marker.setId(chat.getId() + "_" + marker.getMemberId());
                        markers1.add(marker);
                    }
                    chat.setMarkers(markers1);
                }

                if (chat.getCompanionId() == 0) {
                    for (Long id : chat.getMembers()) {
                        if (id != myId) {
                            chat.setCompanionId(id);
                            break;
                        }
                    }
                }

                if (chat.getLastMessage() != null) {
                    chat.getLastMessage().setChatId(chat.getId());
                }
            }
        }

        RealmList<Bot> bots = value.getBots();
        if (bots != null && !bots.isEmpty()) {
            for (Bot bot : bots) {
                bot.setTeamId(value.getTeamId());
            }
        }

        RealmList<Human> humans = value.getMembers();
        if (humans != null && !humans.isEmpty()) {
            for (Human human : humans) {
                human.setTeamId(value.getTeamId());
                if (human.getProfile() != null) {
                    human.getProfile().setId(human.getId());
                }
                if (human.getId() == myId) {
                    human.setIsStarred(false);
                }
            }

        }

        Poll poll = value.getPoll();
        if (poll != null) {
            poll.setId(value.getTeamId());
        }

        Mention mention = value.getMention();
        if (mention != null) {
            mention.setId(value.getTeamId());
        }

        return value;
    }

    private void longToRealm(RealmList<RealmLong> realmList, List<Long> longs) {
        for (Long roomId : longs) {
            RealmLong object = new RealmLong();
            object.setValue(roomId);
            realmList.add(object);
        }
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

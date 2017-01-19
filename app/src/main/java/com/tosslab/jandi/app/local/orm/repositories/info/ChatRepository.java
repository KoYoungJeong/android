package com.tosslab.jandi.app.local.orm.repositories.info;

import android.support.v4.util.LongSparseArray;

import com.tosslab.jandi.app.local.orm.repositories.template.LockTemplate;
import com.tosslab.jandi.app.network.models.start.Chat;
import com.tosslab.jandi.app.network.models.start.LastMessage;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.room.DirectMessageRoom;

import java.util.ArrayList;
import java.util.List;

public class ChatRepository extends LockTemplate {
    private static LongSparseArray<ChatRepository> instance;

    private LongSparseArray<DirectMessageRoom> chats;

    private ChatRepository() {
        super();
        chats = new LongSparseArray<>();
    }

    synchronized public static ChatRepository getInstance(long teamId) {
        if (instance == null) {
            instance = new LongSparseArray<>();
        }

        if (instance.indexOfKey(teamId) >= 0) {
            return instance.get(teamId);
        } else {
            ChatRepository value = new ChatRepository();
            instance.put(teamId, value);
            return value;

        }
    }

    public static ChatRepository getInstance() {
        return getInstance(TeamInfoLoader.getInstance().getTeamId());
    }

    public boolean updateChatOpened(long roomId, boolean isOpened) {
        return execute(() -> {
            if (hasChat(roomId)) {
                chats.get(roomId).getRaw().setIsOpened(isOpened);
                return true;
            }
            return false;
        });
    }

    public boolean hasChat(long roomId) {
        return execute(() -> chats.indexOfKey(roomId) >= 0);
    }

    public Chat getChat(long chatId) {
        return execute(() -> {
            if (hasChat(chatId)) {
                return chats.get(chatId).getRaw();
            } else {
                return null;
            }
        });
    }

    public boolean addChat(Chat chat) {
        return execute(() -> {

            if (!hasChat(chat.getId())) {
                chats.put(chat.getId(), new DirectMessageRoom(chat));
                return true;
            }
            return false;

        });
    }

    public boolean updateLastMessage(long roomId, long lastMessageId, String text, String status) {
        return execute(() -> {

            if (hasChat(roomId)) {
                Chat chat = chats.get(roomId).getRaw();
                LastMessage lastMessage = chat.getLastMessage();
                if (lastMessage == null) {
                    lastMessage = new LastMessage();
                    chat.setLastMessage(lastMessage);
                }

                lastMessage.setId(lastMessageId);
                lastMessage.setText(text);
                lastMessage.setStatus(status);
                return true;
            }

            return false;
        });
    }

    public boolean isChat(long roomId) {
        return execute(() -> hasChat(roomId));
    }

    public boolean updateUnreadCount(long chatId, int unreadCount) {
        return execute(() -> {

            if (hasChat(chatId)) {
                chats.get(chatId).getRaw().setUnreadCount(unreadCount);
                return true;
            }

            return false;
        });
    }

    public boolean incrementUnreadCount(long chatId) {
        return execute(() -> {
            if (hasChat(chatId)) {
                Chat chat = chats.get(chatId).getRaw();
                chat.setUnreadCount(chat.getUnreadCount() + 1);
                return true;
            }

            return false;
        });
    }

    public boolean updateLastLinkId(long roomId, long linkId) {
        return execute(() -> {

            if (hasChat(roomId)) {
                chats.get(roomId).getRaw().setLastLinkId(linkId);
                return true;
            }

            return false;

        });
    }

    public boolean updateReadLinkId(long roomId, long readId) {
        return execute(() -> {

            if (hasChat(roomId)) {
                Chat raw = chats.get(roomId).getRaw();
                if (raw.getReadLinkId() < readId) {
                    raw.setReadLinkId(readId);
                }
                return true;
            }

            return false;
        });
    }

    public int getUnreadCount(long roomId) {
        return execute(() -> {
            if (hasChat(roomId)) {
                return chats.get(roomId).getUnreadCount();
            }
            return 0;
        });
    }

    public boolean deleteChat(long chatId) {
        return execute(() -> {
            if (hasChat(chatId)) {
                chats.delete(chatId);
            }
            return true;

        });
    }

    public List<Chat> getOpenedChats() {
        return execute(() -> {
            List<Chat> chats = new ArrayList<>();
            int size = this.chats.size();
            for (int idx = 0; idx < size; idx++) {

                DirectMessageRoom room = this.chats.valueAt(idx);
                if (room.isJoined()) {
                    chats.add(room.getRaw());
                }
            }
            return chats;
        });
    }

    public void clear() {
        execute(() -> {
            chats.clear();
            return true;
        });
    }

    public void addDirectRoom(long chatRoomId, DirectMessageRoom chatRoom) {
        execute(() -> {
            chats.put(chatRoomId, chatRoom);
            return true;
        });
    }

    public DirectMessageRoom getDirectRoom(long roomId) {
        return execute(() -> {
            if (hasChat(roomId)) {
                return chats.get(roomId);
            }
            return null;
        });
    }

    public List<DirectMessageRoom> getDirectRooms() {
        return execute(() -> {
            List<DirectMessageRoom> rooms = new ArrayList<>();
            int size = chats.size();
            for (int idx = 0; idx < size; idx++) {
                rooms.add(chats.valueAt(idx));
            }

            return rooms;
        });
    }
}

package com.tosslab.jandi.app.local.orm.repositories.info;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.realm.RealmRepository;
import com.tosslab.jandi.app.network.models.start.Chat;
import com.tosslab.jandi.app.network.models.start.InitialInfo;
import com.tosslab.jandi.app.network.models.start.LastMessage;

public class ChatRepository extends RealmRepository {
    private static ChatRepository instance;

    synchronized public static ChatRepository getInstance() {
        if (instance == null) {
            instance = new ChatRepository();
        }
        return instance;
    }

    public boolean updateChatOpened(long roomId, boolean isOpened) {
        return execute(realm -> {

            Chat chat = realm.where(Chat.class).equalTo("id", roomId).findFirst();
            if (chat != null) {
                realm.executeTransaction(realm1 -> chat.setIsOpened(isOpened));
            }

            return true;
        });
    }

    public Chat getChat(long chatId) {
        return execute((realm) -> {
            Chat it = realm.where(Chat.class)
                    .equalTo("id", chatId)
                    .findFirst();

            return realm.copyFromRealm(it);
        });
    }

    public boolean addChat(Chat chat) {
        return execute((realm) -> {

            long teamId;
            if (chat.getTeamId() > 0) {
                teamId = chat.getTeamId();
            } else {
                teamId = AccountRepository.getRepository().getSelectedTeamId();
                chat.setTeamId(teamId);
            }

            InitialInfo initialInfo = realm.where(InitialInfo.class).equalTo("teamId", teamId).findFirst();
            if (initialInfo != null && realm.where(Chat.class)
                    .equalTo("id", chat.getId())
                    .count() <= 0) {
                realm.executeTransaction(realm1 -> {
                    initialInfo.getChats().add(chat);
                });
            }
            return true;
        });
    }

    public boolean updateLastMessage(long roomId, long lastMessageId, String text, String status) {
        return execute((realm) -> {

            Chat chat = realm.where(Chat.class).equalTo("id", roomId).findFirst();

            if (chat != null) {
                realm.executeTransaction(realm1 -> {
                    LastMessage lastMessage = chat.getLastMessage();
                    if (lastMessage != null) {
                        lastMessage.setStatus(status);
                        lastMessage.setText(text);
                        lastMessage.setId(lastMessageId);
                    } else {
                        lastMessage = realm.createObject(LastMessage.class, lastMessageId);
                        lastMessage.setId(lastMessageId);
                        lastMessage.setText(text);
                        lastMessage.setStatus(status);
                        chat.setLastMessage(lastMessage);
                    }

                });
                return true;
            }

            return false;
        });
    }

    public boolean isChat(long roomId) {
        return execute((realm) -> realm.where(Chat.class).equalTo("id", roomId).count() > 0);
    }

    public boolean updateUnreadCount(long chatId, int unreadCount) {
        return execute((realm) -> {

            Chat chat = realm.where(Chat.class).equalTo("id", chatId).findFirst();
            if (chat != null) {
                realm.executeTransaction(realm1 -> chat.setUnreadCount(unreadCount));
                return true;
            }

            return false;
        });
    }

    public boolean incrementUnreadCount(long chatId) {
        return execute((realm) -> {
            Chat chat = realm.where(Chat.class).equalTo("id", chatId).findFirst();
            if (chat != null) {
                realm.executeTransaction(realm1 -> chat.setUnreadCount(chat.getUnreadCount() + 1));
            }

            return false;
        });
    }

    public boolean updateLastLinkId(long roomId, long linkId) {
        return execute((realm) -> {

            Chat chat = realm.where(Chat.class)
                    .equalTo("id", roomId)
                    .findFirst();

            if (chat != null) {
                realm.executeTransaction(realm1 -> chat.setLastLinkId(linkId));
                return true;
            }

            return false;

        });
    }

    public boolean updateReadLinkId(long roomId, long readId) {
        return execute((realm) -> {

            Chat chat = realm.where(Chat.class).equalTo("id", roomId).findFirst();
            if (chat != null) {
                realm.executeTransaction(realm1 -> chat.setReadLinkId(readId));
                return true;
            }

            return false;
        });
    }

    public boolean deleteChat(long chatId) {
        return execute((realm) -> {

            Chat chat = realm.where(Chat.class).equalTo("id", chatId).findFirst();
            if (chat != null) {
                realm.executeTransaction(realm1 -> chat.deleteFromRealm());
                return true;
            }

            return false;
        });
    }
}

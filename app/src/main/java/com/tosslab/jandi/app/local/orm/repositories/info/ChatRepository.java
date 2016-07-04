package com.tosslab.jandi.app.local.orm.repositories.info;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.tosslab.jandi.app.local.orm.repositories.template.LockExecutorTemplate;
import com.tosslab.jandi.app.network.models.start.Chat;

import java.sql.SQLException;

public class ChatRepository extends LockExecutorTemplate {
    private static ChatRepository instance;

    synchronized public static ChatRepository getInstance() {
        if (instance == null) {
            instance = new ChatRepository();
        }
        return instance;
    }

    public boolean updateChatOpened(long roomId, boolean isOpened) {
        return execute(() -> {
            try {
                Dao<Chat, Long> dao = getHelper().getDao(Chat.class);
                UpdateBuilder<Chat, Long> chatUpdateBuilder = dao.updateBuilder();
                chatUpdateBuilder.updateColumnValue("isOpened", isOpened)
                        .where()
                        .eq("id", roomId);
                return chatUpdateBuilder.update() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    public Chat getChat(long chatId) {
        return execute(() -> {

            try {
                Dao<Chat, Long> dao = getDao(Chat.class);
                return dao.queryForId(chatId);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    public boolean addChat(Chat chat) {
        return execute(() -> {
            try {
                Dao<Chat, Object> dao = getDao(Chat.class);
                return dao.create(chat) > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    public boolean updateLastMessage(long roomId, long linkId, String text, String status) {
        return execute(() -> {

            try {
                Dao<Chat, Long> dao = getDao(Chat.class);
                Dao<Chat.LastMessage, Object> lastMessageDao = getDao(Chat.LastMessage.class);
                Chat.LastMessage lastMessage = new Chat.LastMessage();
                lastMessage.setId(linkId);
                lastMessage.setText(text);
                lastMessage.setStatus(status);

                UpdateBuilder<Chat, Long> chatUpdateBuilder = dao.updateBuilder();
                chatUpdateBuilder.updateColumnValue("lastMessage_id", linkId)
                        .where()
                        .eq("id", roomId);
                chatUpdateBuilder.update();

                if (lastMessageDao.queryForId(linkId) != null) {
                    return lastMessageDao.update(lastMessage) > 0;
                } else {
                    return lastMessageDao.create(lastMessage) > 0;
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

            return false;
        });
    }

    public boolean isChat(long roomId) {
        return execute(() -> {
            try {
                Dao<Chat, Long> dao = getDao(Chat.class);
                return dao.queryBuilder()
                        .where()
                        .eq("id", roomId)
                        .countOf() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return false;
        });
    }

    public boolean updateUnreadCount(long chatId, int unreadCount) {
        return execute(() -> {
            try {
                Dao<Chat, Long> dao = getDao(Chat.class);
                UpdateBuilder<Chat, Long> updateBuilder = dao.updateBuilder();
                updateBuilder.updateColumnValue("unreadCount", unreadCount)
                        .where()
                        .eq("id", chatId);
                return updateBuilder.update() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return false;
        });
    }

    public boolean updateLastLinkId(long roomId, long linkId) {
        return execute(() -> {
            try {
                Dao<Chat, Object> dao = getDao(Chat.class);
                UpdateBuilder<Chat, Object> updateBuilder = dao.updateBuilder();
                updateBuilder.updateColumnValue("lastLinkId", linkId)
                        .where()
                        .eq("id", roomId);
                return updateBuilder.update() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    public boolean updateReadLinkId(long roomId, long readId) {
        return execute(() -> {
            try {
                Dao<Chat, Long> dao = getDao(Chat.class);
                UpdateBuilder<Chat, Long> updateBuilder = dao.updateBuilder();
                updateBuilder.updateColumnValue("readLinkId", readId)
                        .where()
                        .eq("id", roomId);
                return updateBuilder.update() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    public boolean deleteChat(long chatId) {
        return execute(() -> {
            try {
                Dao<Chat, Object> dao = getDao(Chat.class);
                return dao.deleteById(chatId) > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });
    }
}

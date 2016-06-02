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

    public void updateStarred(long chatId, boolean isStarred) {
        execute(() -> {
            try {

                Dao<Chat, ?> dao = getHelper().getDao(Chat.class);
                UpdateBuilder<Chat, ?> chatUpdateBuilder = dao.updateBuilder();
                chatUpdateBuilder.updateColumnValue("isStarred", isStarred)
                        .where()
                        .eq("id", chatId);
                chatUpdateBuilder.update();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    public void deleteChat(long roomId) {
        execute(() -> {
            try {
                Dao<Chat, Long> dao = getHelper().getDao(Chat.class);
                return dao.deleteById(roomId) > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });
    }
}

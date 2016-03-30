package com.tosslab.jandi.app.local.orm.repositories;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.tosslab.jandi.app.local.orm.repositories.template.LockExecutorTemplate;
import com.tosslab.jandi.app.network.models.ResChat;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 7. 22..
 */
public class ChatRepository extends LockExecutorTemplate {

    public static ChatRepository getRepository() {
        return new ChatRepository();
    }

    public boolean upsertChats(List<ResChat> chats) {
        return execute(() -> {
            try {
                long selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();

                Dao<ResChat, ?> chatDao = getHelper().getDao(ResChat.class);
                UpdateBuilder<ResChat, ?> updateBuilder = chatDao.updateBuilder();
                updateBuilder.updateColumnValue("isOld", true);
                updateBuilder.update();

                chatDao.callBatchTasks(() -> {
                    int order = 0;
                    for (ResChat chat : chats) {
                        chat.setTeamId(selectedTeamId);
                        chat.setOrder(order++);
                        chat.setIsOld(false);
                        chatDao.createOrUpdate(chat);
                    }
                    return null;
                });
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;

        });
    }

    public List<ResChat> getChats() {
        return execute(() -> {
            try {
                long selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
                Dao<ResChat, ?> chatDao = getHelper().getDao(ResChat.class);
                return chatDao.queryBuilder()
                        .orderBy("order", true)
                        .where()
                        .eq("teamId", selectedTeamId)
                        .and()
                        .eq("isOld", false)
                        .query();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return new ArrayList<ResChat>();

        });
    }

    public ResChat getChat(long userId) {
        return execute(() -> {
            try {
                long selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
                return getHelper().getDao(ResChat.class)
                        .queryBuilder()
                        .where()
                        .eq("teamId", selectedTeamId)
                        .and()
                        .eq("userId", userId)
                        .queryForFirst();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return new ResChat();

        });
    }

    public int deleteChat(long roomId) {
        return execute(() -> {
            try {
                Dao<ResChat, ?> dao = getHelper().getDao(ResChat.class);
                DeleteBuilder<ResChat, ?> deleteBuilder = dao.deleteBuilder();
                deleteBuilder
                        .where()
                        .eq("roomId", roomId);

                return deleteBuilder.delete();

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0;

        });
    }

    public ResChat getChatByRoom(long roomId) {
        return execute(() -> {
            try {
                long selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
                Dao<ResChat, ?> chatDao = getHelper().getDao(ResChat.class);
                return chatDao.queryBuilder()
                        .orderBy("order", true)
                        .where()
                        .eq("teamId", selectedTeamId)
                        .and()
                        .eq("roomId", roomId)
                        .queryForFirst();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            ResChat resChat = new ResChat();
            resChat.setEntityId(-1);
            return resChat;

        });
    }
}

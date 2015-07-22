package com.tosslab.jandi.app.local.orm.repositories;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.OrmDatabaseHelper;
import com.tosslab.jandi.app.network.models.ResChat;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 7. 22..
 */
public class ChatRepository {

    private static ChatRepository repository;
    private final OrmDatabaseHelper helper;

    private ChatRepository() {
        helper = OpenHelperManager.getHelper(JandiApplication.getContext(), OrmDatabaseHelper.class);
    }

    public static ChatRepository getRepository() {
        if (repository == null) {
            repository = new ChatRepository();
        }
        return repository;
    }

    public boolean upsertChats(List<ResChat> chats) {
        try {
            Dao<ResChat, ?> chatDao = helper.getDao(ResChat.class);

            int selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();

            for (ResChat chat : chats) {
                chat.setTeamId(selectedTeamId);
                chatDao.createOrUpdate(chat);
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;

    }

    public List<ResChat> getChats() {
        int selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
        try {
            Dao<ResChat, ?> chatDao = helper.getDao(ResChat.class);
            return chatDao.queryBuilder()
                    .where()
                    .eq("teamId", selectedTeamId)
                    .query();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    public int deleteChat(int roomId) {
        try {
            Dao<ResChat, ?> dao = helper.getDao(ResChat.class);
            DeleteBuilder<ResChat, ?> deleteBuilder = dao.deleteBuilder();
            deleteBuilder
                    .where()
                    .eq("roomId", roomId);

            return deleteBuilder.delete();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}

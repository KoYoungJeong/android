package com.tosslab.jandi.app.local.orm.dao;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.tosslab.jandi.app.network.models.start.Bot;
import com.tosslab.jandi.app.network.models.start.Chat;
import com.tosslab.jandi.app.network.models.start.Folder;
import com.tosslab.jandi.app.network.models.start.Human;
import com.tosslab.jandi.app.network.models.start.InitialInfo;
import com.tosslab.jandi.app.network.models.start.Team;
import com.tosslab.jandi.app.network.models.start.Topic;

import java.sql.SQLException;
import java.util.Collection;

public class InitializeInfoDaoImpl extends BaseDaoImpl<InitialInfo, Long> {
    public InitializeInfoDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, InitialInfo.class);
    }

    @Override
    public int create(InitialInfo data) throws SQLException {
        int id = super.create(data);

        if (data.getSelf() != null) {
            Dao<InitialInfo.Self, ?> dao = DaoManager.createDao(getConnectionSource(), InitialInfo.Self.class);
            dao.createOrUpdate(data.getSelf());
        }

        if (data.getPoll() != null) {
            Dao<InitialInfo.Poll, ?> dao = DaoManager.createDao(getConnectionSource(), InitialInfo.Poll.class);
            dao.createOrUpdate(data.getPoll());
        }

        if (data.getMention() != null) {
            Dao<InitialInfo.Mention, ?> dao = DaoManager.createDao(getConnectionSource(), InitialInfo.Mention.class);
            dao.createOrUpdate(data.getMention());
        }

        if (data.getTeam() != null) {
            Dao<Team, ?> dao = DaoManager.createDao(getConnectionSource(), Team.class);
            dao.createOrUpdate(data.getTeam());
        }

        upsertFolders(data.getTeamId(), data.getFolders());
        upsertTopics(data.getTeamId(), data.getTopics());
        upsertChats(data.getTeamId(), data.getChats());
        upsertHumans(data.getTeamId(), data.getMembers());
        upsertBots(data.getTeamId(), data.getBots());

        return id;
    }


    private void upsertFolders(long teamId, Collection<Folder> folders) throws SQLException {
        Dao<Folder, ?> dao = DaoManager.createDao(getConnectionSource(), Folder.class);
        DeleteBuilder<Folder, ?> folderDeleteBuilder = dao.deleteBuilder();
        folderDeleteBuilder.where()
                .eq("initialInfo_id", teamId);
        folderDeleteBuilder.delete();
        if (folders != null && !folders.isEmpty()) {

            try {
                dao.callBatchTasks(() -> {
                    for (Folder folder : folders) {
                        dao.create(folder);
                    }
                    return null;
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void upsertTopics(long teamId, Collection<Topic> topics) throws SQLException {
        Dao<Topic, ?> dao = DaoManager.createDao(getConnectionSource(), Topic.class);
        DeleteBuilder<Topic, ?> topicDeleteBuilder = dao.deleteBuilder();
        topicDeleteBuilder.where()
                .eq("initialInfo_id", teamId);
        topicDeleteBuilder.delete();

        if (topics != null && !topics.isEmpty()) {
            try {
                dao.callBatchTasks(() -> {
                    for (Topic topic : topics) {
                        dao.create(topic);
                    }
                    return null;
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void upsertChats(long teamId, Collection<Chat> chats) throws SQLException {
        Dao<Chat, ?> dao = DaoManager.createDao(getConnectionSource(), Chat.class);
        DeleteBuilder<Chat, ?> topicDeleteBuilder = dao.deleteBuilder();
        topicDeleteBuilder.where()
                .eq("initialInfo_id", teamId);
        topicDeleteBuilder.delete();

        if (chats != null && !chats.isEmpty()) {
            try {
                dao.callBatchTasks(() -> {
                    for (Chat topic : chats) {
                        dao.create(topic);
                    }
                    return null;
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void upsertHumans(long teamId, Collection<Human> humans) throws SQLException {
        Dao<Human, ?> dao = DaoManager.createDao(getConnectionSource(), Human.class);
        DeleteBuilder<Human, ?> topicDeleteBuilder = dao.deleteBuilder();
        topicDeleteBuilder.where()
                .eq("initialInfo_id", teamId);
        topicDeleteBuilder.delete();

        if (humans != null && !humans.isEmpty()) {
            try {
                dao.callBatchTasks(() -> {
                    for (Human topic : humans) {
                        dao.create(topic);
                    }
                    return null;
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void upsertBots(long teamId, Collection<Bot> bots) throws SQLException {
        Dao<Bot, ?> dao = DaoManager.createDao(getConnectionSource(), Bot.class);
        DeleteBuilder<Bot, ?> topicDeleteBuilder = dao.deleteBuilder();
        topicDeleteBuilder.where()
                .eq("initialInfo_id", teamId);
        topicDeleteBuilder.delete();

        if (bots != null && !bots.isEmpty()) {
            try {
                dao.callBatchTasks(() -> {
                    for (Bot topic : bots) {
                        dao.create(topic);
                    }
                    return null;
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int update(InitialInfo data) throws SQLException {
        int row = super.update(data);

        if (data.getSelf() != null) {
            Dao<InitialInfo.Self, ?> dao = DaoManager.createDao(getConnectionSource(), InitialInfo.Self.class);
            dao.update(data.getSelf());
        }

        if (data.getPoll() != null) {
            Dao<InitialInfo.Poll, ?> dao = DaoManager.createDao(getConnectionSource(), InitialInfo.Poll.class);
            dao.update(data.getPoll());
        }

        if (data.getMention() != null) {
            Dao<InitialInfo.Mention, ?> dao = DaoManager.createDao(getConnectionSource(), InitialInfo.Mention.class);
            dao.update(data.getMention());
        }

        if (data.getTeam() != null) {
            Dao<Team, ?> dao = DaoManager.createDao(getConnectionSource(), Team.class);
            dao.update(data.getTeam());
        }

        upsertFolders(data.getTeamId(), data.getFolders());
        upsertTopics(data.getTeamId(), data.getTopics());
        upsertChats(data.getTeamId(), data.getChats());
        upsertHumans(data.getTeamId(), data.getMembers());
        upsertBots(data.getTeamId(), data.getBots());

        return row;
    }
}

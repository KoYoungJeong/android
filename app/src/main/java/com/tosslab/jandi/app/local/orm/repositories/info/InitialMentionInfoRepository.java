package com.tosslab.jandi.app.local.orm.repositories.info;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.template.LockExecutorTemplate;
import com.tosslab.jandi.app.network.models.start.InitialInfo;

import java.sql.SQLException;

/**
 * Created by tony on 2016. 9. 20..
 */
public class InitialMentionInfoRepository extends LockExecutorTemplate {

    private static InitialMentionInfoRepository instance;

    synchronized public static InitialMentionInfoRepository getInstance() {
        if (instance == null) {
            instance = new InitialMentionInfoRepository();
        }
        return instance;
    }

    public InitialInfo.Mention getMention() {
        return execute(() -> {
            long selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
            try {
                Dao<InitialInfo.Mention, Long> dao = getDao(InitialInfo.Mention.class);
                return dao.queryForId(selectedTeamId);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            InitialInfo.Mention mention = new InitialInfo.Mention();
            mention.setId(selectedTeamId);
            mention.setLastMentionedMessageId(-1);
            mention.setUnreadCount(0);
            return mention;
        });
    }

    public boolean upsertMention(InitialInfo.Mention mention) {
        if (mention == null) {
            return false;
        }

        return execute(() -> {
            try {
                Dao<InitialInfo.Mention, Object> dao = getDao(InitialInfo.Mention.class);
                dao.createOrUpdate(mention);
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    public boolean clearUnreadCount() {
        return execute(() -> {
            try {
                long selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
                Dao<InitialInfo.Mention, Long> dao = getDao(InitialInfo.Mention.class);
                UpdateBuilder<InitialInfo.Mention, Long> updateBuilder = dao.updateBuilder();
                updateBuilder.updateColumnValue("unreadCount", 0)
                        .where().eq("id", selectedTeamId);
                return updateBuilder.update() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    public boolean increaseUnreadCount() {
        return execute(() -> {
            try {
                long selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
                Dao<InitialInfo.Mention, Long> dao = getDao(InitialInfo.Mention.class);
                UpdateBuilder<InitialInfo.Mention, Long> updateBuilder = dao.updateBuilder();
                updateBuilder.updateColumnExpression("unreadCount", "unreadCount + 1")
                        .where().eq("id", selectedTeamId);
                return updateBuilder.update() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    public boolean decreaseUnreadCount() {
        return execute(() -> {
            try {
                long selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
                Dao<InitialInfo.Mention, Object> dao = getDao(InitialInfo.Mention.class);
                UpdateBuilder<InitialInfo.Mention, Object> updateBuilder = dao.updateBuilder();
                updateBuilder.updateColumnExpression("unreadCount", "unreadCount - 1")
                        .where().eq("id", selectedTeamId);
                return updateBuilder.update() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });
    }
}
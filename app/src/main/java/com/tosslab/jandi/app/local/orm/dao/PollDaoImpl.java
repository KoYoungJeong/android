package com.tosslab.jandi.app.local.orm.dao;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.tosslab.jandi.app.network.models.poll.Poll;

import java.sql.SQLException;
import java.util.Collection;

/**
 * Created by tonyjs on 16. 6. 15..
 */
public class PollDaoImpl extends BaseDaoImpl<Poll, Integer> {

    public PollDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, Poll.class);
    }

    @Override
    public int create(Poll data) throws SQLException {
        upsertPoll(data);
        return super.create(data);
    }

    @Override
    public int update(Poll data) throws SQLException {
        upsertPoll(data);
        return super.update(data);
    }

    private void upsertPoll(Poll poll) throws SQLException {
        Dao<Poll.Item, ?> pollItemDao =
                DaoManager.createDao(getConnectionSource(), Poll.Item.class);
        DeleteBuilder<Poll.Item, ?> pollDaoDeleteBuilder = pollItemDao.deleteBuilder();
        pollDaoDeleteBuilder.where()
                .eq("poll_id", poll.getId());
        pollDaoDeleteBuilder.delete();

        Collection<Poll.Item> items = poll.getItems();
        if (items != null && !items.isEmpty()) {
            for (Poll.Item item : items) {
                pollItemDao.create(item);
            }
        }

        Collection<Poll.Item> electedItems = poll.getElectedItems();
        if (electedItems != null && !electedItems.isEmpty()) {
            for (Poll.Item item : electedItems) {
                item.setElected(true);
                pollItemDao.createOrUpdate(item);
            }
        }

    }
}

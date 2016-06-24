package com.tosslab.jandi.app.local.orm.repositories;

import com.j256.ormlite.dao.Dao;
import com.tosslab.jandi.app.local.orm.repositories.template.LockExecutorTemplate;
import com.tosslab.jandi.app.network.models.poll.Poll;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tonyjs on 16. 6. 21..
 */
public class PollRepository extends LockExecutorTemplate {

    private static PollRepository pollRepository;

    public static PollRepository getInstance() {
        if (pollRepository == null) {
            pollRepository = new PollRepository();
        }
        return pollRepository;
    }

    public boolean upsertPoll(Poll poll) {
        if (poll == null || poll.getId() <= 0) {
            return false;
        }

        return execute(() -> {
            try {
                Dao<Poll, ?> dao = getHelper().getDao(Poll.class);
                dao.createOrUpdate(poll);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    public List<Poll> getPolls() {
        return execute(() -> {
            try {
                return getHelper().getDao(Poll.class)
                        .queryForAll();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return new ArrayList<Poll>();
        });
    }

}

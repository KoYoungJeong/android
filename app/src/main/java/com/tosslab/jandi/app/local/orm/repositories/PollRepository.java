package com.tosslab.jandi.app.local.orm.repositories;

import android.text.TextUtils;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
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
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    public boolean upsertPollStatus(long topicId, String status) {
        if (topicId <= 0 || TextUtils.isEmpty(status)) {
            return false;
        }

        if (status.equals("created")
                || status.equals("finished")
                || status.equals("deleted")) {

            return execute(() -> {
                try {
                    Dao<Poll, ?> dao = getHelper().getDao(Poll.class);
                    UpdateBuilder<Poll, ?> pollUpdateBuilder = dao.updateBuilder();
                    pollUpdateBuilder.updateColumnValue("status", status);

                    pollUpdateBuilder.where()
                            .eq("topicId", topicId);

                    pollUpdateBuilder.update();

                    return true;
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                return false;
            });
        }

        return false;
    }

    public boolean upsertPollVoteStatus(Poll poll) {

        if (poll == null || poll.getId() <= 0 || TextUtils.isEmpty(poll.getStatus())) {
            return false;
        }

        return execute(() -> {
            try {
                Dao<Poll, ?> dao = getHelper().getDao(Poll.class);
                UpdateBuilder<Poll, ?> pollUpdateBuilder = dao.updateBuilder();

                pollUpdateBuilder.updateColumnValue("status", poll.getStatus());
                pollUpdateBuilder.updateColumnValue("voteStatus", poll.getVoteStatus());
                pollUpdateBuilder.updateColumnValue("votedItemSeqs", poll.getVotedItemSeqs());

                pollUpdateBuilder.where()
                        .eq("id", poll.getId());

                pollUpdateBuilder.update();
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

    public boolean upsertPollList(List<Poll> polls) {
        return execute(() -> {
            try {

                Dao<Poll, ?> dao = getHelper().getDao(Poll.class);

                // 내부에서 트랜잭션 commit 컨트롤을 함
                dao.callBatchTasks(() -> {
                    for (Poll poll : polls) {
                        dao.createOrUpdate(poll);
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

    public int clearAll() {
        return execute(() -> {
            try {
                Dao<Poll, ?> dao = getHelper().getDao(Poll.class);
                DeleteBuilder<Poll, ?> deleteBuilder = dao.deleteBuilder();
                return deleteBuilder.delete();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return 0;

        });
    }

    public Poll getPollById(long pollId) {
        return execute(() -> {
            try {
                Dao<Poll, ?> dao = getHelper().getDao(Poll.class);
                return dao.queryBuilder()
                        .where()
                        .eq("id", pollId)
                        .queryForFirst();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            Poll poll = new Poll();
            poll.setId(-1);
            return poll;
        });
    }

    public int clear(long teamId) {
        return execute(() -> {
            try {
                Dao<Poll, ?> dao = getHelper().getDao(Poll.class);
                DeleteBuilder<Poll, ?> deleteBuilder = dao.deleteBuilder();
                deleteBuilder.where().eq("teamId", teamId);
                return deleteBuilder.delete();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return 0;

        });
    }

    public int removeOfTopic(long topicId) {
        return execute(() -> {
            try {
                Dao<Poll, Long> dao = getDao(Poll.class);
                DeleteBuilder<Poll, Long> deleteBuilder = dao.deleteBuilder();
                deleteBuilder.where()
                        .eq("topicId", topicId);

                return deleteBuilder.delete();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0;
        });
    }
}

package com.tosslab.jandi.app.local.orm.repositories.info;

import android.support.v4.util.LongSparseArray;

import com.tosslab.jandi.app.local.orm.repositories.template.LockTemplate;
import com.tosslab.jandi.app.network.models.start.Poll;
import com.tosslab.jandi.app.team.TeamInfoLoader;

public class InitialPollInfoRepository extends LockTemplate {

    private static LongSparseArray<InitialPollInfoRepository> instance;

    private Poll poll;

    private InitialPollInfoRepository() {
        super();
    }

    synchronized public static InitialPollInfoRepository getInstance(long teamId) {
        if (instance == null) {
            instance = new LongSparseArray<>();
        }

        if (instance.indexOfKey(teamId) >= 0) {
            return instance.get(teamId);
        } else {
            InitialPollInfoRepository value = new InitialPollInfoRepository();
            instance.put(teamId, value);
            return value;

        }
    }

    public static InitialPollInfoRepository getInstance() {
        return getInstance(TeamInfoLoader.getInstance().getTeamId());
    }

    private Poll getPoll() {
        return execute(() -> {
            if (poll == null) {
                poll = new Poll();
            }
            return poll;
        });
    }

    public int getVotableCount() {
        return execute(() -> getPoll().getVotableCount());
    }

    public boolean increaseVotableCount() {
        return execute(() -> {

            Poll poll = getPoll();
            poll.setVotableCount(poll.getVotableCount() + 1);

            return true;
        });
    }

    public boolean decreaseVotableCount() {
        return execute(() -> {

            Poll poll = getPoll();
            poll.setVotableCount(poll.getVotableCount() - 1);

            return true;
        });
    }

    public boolean updateVotableCount(int votableCount) {
        return execute(() -> {
            getPoll().setVotableCount(votableCount);

            return true;
        });
    }
}
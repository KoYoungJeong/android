package com.tosslab.jandi.app.local.orm.repositories.info;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.realm.RealmRepository;
import com.tosslab.jandi.app.network.models.start.Poll;

/**
 * Created by tee on 2016. 8. 17..
 */

public class InitialPollInfoRepository extends RealmRepository {

    private static InitialPollInfoRepository instance;

    synchronized public static InitialPollInfoRepository getInstance() {
        if (instance == null) {
            instance = new InitialPollInfoRepository();
        }
        return instance;
    }

    public int getVotableCount() {
        return execute(realm -> {

            long selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
            Poll poll = realm.where(Poll.class).equalTo("id", selectedTeamId).findFirst();
            if (poll != null) {
                return poll.getVotableCount();
            }

            return 0;
        });
    }

    public boolean increaseVotableCount() {
        return execute(realm -> {

            long selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
            Poll poll = realm.where(Poll.class).equalTo("id", selectedTeamId).findFirst();
            if (poll != null) {
                realm.executeTransaction(realm1 -> poll.setVotableCount(poll.getVotableCount() + 1));
                return true;
            }

            return false;
        });
    }

    public boolean decreaseVotableCount() {
        return execute(realm -> {

            long selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
            Poll poll = realm.where(Poll.class).equalTo("id", selectedTeamId).findFirst();
            if (poll != null && poll.getVotableCount() > 0) {
                realm.executeTransaction(realm1 -> poll.setVotableCount(poll.getVotableCount() - 1));
                return true;
            }

            return false;
        });
    }

    public boolean updateVotableCount(int votableCount) {
        return execute(realm -> {
            long selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();

            Poll poll = realm.where(Poll.class).equalTo("id", selectedTeamId).findFirst();
            if (poll != null) {
                realm.executeTransaction(realm1 -> poll.setVotableCount(votableCount));
                return true;
            }

            return false;
        });
    }
}
package com.tosslab.jandi.app.local.orm.repositories.info;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.realm.RealmRepository;
import com.tosslab.jandi.app.network.models.start.Mention;

/**
 * Created by tony on 2016. 9. 20..
 */
public class InitialMentionInfoRepository extends RealmRepository {

    private static InitialMentionInfoRepository instance;

    synchronized public static InitialMentionInfoRepository getInstance() {
        if (instance == null) {
            instance = new InitialMentionInfoRepository();
        }
        return instance;
    }

    public Mention getMention() {
        return execute((realm) -> {

            long selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
            Mention mention = realm.where(Mention.class)
                    .equalTo("id", selectedTeamId).findFirst();
            if (mention == null) {
                realm.executeTransaction(realm1 -> {
                    Mention mention2 = realm.createObject(Mention.class, selectedTeamId);
                    mention2.setLastMentionedMessageId(-1);
                    mention2.setUnreadCount(0);

                });

                return realm.where(Mention.class)
                        .equalTo("id", selectedTeamId)
                        .findFirst();
            }

            return mention;
        });
    }

    public boolean upsertMention(Mention mention) {

        return execute(realm -> {
            if (mention == null) {
                return false;
            }

            realm.executeTransaction(realm1 -> realm.copyToRealmOrUpdate(mention));

            return true;
        });
    }

    public boolean clearUnreadCount() {
        return execute(realm -> {
            long selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
            Mention mention = realm.where(Mention.class)
                    .equalTo("id", selectedTeamId)
                    .findFirst();

            if (mention != null) {
                realm.executeTransaction(realm1 -> mention.setUnreadCount(0));
                return true;
            }

            return false;
        });
    }

    public boolean increaseUnreadCount() {
        return execute(realm -> {


            long selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();

            Mention mention = realm.where(Mention.class)
                    .equalTo("id", selectedTeamId)
                    .findFirst();

            if (mention != null) {
                realm.executeTransaction(realm1 -> mention.setUnreadCount(mention.getUnreadCount() + 1));
                return true;
            }

            return false;
        });
    }

    public boolean decreaseUnreadCount() {
        return execute(realm -> {

            long selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();

            Mention mention = realm.where(Mention.class)
                    .equalTo("id", selectedTeamId)
                    .findFirst();

            if (mention != null && mention.getUnreadCount() > 0) {
                realm.executeTransaction(realm1 -> mention.setUnreadCount(mention.getUnreadCount() - 1));
                return true;
            }

            return false;
        });
    }
}
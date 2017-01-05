package com.tosslab.jandi.app.local.orm.repositories.info;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.realm.RealmRepository;
import com.tosslab.jandi.app.network.models.start.Human;
import com.tosslab.jandi.app.network.models.start.InitialInfo;
import com.tosslab.jandi.app.network.models.start.Profile;

import java.util.List;

import io.realm.RealmResults;

public class HumanRepository extends RealmRepository {
    private static HumanRepository instance;

    synchronized public static HumanRepository getInstance() {
        if (instance == null) {
            instance = new HumanRepository();
        }
        return instance;
    }

    public boolean isHuman(long memberId) {
        return execute((realm) -> realm.where(Human.class)
                .equalTo("id", memberId)
                .count() > 0);
    }

    public Human getHuman(long memberId) {
        return execute((realm) -> {
            Human it = realm.where(Human.class)
                    .equalTo("id", memberId)
                    .findFirst();
            if (it != null) {
                return realm.copyFromRealm(it);
            } else {
                return null;
            }
        });
    }

    public int getMemberCount(long teamId) {
        return execute((realm) -> (int) realm.where(Human.class)
                .equalTo("teamId", teamId)
                .count());
    }

    public boolean updateStatus(long memberId, String status) {
        return execute((realm) -> {

            Human human = realm.where(Human.class).equalTo("id", memberId).findFirst();
            if (human != null) {
                realm.executeTransaction(realm1 -> human.setStatus(status));
                return true;
            }

            return false;
        });
    }

    public boolean updatePhotoUrl(long memberId, String photoUrl) {
        return execute((realm) -> {

            Human human = realm.where(Human.class).equalTo("id", memberId).findFirst();
            if (human != null) {
                realm.executeTransaction(realm1 -> human.setPhotoUrl(photoUrl));
                return true;
            }

            return false;
        });
    }

    public boolean updateHuman(Human member) {
        return execute((realm) -> {
            long selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
            member.setTeamId(selectedTeamId);
            if (member.getProfile() != null) {
                member.getProfile().setId(member.getId());
            }

            realm.executeTransaction(realm1 -> realm.copyToRealmOrUpdate(member));
            return true;
        });
    }

    public boolean addHuman(long teamId, Human member) {
        return execute((realm) -> {

            InitialInfo initialInfo = realm.where(InitialInfo.class).equalTo("teamId", teamId).findFirst();
            if (initialInfo != null) {
                realm.executeTransaction(realm1 -> {
                    member.setTeamId(teamId);
                    if (member.getProfile() != null) {
                        member.getProfile().setId(member.getId());
                    }
                    initialInfo.getMembers().add(member);
                });
                return true;
            } else {
                return false;
            }

        });
    }

    public boolean updateStarred(long memberId, boolean isStarred) {
        return execute((realm) -> {


            Human human = realm.where(Human.class).equalTo("id", memberId).findFirst();
            if (human != null) {
                realm.executeTransaction(realm1 -> human.setIsStarred(isStarred));
                return true;
            }

            return false;
        });
    }

    public boolean containsPhone(String queryNum) {
        return execute(realm -> realm.where(Profile.class)
                .contains("phoneNumber", queryNum)
                .count() > 0);
    }

    public List<Human> getContainsPhone(String queryNum) {
        return execute(realm -> {
            RealmResults<Human> it = realm.where(Human.class)
                    .contains("profile.phoneNumber", queryNum)
                    .findAll();
            if (it != null) {
                return realm.copyFromRealm(it);
            } else {
                return null;
            }
        });
    }

    public boolean updateRank(long userId, long rankId) {
        return execute(realm -> {

            Human human = realm.where(Human.class)
                    .equalTo("id", userId)
                    .findFirst();

            if (human != null) {
                realm.executeTransaction(realm1 -> {
                    human.setRankId(rankId);
                });

                return true;
            }

            return false;
        });
    }
}

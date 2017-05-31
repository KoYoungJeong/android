package com.tosslab.jandi.app.local.orm.repositories.info;

import android.support.v4.util.LongSparseArray;

import com.tosslab.jandi.app.local.orm.repositories.template.LockTemplate;
import com.tosslab.jandi.app.network.models.start.Absence;
import com.tosslab.jandi.app.network.models.start.Human;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;

import java.util.ArrayList;
import java.util.List;

public class HumanRepository extends LockTemplate {
    private static LongSparseArray<HumanRepository> instance;

    private LongSparseArray<User> users;

    private HumanRepository() {
        super();
        users = new LongSparseArray<>();
    }

    synchronized public static HumanRepository getInstance(long teamId) {
        if (instance == null) {
            instance = new LongSparseArray<>();
        }

        if (instance.indexOfKey(teamId) >= 0) {
            return instance.get(teamId);
        } else {
            HumanRepository repository = new HumanRepository();
            instance.put(teamId, repository);
            return repository;
        }

    }

    public static HumanRepository getInstance() {
        return getInstance(TeamInfoLoader.getInstance().getTeamId());
    }

    public boolean isHuman(long memberId) {
        return execute(() -> hasUser(memberId));
    }

    public Human getHuman(long memberId) {
        return execute(() -> {
            if (hasUser(memberId)) {
                return users.get(memberId).getRaw();
            } else {
                return null;
            }
        });
    }

    public boolean hasUser(long memberId) {
        return execute(() -> users.indexOfKey(memberId) >= 0);
    }

    public int getMemberCount() {
        return execute(() -> users.size());
    }

    public boolean updateStatus(long memberId, String status) {
        return execute(() -> {
            if (hasUser(memberId)) {
                users.get(memberId).getRaw().setStatus(status);
                return true;
            }

            return false;
        });
    }

    public boolean updatePhotoUrl(long memberId, String photoUrl) {
        return execute(() -> {
            if (hasUser(memberId)) {
                users.get(memberId).getRaw().setPhotoUrl(photoUrl);
                return true;
            }
            return false;
        });
    }

    public boolean updateHuman(Human member) {
        return execute(() -> {
            if (hasUser(member.getId())) {
                Human saved = users.get(member.getId()).getRaw();
                saved.setType(member.getType());
                saved.setName(member.getName());
                saved.setPhotoUrl(member.getPhotoUrl());
                saved.setAccountId(member.getAccountId());
                saved.setStatus(member.getStatus());
                saved.setProfile(member.getProfile());
                saved.setJoinTopics(member.getJoinTopics());
                saved.setRankId(member.getRankId());
                return true;
            } else {
                users.put(member.getId(), new User(member));
                return false;
            }
        });
    }

    public boolean addHuman(Human member) {
        return execute(() -> {
            if (hasUser(member.getId())) {
                User user = users.get(member.getId());
                member.setIsStarred(user.isStarred());
            }
            users.put(member.getId(), new User(member));
            return true;
        });
    }

    public void addUser(User user) {
        users.put(user.getId(), user);
    }

    public boolean updateStarred(long memberId, boolean isStarred) {
        return execute(() -> {
            if (hasUser(memberId)) {
                users.get(memberId).getRaw().setIsStarred(isStarred);
                return true;
            }
            return false;
        });
    }

    public boolean containsPhone(String queryNum) {
        return execute(() -> {
            for (int idx = 0; idx < users.size(); idx++) {
                if (users.valueAt(idx).getPhoneNumber().contains(queryNum)) {
                    return true;
                }
            }
            return false;
        });
    }

    public List<Human> getContainsPhone(String queryNum) {
        return execute(() -> {
            List<Human> humen = new ArrayList<>();
            for (int idx = 0; idx < users.size(); idx++) {
                User user = users.valueAt(idx);
                if (user.getPhoneNumber().contains(queryNum)) {
                    humen.add(user.getRaw());
                }
            }
            return humen;
        });
    }

    public boolean updateRank(long userId, long rankId) {
        return execute(() -> {
            if (hasUser(userId)) {
                users.get(userId).getRaw().setRankId(rankId);
                return true;
            }
            return false;
        });
    }

    public boolean updateAbsence(long userId, Absence absence) {
        return execute(() -> {
            if (hasUser(userId)) {
                users.get(userId).getRaw().setAbsence(absence);
                return true;
            }
            return false;
        });
    }

    public User getUser(long myId) {
        return execute(() -> users.get(myId));
    }

    public List<User> getUsers() {
        return execute(() -> {
            List<User> temp = new ArrayList<>();
            int size = users.size();
            for (int idx = 0; idx < size; idx++) {
                temp.add(users.valueAt(idx));
            }
            return temp;
        });
    }


    public void clear() {
        execute(() -> {
            users.clear();
            return true;
        });
    }

}

package com.tosslab.jandi.app.lists.entities.entitymanager;

import android.content.Context;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Steve SeongUg Jung on 15. 6. 30..
 */
class EntityManagerLockProxy extends EntityManager {

    private Lock lock;

    protected EntityManagerLockProxy() {
        super();
        lock = new ReentrantLock();

    }

    @Override
    public void refreshEntity() {
        lock.lock();
        try {
            super.refreshEntity();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void refreshEntity(ResLeftSideMenu resLeftSideMenu) {
        lock.lock();
        try {
            super.refreshEntity(resLeftSideMenu);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<FormattedEntity> getJoinedChannels() {
        lock.lock();
        try {
            return super.getJoinedChannels();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<FormattedEntity> getUnjoinedChannels() {
        lock.lock();
        try {
            return super.getUnjoinedChannels();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<FormattedEntity> getGroups() {
        lock.lock();
        try {
            return super.getGroups();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<FormattedEntity> getFormattedUsers() {
        lock.lock();
        try {
            return super.getFormattedUsers();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<FormattedEntity> getFormattedUsersWithoutMe() {
        lock.lock();
        try {
            return super.getFormattedUsersWithoutMe();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<FormattedEntity> getCategorizableEntities() {
        lock.lock();
        try {
            return super.getCategorizableEntities();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public FormattedEntity getMe() {
        lock.lock();
        try {
            return super.getMe();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String getDistictId() {
        lock.lock();
        try {
            return super.getDistictId();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String getTeamName() {
        lock.lock();
        try {
            return super.getTeamName();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int getDefaultTopicId() {
        lock.lock();
        try {
            return super.getDefaultTopicId();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int getTeamId() {
        lock.lock();
        try {
            return super.getTeamId();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<FormattedEntity> retrieveGivenEntities(List<Integer> givenEntityIds) {
        lock.lock();
        try {
            return super.retrieveGivenEntities(givenEntityIds);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<FormattedEntity> retrieveExclusivedEntities(List<Integer> givenEntityIds) {
        lock.lock();
        try {
            return super.retrieveExclusivedEntities(givenEntityIds);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<FormattedEntity> retrieveAccessableEntities() {
        lock.lock();
        try {
            return super.retrieveAccessableEntities();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public FormattedEntity getEntityById(int entityId) {
        lock.lock();
        try {
            return super.getEntityById(entityId);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String getEntityNameById(int entityId) {
        lock.lock();
        try {
            return super.getEntityNameById(entityId);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<FormattedEntity> getUnjoinedMembersOfEntity(int entityId, int entityType) {
        lock.lock();
        try {
            return super.getUnjoinedMembersOfEntity(entityId, entityType);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean isMyTopic(int entityId) {
        lock.lock();
        try {
            return super.isMyTopic(entityId);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean isMe(int userId) {
        lock.lock();
        try {
            return super.isMe(userId);
        } finally {
            lock.unlock();
        }
    }
}

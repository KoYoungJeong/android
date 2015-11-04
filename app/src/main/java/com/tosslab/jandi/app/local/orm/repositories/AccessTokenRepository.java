package com.tosslab.jandi.app.local.orm.repositories;

import android.text.TextUtils;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.OrmDatabaseHelper;
import com.tosslab.jandi.app.network.models.ResAccessToken;

import java.sql.SQLException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Steve SeongUg Jung on 15. 7. 23..
 */
public class AccessTokenRepository {

    private static AccessTokenRepository repository;
    private final OrmDatabaseHelper helper;
    private final Lock lock;

    private AccessTokenRepository() {
        helper = OpenHelperManager.getHelper(JandiApplication.getContext(), OrmDatabaseHelper.class);
        lock = new ReentrantLock();
    }

    public static AccessTokenRepository getRepository() {
        if (repository == null) {
            repository = new AccessTokenRepository();
        }
        return repository;
    }

    /**
     * It's for Only TestCode.
     */
    public static void release() {
        repository = null;
    }

    public boolean upsertAccessToken(ResAccessToken newResAccessToken) {
        lock.lock();
        try {

            Dao<ResAccessToken, ?> dao = helper.getDao(ResAccessToken.class);
            ResAccessToken resAccessToken = dao.queryBuilder().queryForFirst();

            if (resAccessToken == null) {
                dao.create(newResAccessToken);
            } else {
                String accessToken = newResAccessToken.getAccessToken();
                String refreshToken = newResAccessToken.getRefreshToken();
                String tokenType = newResAccessToken.getTokenType();
                String expireTime = newResAccessToken.getExpireTime();

                UpdateBuilder<ResAccessToken, ?> updateBuilder = dao.updateBuilder();
                if (!TextUtils.isEmpty(accessToken)) {
                    updateBuilder.updateColumnValue("accessToken", accessToken);
                }
                if (!TextUtils.isEmpty(refreshToken)) {
                    updateBuilder.updateColumnValue("refreshToken", refreshToken);
                }
                if (!TextUtils.isEmpty(tokenType)) {
                    updateBuilder.updateColumnValue("tokenType", tokenType);
                }
                if (!TextUtils.isEmpty(expireTime)) {
                    updateBuilder.updateColumnValue("expireTime", expireTime);
                }

                updateBuilder.where()
                        .eq("_id", resAccessToken.get_id());
                updateBuilder.update();

            }

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

        return false;
    }

    public ResAccessToken getAccessToken() {
        lock.lock();
        ResAccessToken resAccessToken = null;
        try {
            Dao<ResAccessToken, ?> dao = helper.getDao(ResAccessToken.class);
            resAccessToken = dao.queryBuilder().queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

        if (resAccessToken != null) {
            return resAccessToken;
        } else {
            resAccessToken = new ResAccessToken();
            resAccessToken.setAccessToken("");
            resAccessToken.setRefreshToken("");
            resAccessToken.setTokenType("");
            resAccessToken.setExpireTime("");
            return resAccessToken;
        }
    }

    public boolean deleteAccessToken() {
        lock.lock();
        try {
            Dao<ResAccessToken, ?> dao = helper.getDao(ResAccessToken.class);
            dao.deleteBuilder()
                    .delete();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            lock.lock();
        }

        return false;
    }
}

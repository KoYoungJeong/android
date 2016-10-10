package com.tosslab.jandi.app.local.orm.repositories;

import android.text.TextUtils;

import com.j256.ormlite.dao.Dao;
import com.tosslab.jandi.app.local.orm.repositories.template.LockExecutorTemplate;
import com.tosslab.jandi.app.network.models.PushToken;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PushTokenRepository extends LockExecutorTemplate {

    private static PushTokenRepository pushTokenRepository;

    public static PushTokenRepository getInstance() {
        if (pushTokenRepository == null) {
            pushTokenRepository = new PushTokenRepository();
        }
        return pushTokenRepository;
    }

    public boolean upsertPushToken(PushToken pushToken) {
        return execute(() -> {
            try {
                String service = pushToken.getService();
                if (!TextUtils.equals("gcm", service)
                        && !TextUtils.equals("baidu", service)) {
                    return false;
                }
                Dao<PushToken, ?> dao = getHelper().getDao(PushToken.class);
                dao.createOrUpdate(pushToken);
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    public List<PushToken> getPushTokenList() {
        return execute(() -> {

            try {
                Dao<PushToken, ?> dao = getHelper().getDao(PushToken.class);
                return dao.queryForAll();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return new ArrayList<PushToken>();
        });
    }

    public boolean hasGcmPushToken() {
        return execute(() -> {
            try {
                Dao<PushToken, Object> dao = getDao(PushToken.class);
                return dao.queryBuilder()
                        .where()
                        .eq("service","gcm")
                        .countOf() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return false;
        });
    }

    public boolean deletePushToken() {
        return execute(() -> {
            try {
                getHelper().getDao(PushToken.class).deleteBuilder().delete();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    public boolean deleteGcmToken() {
        return execute(() -> {
            try {
                Dao<PushToken, String> dao = getDao(PushToken.class);
                dao.deleteById("gcm");
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });
    }
}

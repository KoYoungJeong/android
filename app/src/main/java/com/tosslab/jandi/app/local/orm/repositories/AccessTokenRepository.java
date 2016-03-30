package com.tosslab.jandi.app.local.orm.repositories;

import android.text.TextUtils;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.tosslab.jandi.app.local.orm.repositories.template.LockExecutorTemplate;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.sql.SQLException;

/**
 * Created by Steve SeongUg Jung on 15. 7. 23..
 */
public class AccessTokenRepository extends LockExecutorTemplate {

    public static AccessTokenRepository getRepository() {
        return new AccessTokenRepository();
    }

    public boolean upsertAccessToken(ResAccessToken newResAccessToken) {
        return execute(() -> {
            if (newResAccessToken == null) {
                return false;
            }

            try {

                Dao<ResAccessToken, ?> dao = getHelper().getDao(ResAccessToken.class);
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
            }

            return false;

        });
    }

    public ResAccessToken getAccessToken() {
        return execute(() -> {
            ResAccessToken resAccessToken = null;
            try {
                Dao<ResAccessToken, ?> dao = getHelper().getDao(ResAccessToken.class);
                resAccessToken = dao.queryBuilder().queryForFirst();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            LogUtil.i(resAccessToken != null ? resAccessToken.toString() : "Access token null !!");

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

        });
    }

    public boolean deleteAccessToken() {
        return execute(() -> {
            try {
                Dao<ResAccessToken, ?> dao = getHelper().getDao(ResAccessToken.class);
                dao.deleteBuilder()
                        .delete();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return false;

        });
    }
}

package com.tosslab.jandi.app.local.orm.repositories.search;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.tosslab.jandi.app.local.orm.domain.MemberRecentKeyword;
import com.tosslab.jandi.app.local.orm.repositories.template.LockExecutorTemplate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class MemberRecentKeywordRepository extends LockExecutorTemplate {

    private static MemberRecentKeywordRepository instance;

    synchronized public static MemberRecentKeywordRepository getInstance() {
        if (instance == null) {
            instance = new MemberRecentKeywordRepository();
        }
        return instance;
    }

    public boolean upsertKeyword(String keyword) {
        return execute(() -> {

            try {
                Dao<MemberRecentKeyword, Object> dao = getDao(MemberRecentKeyword.class);
                DeleteBuilder<MemberRecentKeyword, Object> deleteBuilder = dao.deleteBuilder();
                deleteBuilder.where()
                        .eq("keyword", keyword);
                deleteBuilder.delete();

                return dao.create(new MemberRecentKeyword(keyword)) > 0;

            } catch (SQLException e) {
                e.printStackTrace();
            }

            return false;
        });
    }

    public List<MemberRecentKeyword> getKeywords() {
        return execute(() -> {
            try {
                Dao<MemberRecentKeyword, Object> dao = getDao(MemberRecentKeyword.class);
                return dao.queryBuilder().orderBy("_id", false).query();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return new ArrayList<MemberRecentKeyword>();
        });
    }

    public boolean removeAll() {
        return execute(() -> {
            try {
                Dao<MemberRecentKeyword, Object> dao = getDao(MemberRecentKeyword.class);
                dao.deleteBuilder().delete();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return false;
        });
    }

    public boolean remove(long id) {
        return execute(() -> {
            try {
                Dao<MemberRecentKeyword, Object> dao = getDao(MemberRecentKeyword.class);
                DeleteBuilder<MemberRecentKeyword, Object> deleteBuilder = dao.deleteBuilder();
                deleteBuilder.where()
                        .eq("_id", id);
                return deleteBuilder.delete() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return false;

        });
    }
}

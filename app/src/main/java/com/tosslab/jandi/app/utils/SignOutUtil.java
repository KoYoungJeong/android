package com.tosslab.jandi.app.utils;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.OrmDatabaseHelper;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.utils.parse.PushUtil;

public class SignOutUtil {
    public static void removeSignData() {
        JandiPreference.signOut(JandiApplication.getContext());
        BadgeUtils.clearBadge(JandiApplication.getContext());
        PushUtil.unsubscribeParsePush();
        OpenHelperManager.getHelper(JandiApplication.getContext(), OrmDatabaseHelper.class)
                .clearAllData();
        TeamInfoLoader instance = TeamInfoLoader.getInstance();
        instance = null;
        TokenUtil.clearTokenInfo();

    }

    public static void initSignData() {
        PushUtil.unsubscribeParsePush();
        BadgeUtils.clearBadge(JandiApplication.getContext());
        OpenHelperManager.getHelper(JandiApplication.getContext(), OrmDatabaseHelper.class)
                .clearAllData();
        TokenUtil.clearTokenInfo();
    }

}

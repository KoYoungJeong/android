package com.tosslab.jandi.app.utils;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.OrmDatabaseHelper;
import com.tosslab.jandi.app.utils.parse.PushUtil;

public class SignOutUtil {
    public static void removeSignData() {
        JandiPreference.signOut(JandiApplication.getContext());
        PushUtil.unsubscribeParsePush();
        OpenHelperManager.getHelper(JandiApplication.getContext(), OrmDatabaseHelper.class)
                .clearAllData();
        TokenUtil.clearTokenInfo();

    }

    public static void initSignData() {
        PushUtil.unsubscribeParsePush();
        OpenHelperManager.getHelper(JandiApplication.getContext(), OrmDatabaseHelper.class)
                .clearAllData();
        TokenUtil.clearTokenInfo();
    }
}

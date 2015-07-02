package org.robolectric;

import android.content.Context;

import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ResAccountInfo;

import org.robolectric.shadows.ShadowLog;

/**
 * Created by Steve SeongUg Jung on 14. 12. 19..
 */
public class BaseInitUtil {

    public static final String TEST_ID = "steve@tosslab.com";
    public static final String TEST_PASSWORD = "dnrl~12AB";

    public static void initData(Context context) {

        httpOn();
        logOn();

        ResAccountInfo accountInfo = RequestApiManager.getInstance().getAccountInfoByMainRest();
        JandiAccountDatabaseManager.getInstance(context).upsertAccountDevices(accountInfo.getDevices());
        JandiAccountDatabaseManager.getInstance(context).upsertAccountTeams(accountInfo.getMemberships());
        JandiAccountDatabaseManager.getInstance(context).upsertAccountEmail(accountInfo.getEmails());
        JandiAccountDatabaseManager.getInstance(context).upsertAccountInfo(accountInfo);
    }

    public static void logOn() {
        System.setProperty("robolectric.logging", "stdout");
        ShadowLog.stream = System.out;
    }

    public static void httpOn() {
        Robolectric.getFakeHttpLayer().interceptHttpRequests(false);
    }


}

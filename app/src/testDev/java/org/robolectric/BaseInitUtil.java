package org.robolectric;

import android.content.Context;

import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.client.JandiRestClient;
import com.tosslab.jandi.app.network.client.JandiRestClient_;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.utils.TokenUtil;

import org.robolectric.shadows.ShadowLog;

/**
 * Created by Steve SeongUg Jung on 14. 12. 19..
 */
public class BaseInitUtil {

    public static final String TEST_ID = "steve@tosslab.com";
    public static final String TEST_PASSWORD = "dnrl~12AB";

    public static void initData(Context context) {

        Robolectric.getFakeHttpLayer().interceptHttpRequests(false);

        JandiRestClient jandiRestClient = new JandiRestClient_(context);
        ResAccessToken accessToken = jandiRestClient.getAccessToken(ReqAccessToken.createPasswordReqToken(TEST_ID, TEST_PASSWORD));
        TokenUtil.saveTokenInfoByPassword(context, accessToken);

        jandiRestClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
        ResAccountInfo accountInfo = jandiRestClient.getAccountInfo();
        JandiAccountDatabaseManager.getInstance(context).upsertAccountDevices(accountInfo.getDevices());
        JandiAccountDatabaseManager.getInstance(context).upsertAccountTeams(accountInfo.getMemberships());
        JandiAccountDatabaseManager.getInstance(context).upsertAccountEmail(accountInfo.getEmails());
        JandiAccountDatabaseManager.getInstance(context).upsertAccountInfo(accountInfo);


        System.setProperty("robolectric.logging", "stdout");
        ShadowLog.stream = System.out;
    }
}

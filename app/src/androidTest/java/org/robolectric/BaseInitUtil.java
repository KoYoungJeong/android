package org.robolectric;

import android.content.Context;

import com.tosslab.jandi.app.local.database.JandiDatabaseManager;
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

    public static final String TEST_ID = "mk@tosslab.com";
    public static final String TEXT_PASSWORD = "1234";

    public static void initData(Context context) {

        JandiRestClient jandiRestClient = new JandiRestClient_(context);
        ResAccessToken accessToken = jandiRestClient.getAccessToken(ReqAccessToken.createPasswordReqToken(TEST_ID, TEXT_PASSWORD));
        TokenUtil.saveTokenInfoByPassword(context, accessToken);

        jandiRestClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
        ResAccountInfo accountInfo = jandiRestClient.getAccountInfo();
        JandiDatabaseManager.getInstance(context).upsertAccountDevices(accountInfo.getDevices());
        JandiDatabaseManager.getInstance(context).upsertAccountTeams(accountInfo.getMemberships());
        JandiDatabaseManager.getInstance(context).upsertAccountEmail(accountInfo.getEmails());
        JandiDatabaseManager.getInstance(context).upsertAccountInfo(accountInfo);

        Robolectric.getFakeHttpLayer().interceptHttpRequests(false);
        System.setProperty("robolectric.logging", "stdout");
        ShadowLog.stream = System.out;
    }
}

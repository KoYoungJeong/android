package org.robolectric;

import android.content.Context;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
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

        httpOn();
        logOn();

        ResAccessToken accessToken = RequestApiManager.getInstance().getAccessTokenByMainRest(
                ReqAccessToken.createPasswordReqToken(TEST_ID, TEST_PASSWORD));

        TokenUtil.saveTokenInfoByPassword(accessToken);

        ResAccountInfo accountInfo = RequestApiManager.getInstance().getAccountInfoByMainRest();
        AccountRepository.getRepository().upsertAccountAllInfo(accountInfo);
    }

    public static void logOn() {
        System.setProperty("robolectric.logging", "stdout");
        ShadowLog.stream = System.out;
    }

    public static void httpOn() {
        Robolectric.getFakeHttpLayer().interceptHttpRequests(false);
    }


}

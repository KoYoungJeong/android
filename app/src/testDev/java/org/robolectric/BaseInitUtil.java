package org.robolectric;

import android.content.Context;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.DaoManager;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.AnnouncementRepository;
import com.tosslab.jandi.app.local.orm.repositories.ChatRepository;
import com.tosslab.jandi.app.local.orm.repositories.FileDetailRepository;
import com.tosslab.jandi.app.local.orm.repositories.LeftSideMenuRepository;
import com.tosslab.jandi.app.local.orm.repositories.MarkerRepository;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.local.orm.repositories.ReadyMessageRepository;
import com.tosslab.jandi.app.local.orm.repositories.SendMessageRepository;
import com.tosslab.jandi.app.local.orm.repositories.StickerRepository;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.utils.TokenUtil;

import org.robolectric.shadows.ShadowLog;
import org.robolectric.shadows.httpclient.FakeHttp;

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
        int teamId = accountInfo.getMemberships().iterator().next().getTeamId();
        AccountRepository.getRepository().upsertAccountAllInfo(accountInfo);
        AccountRepository.getRepository().updateSelectedTeamInfo(teamId);
    }

    public static void logOn() {
        System.setProperty("robolectric.logging", "stdout");
        ShadowLog.stream = System.out;
    }

    public static void httpOn() {
        FakeHttp.getFakeHttpLayer().interceptHttpRequests(false);
    }


    public static void releaseDatabase() {
        AccountRepository.release();
        ChatRepository.release();
        FileDetailRepository.release();
        LeftSideMenuRepository.release();
        MessageRepository.release();
        MarkerRepository.release();
        StickerRepository.release();
        SendMessageRepository.release();
        ReadyMessageRepository.release();
        AnnouncementRepository.release();
        DaoManager.clearCache();
        for (int idx = 0; idx < 10; ++idx) {
            OpenHelperManager.releaseHelper();
        }

    }
}

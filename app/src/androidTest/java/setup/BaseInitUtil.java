package setup;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.DaoManager;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.OrmDatabaseHelper;
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
import com.tosslab.jandi.app.local.orm.repositories.TopicFolderRepository;
import com.tosslab.jandi.app.network.client.account.AccountApi;
import com.tosslab.jandi.app.network.client.main.LeftSideApi;
import com.tosslab.jandi.app.network.client.main.LoginApi;
import com.tosslab.jandi.app.network.client.publictopic.ChannelApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ReqCreateTopic;
import com.tosslab.jandi.app.network.models.ReqDeleteTopic;
import com.tosslab.jandi.app.network.models.ReqInviteTopicUsers;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.jayway.awaitility.Awaitility.await;


// email : androidtester1@gustr.com / androidtester2@gustr.com / androidtester3@gustr.com
public class BaseInitUtil {

    public static final int STATE_TEMP_TOPIC_NOT_CREATED = 0;
    public static final int STATE_TEMP_TOPIC_CREATED = 1;


    public static long tempTopicId = -1;
    public static int topicState = STATE_TEMP_TOPIC_NOT_CREATED;

    public static String TEST1_EMAIL = "ekuvekez-9240@yopmail.com";
    public static String TEST2_EMAIL = "xutycaji-3985@yopmail.com";
    public static String TEST3_EMAIL = "issytovix-5024@yopmail.com";

    public static String TEST_EMAIL = TEST1_EMAIL;
    public static String TEST_PASSWORD = "1234asdf";

    private static Context ORIGIN_CONTEXT;

    public static void initData() {
        if (ORIGIN_CONTEXT != null) {
            restoreContext();
        }
        turnOnWifi();
        userSignin();
    }

    public static void turnOnWifi() {
        WifiManager wifiManager = (WifiManager) JandiApplication.getContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
            await().until(wifiManager::isWifiEnabled);
        }
    }

    public static void clear() {
        OpenHelperManager.getHelper(JandiApplication.getContext(), OrmDatabaseHelper.class).clearAllData();
        JandiPreference.signOut(JandiApplication.getContext());
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
        TopicFolderRepository.release();
        DaoManager.clearCache();
        for (int idx = 0; idx < 10; ++idx) {
            OpenHelperManager.releaseHelper();
        }
    }


    public static long getUserIdByEmail(String email) {
        ResAccessToken accessToken = null;
        try {
            accessToken = new LoginApi().getAccessToken(
                    ReqAccessToken.createPasswordReqToken(email, TEST_PASSWORD));

            TokenUtil.saveTokenInfoByPassword(accessToken);
            ResAccountInfo accountInfo = new AccountApi().getAccountInfo();
            long result = accountInfo.getMemberships().iterator().next().getMemberId();
            accessToken = new LoginApi().getAccessToken(
                    ReqAccessToken.createPasswordReqToken(TEST1_EMAIL, TEST_PASSWORD));
            TokenUtil.saveTokenInfoByPassword(accessToken);
            return result;
        } catch (RetrofitException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static String getUserNameByEmail(String email) throws RetrofitException {
        ResAccessToken accessToken = new LoginApi().getAccessToken(
                ReqAccessToken.createPasswordReqToken(email, TEST_PASSWORD));
        TokenUtil.saveTokenInfoByPassword(accessToken);
        ResAccountInfo accountInfo = new AccountApi().getAccountInfo();
        String result = accountInfo.getMemberships().iterator().next().getName();
        accessToken = new LoginApi().getAccessToken(
                ReqAccessToken.createPasswordReqToken(TEST1_EMAIL, TEST_PASSWORD));
        TokenUtil.saveTokenInfoByPassword(accessToken);
        return result;
    }

    public static void userSignin(String testId) {

        clear();
        if (TextUtils.isEmpty(testId)) {
            testId = TEST1_EMAIL;
        }

        String testPasswd = TEST_PASSWORD;

        clear();

        try {
            ResAccessToken accessToken = null;
            accessToken = new LoginApi().getAccessToken(
                    ReqAccessToken.createPasswordReqToken(testId, testPasswd));
            TokenUtil.saveTokenInfoByPassword(accessToken);

            ResAccountInfo accountInfo = new AccountApi().getAccountInfo();
            long teamId = accountInfo.getMemberships().iterator().next().getTeamId();
            AccountRepository.getRepository().upsertAccountAllInfo(accountInfo);
            AccountRepository.getRepository().updateSelectedTeamInfo(teamId);

            ResLeftSideMenu leftSideMenu = new LeftSideApi().getInfosForSideMenu(AccountRepository.getRepository().getSelectedTeamId());
            LeftSideMenuRepository.getRepository().upsertLeftSideMenu(leftSideMenu);
        } catch (RetrofitException e) {
            e.printStackTrace();
        }


    }

    public static void userSignin() {
        userSignin("");
    }

    public static void createDummyTopic() {
        if (topicState == STATE_TEMP_TOPIC_NOT_CREATED) {
            try {
                userSignin();
                ResAccountInfo accountInfo = new AccountApi().getAccountInfo();
                long teamId = accountInfo.getMemberships().iterator().next().getTeamId();
                ReqCreateTopic topic = new ReqCreateTopic();
                topic.teamId = teamId;
                topic.name = "테스트 토픽 : " + new Date();
                topic.description = "테스트 토픽 입니다.";
                ResCommon resCommon = null;
                resCommon = new ChannelApi().createChannel(teamId, topic);
                tempTopicId = resCommon.id;
                topicState = STATE_TEMP_TOPIC_CREATED;
            } catch (RetrofitException e) {
                e.printStackTrace();
            }
        }
        refreshLeftSideMenu();
    }

    public static void inviteDummyMembers() {
        try {
            ResAccountInfo accountInfo = new AccountApi().getAccountInfo();
            long teamId = accountInfo.getMemberships().iterator().next().getTeamId();
            long tester2Id = getUserIdByEmail(TEST2_EMAIL);
            long tester3Id = getUserIdByEmail(TEST3_EMAIL);
            List<Long> members = new ArrayList<>();
            members.add(tester2Id);
            members.add(tester3Id);
            ReqInviteTopicUsers reqInviteTopicUsers = new ReqInviteTopicUsers(members, teamId);
            new ChannelApi().invitePublicTopic(tempTopicId, reqInviteTopicUsers);
            refreshLeftSideMenu();
        } catch (RetrofitException e) {
            e.printStackTrace();
        }

    }

    public static void deleteDummyTopic() {
        ResAccountInfo accountInfo = null;
        try {
            accountInfo = new AccountApi().getAccountInfo();
            long teamId = accountInfo.getMemberships().iterator().next().getTeamId();
            if (topicState == STATE_TEMP_TOPIC_CREATED) {
                new ChannelApi().deleteTopic(tempTopicId, new ReqDeleteTopic(teamId));
                topicState = STATE_TEMP_TOPIC_NOT_CREATED;
            }
            refreshLeftSideMenu();
        } catch (RetrofitException retrofitError) {
            retrofitError.printStackTrace();
        }
    }

    public static void refreshLeftSideMenu() {
        try {
            ResLeftSideMenu leftSideMenu = new LeftSideApi().getInfosForSideMenu(AccountRepository.getRepository().getSelectedTeamId());
            LeftSideMenuRepository.getRepository().upsertLeftSideMenu(leftSideMenu);
            EntityManager.getInstance().refreshEntity();
        } catch (RetrofitException e) {
            e.printStackTrace();
        }
    }

    public static void disconnectWifi() {
        ORIGIN_CONTEXT = JandiApplication.getContext();
        LogUtil.d("disconnectWifi : " + ORIGIN_CONTEXT.toString());
        JandiApplication mock = Mockito.mock(JandiApplication.class);
        JandiApplication.setContext(mock);
        ConnectivityManager mockConnectManager = Mockito.mock(ConnectivityManager.class);
        Mockito.when(mock.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(mockConnectManager);
        Mockito.when(mockConnectManager.getActiveNetworkInfo()).thenReturn(null);
    }


    public static void restoreContext() {
        LogUtil.d("restoreContext : " + ORIGIN_CONTEXT.toString());
        JandiApplication.setContext(ORIGIN_CONTEXT);
    }
}

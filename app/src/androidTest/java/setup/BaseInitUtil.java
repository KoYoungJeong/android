package setup;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.InitialInfoRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.RankRepository;
import com.tosslab.jandi.app.network.client.account.AccountApi;
import com.tosslab.jandi.app.network.client.main.LoginApi;
import com.tosslab.jandi.app.network.client.publictopic.ChannelApi;
import com.tosslab.jandi.app.network.client.start.StartApi;
import com.tosslab.jandi.app.network.client.teams.TeamApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ReqCreateTopic;
import com.tosslab.jandi.app.network.models.ReqDeleteTopic;
import com.tosslab.jandi.app.network.models.ReqInviteTopicUsers;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.start.RawInitialInfo;
import com.tosslab.jandi.app.network.models.start.Topic;
import com.tosslab.jandi.app.network.models.team.rank.Ranks;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.SignOutUtil;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import rx.Observable;

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
        ConnectivityManager connectivityManager = (ConnectivityManager) JandiApplication.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
            await().until(() -> wifiManager.isWifiEnabled() || connectivityManager.getActiveNetworkInfo().isConnected());
        }
    }

    public static void clear() {
        SignOutUtil.removeSignData();
    }

    public static long getUserIdByEmail(String email) {
        ResAccessToken accessToken = null;
        try {
            accessToken = new LoginApi(RetrofitBuilder.getInstance()).getAccessToken(
                    ReqAccessToken.createPasswordReqToken(email, TEST_PASSWORD));

            TokenUtil.saveTokenInfoByPassword(accessToken);
            ResAccountInfo accountInfo = new AccountApi(RetrofitBuilder.getInstance()).getAccountInfo();
            long result = accountInfo.getMemberships().iterator().next().getMemberId();
            accessToken = new LoginApi(RetrofitBuilder.getInstance()).getAccessToken(
                    ReqAccessToken.createPasswordReqToken(TEST1_EMAIL, TEST_PASSWORD));
            TokenUtil.saveTokenInfoByPassword(accessToken);
            return result;
        } catch (RetrofitException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static String getUserNameByEmail(String email) throws RetrofitException {
        ResAccessToken accessToken = new LoginApi(RetrofitBuilder.getInstance()).getAccessToken(
                ReqAccessToken.createPasswordReqToken(email, TEST_PASSWORD));
        TokenUtil.saveTokenInfoByPassword(accessToken);
        ResAccountInfo accountInfo = new AccountApi(RetrofitBuilder.getInstance()).getAccountInfo();
        String result = accountInfo.getMemberships().iterator().next().getName();
        accessToken = new LoginApi(RetrofitBuilder.getInstance()).getAccessToken(
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

        try {
            ResAccessToken accessToken = null;
            accessToken = new LoginApi(RetrofitBuilder.getInstance()).getAccessToken(
                    ReqAccessToken.createPasswordReqToken(testId, testPasswd));
            TokenUtil.saveTokenInfoByPassword(accessToken);

            ResAccountInfo accountInfo = new AccountApi(RetrofitBuilder.getInstance()).getAccountInfo();
            long teamId = Observable.from(accountInfo.getMemberships())
                    .takeFirst(userTeam -> TextUtils.equals(userTeam.getName(), "android-test-code"))
                    .toBlocking().first().getTeamId();
            AccountUtil.removeDuplicatedTeams(accountInfo);
            AccountRepository.getRepository().upsertAccountAllInfo(accountInfo);
            AccountRepository.getRepository().updateSelectedTeamInfo(teamId);

            refreshTeamInfo();

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
                ResAccountInfo accountInfo = new AccountApi(RetrofitBuilder.getInstance()).getAccountInfo();
                long teamId = accountInfo.getMemberships().iterator().next().getTeamId();
                ReqCreateTopic topic = new ReqCreateTopic();
                topic.teamId = teamId;
                topic.name = "테스트 토픽 : " + new Date();
                topic.description = "테스트 토픽 입니다.";
                Topic resCommon = new ChannelApi(RetrofitBuilder.getInstance()).createChannel(teamId, topic);
                tempTopicId = resCommon.getId();
                topicState = STATE_TEMP_TOPIC_CREATED;
            } catch (RetrofitException e) {
                e.printStackTrace();
            }
        }
        refreshTeamInfo();
    }

    public static void inviteDummyMembers() {
        try {
            ResAccountInfo accountInfo = new AccountApi(RetrofitBuilder.getInstance()).getAccountInfo();
            long teamId = accountInfo.getMemberships().iterator().next().getTeamId();
            long tester2Id = getUserIdByEmail(TEST2_EMAIL);
            long tester3Id = getUserIdByEmail(TEST3_EMAIL);
            List<Long> members = new ArrayList<>();
            members.add(tester2Id);
            members.add(tester3Id);
            ReqInviteTopicUsers reqInviteTopicUsers = new ReqInviteTopicUsers(members, teamId);
            new ChannelApi(RetrofitBuilder.getInstance()).invitePublicTopic(tempTopicId, reqInviteTopicUsers);
            refreshTeamInfo();
        } catch (RetrofitException e) {
            e.printStackTrace();
        }

    }

    public static void deleteDummyTopic() {
        ResAccountInfo accountInfo = null;
        try {
            accountInfo = new AccountApi(RetrofitBuilder.getInstance()).getAccountInfo();
            long teamId = accountInfo.getMemberships().iterator().next().getTeamId();
            if (topicState == STATE_TEMP_TOPIC_CREATED) {
                new ChannelApi(RetrofitBuilder.getInstance()).deleteTopic(tempTopicId, new ReqDeleteTopic(teamId));
                topicState = STATE_TEMP_TOPIC_NOT_CREATED;
            }
            refreshTeamInfo();
        } catch (RetrofitException retrofitError) {
            retrofitError.printStackTrace();
        }
    }

    public static void refreshTeamInfo() {
        try {
            long teamId = AccountRepository.getRepository().getSelectedTeamId();
            String initialInfo = new StartApi(RetrofitBuilder.getInstance()).getRawInitializeInfo(teamId);
            InitialInfoRepository.getInstance().upsertRawInitialInfo(new RawInitialInfo(teamId, initialInfo));
            Ranks ranks = new TeamApi(RetrofitBuilder.getInstance()).getRanks(teamId);
            RankRepository.getInstance().addRanks(ranks.getRanks());
            TeamInfoLoader.getInstance().refresh();
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

    public static void releaseDatabase() {
        OpenHelperManager.releaseHelper();
    }
}

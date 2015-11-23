package setup;

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
import com.tosslab.jandi.app.network.manager.RequestApiManager;
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

import java.util.ArrayList;
import java.util.List;

import retrofit.RetrofitError;


// email : androidtester1@gustr.com / androidtester2@gustr.com / androidtester3@gustr.com
public class BaseInitUtil {

    public static final int STATE_TEMP_TOPIC_NOT_CREATED = 0;
    public static final int STATE_TEMP_TOPIC_CREATED = 1;


    public static int tempTopicId = -1;
    public static int topicState = STATE_TEMP_TOPIC_NOT_CREATED;

    public static String TEST_ID = "androidtester1@gustr.com";
    public static String TEST_PASSWORD = "asdf1234";

    public static String TEST1_ID = "androidtester1@gustr.com";
    public static String TEST2_ID = "androidtester2@gustr.com";
    public static String TEST3_ID = "androidtester3@gustr.com";

    public static void initData() {
        userSignin();
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

    public static int getUserIdByEmail(String email) {
        ResAccessToken accessToken = RequestApiManager.getInstance().getAccessTokenByMainRest(
                ReqAccessToken.createPasswordReqToken(email, "1234asdf"));
        TokenUtil.saveTokenInfoByPassword(accessToken);
        ResAccountInfo accountInfo = RequestApiManager.getInstance().getAccountInfoByMainRest();
        int result = accountInfo.getMemberships().iterator().next().getMemberId();
        accessToken = RequestApiManager.getInstance().getAccessTokenByMainRest(
                ReqAccessToken.createPasswordReqToken("androidtester1@gustr.com", "1234asdf"));
        TokenUtil.saveTokenInfoByPassword(accessToken);
        return result;
    }

    public static void userSignin(String testId) {
        if (TextUtils.isEmpty(testId)) {
            testId = "androidtester1@gustr.com";
        }

        String testPasswd = "1234asdf";

        clear();

        ResAccessToken accessToken = RequestApiManager.getInstance().getAccessTokenByMainRest(
                ReqAccessToken.createPasswordReqToken(testId, testPasswd));

        TokenUtil.saveTokenInfoByPassword(accessToken);

        ResAccountInfo accountInfo = RequestApiManager.getInstance().getAccountInfoByMainRest();
        int teamId = accountInfo.getMemberships().iterator().next().getTeamId();
        AccountRepository.getRepository().upsertAccountAllInfo(accountInfo);
        AccountRepository.getRepository().updateSelectedTeamInfo(teamId);

        ResLeftSideMenu leftSideMenu = RequestApiManager.getInstance().getInfosForSideMenuByMainRest(AccountRepository.getRepository().getSelectedTeamId());
        LeftSideMenuRepository.getRepository().upsertLeftSideMenu(leftSideMenu);
    }

    public static void userSignin() {
        userSignin("");
    }

    public static void createDummyTopic() {
        if (topicState == STATE_TEMP_TOPIC_NOT_CREATED) {
            userSignin();
            ResAccountInfo accountInfo = RequestApiManager.getInstance().getAccountInfoByMainRest();
            int teamId = accountInfo.getMemberships().iterator().next().getTeamId();
            ReqCreateTopic topic = new ReqCreateTopic();
            topic.teamId = teamId;
            topic.name = "테스트 토픽";
            topic.description = "테스트 토픽 입니다.";
            ResCommon resCommon = null;
            try {
                resCommon = RequestApiManager.getInstance().createChannelByChannelApi(topic);
            } catch (RetrofitError e) {
                e.printStackTrace();
            }
            tempTopicId = resCommon.id;
            topicState = STATE_TEMP_TOPIC_CREATED;
        }
        refreshLeftSideMenu();
    }

    public static void inviteDummyMembers() {
        ResAccountInfo accountInfo = RequestApiManager.getInstance().getAccountInfoByMainRest();
        int teamId = accountInfo.getMemberships().iterator().next().getTeamId();
        int tester2Id = getUserIdByEmail(TEST2_ID);
        int tester3Id = getUserIdByEmail(TEST3_ID);
        List<Integer> members = new ArrayList<>();
        members.add(tester2Id);
        members.add(tester3Id);
        ReqInviteTopicUsers reqInviteTopicUsers = new ReqInviteTopicUsers(members, teamId);
        RequestApiManager.getInstance().invitePublicTopicByChannelApi(tempTopicId, reqInviteTopicUsers);
        refreshLeftSideMenu();

    }

    public static void deleteDummyTopic() {
        ResAccountInfo accountInfo = RequestApiManager.getInstance().getAccountInfoByMainRest();
        int teamId = accountInfo.getMemberships().iterator().next().getTeamId();
        if (topicState == STATE_TEMP_TOPIC_CREATED) {
            RequestApiManager.getInstance().deleteTopicByChannelApi(tempTopicId, new ReqDeleteTopic(teamId));
            topicState = STATE_TEMP_TOPIC_NOT_CREATED;
        }
        refreshLeftSideMenu();
    }

    public static void refreshLeftSideMenu() {
        ResLeftSideMenu leftSideMenu = RequestApiManager.getInstance().getInfosForSideMenuByMainRest(AccountRepository.getRepository().getSelectedTeamId());
        LeftSideMenuRepository.getRepository().upsertLeftSideMenu(leftSideMenu);
        EntityManager.getInstance().refreshEntity();
    }

}

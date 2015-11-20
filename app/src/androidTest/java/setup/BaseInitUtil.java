package setup;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.DaoManager;
import com.tosslab.jandi.app.JandiApplication;
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
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.TokenUtil;

public class BaseInitUtil {
    public static final String TEST_ID = "steve@tosslab.com";
    public static final String TEST_PASSWORD = "dnrl~12AB";

    public static void initData() {

        clear();

        OpenHelperManager.getHelper(JandiApplication.getContext(), OrmDatabaseHelper.class)
                .clearAllData();
        JandiPreference.signOut(JandiApplication.getContext());

        ResAccessToken accessToken = RequestApiManager.getInstance().getAccessTokenByMainRest(
                ReqAccessToken.createPasswordReqToken(TEST_ID, TEST_PASSWORD));

        TokenUtil.saveTokenInfoByPassword(accessToken);

        ResAccountInfo accountInfo = RequestApiManager.getInstance().getAccountInfoByMainRest();
        int teamId = accountInfo.getMemberships().iterator().next().getTeamId();
        AccountRepository.getRepository().upsertAccountAllInfo(accountInfo);
        AccountRepository.getRepository().updateSelectedTeamInfo(teamId);

        initEntity();
    }

    public static void clear() {
        OpenHelperManager.getHelper(JandiApplication.getContext(), OrmDatabaseHelper.class).clearAllData();
        JandiPreference.signOut(JandiApplication.getContext());
    }


    public static void initEntity() {
        int selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
        ResLeftSideMenu sideMenu = RequestApiManager.getInstance().getInfosForSideMenuByMainRest(selectedTeamId);
        LeftSideMenuRepository.getRepository().upsertLeftSideMenu(sideMenu);


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
}

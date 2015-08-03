package com.tosslab.jandi.app.push;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.ChatRepository;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.push.model.JandiInterfaceModel;
import com.tosslab.jandi.app.ui.intro.IntroActivity_;
import com.tosslab.jandi.app.ui.maintab.MainTabActivity_;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;

/**
 * Created by Steve SeongUg Jung on 15. 1. 15..
 */
@EActivity(R.layout.activity_intro)
public class PushInterfaceActivity extends AppCompatActivity {

    public static final String EXTRA_USED = "used";
    // Push -> 선택된 엔티티 설정이 안됨에 따라...
    public static int selectedEntityId;
    @Extra(JandiConstants.EXTRA_ENTITY_ID)
    int entityId;
    @Extra(JandiConstants.EXTRA_ENTITY_TYPE)
    int entityType;
    @Extra(JandiConstants.EXTRA_IS_FROM_PUSH)
    boolean isFromPush;
    @Extra(JandiConstants.EXTRA_TEAM_ID)
    int teamId;
    @Bean
    JandiInterfaceModel jandiInterfaceModel;

    @AfterInject
    void initObject() {

        boolean used = (getIntent().getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0;

        if (!used) {
            selectedEntityId = entityId;
            checkTeamInfo();
        } else {
            selectedEntityId = -1;
            moveIntroActivity();
            return;
        }
    }

    @Background
    void checkTeamInfo() {

        if (!jandiInterfaceModel.hasTeamInfo(teamId)) {
            moveIntroActivity();
            return;
        }

        if (jandiInterfaceModel.setupSelectedTeam(teamId)) {

            String distictId = EntityManager.getInstance(PushInterfaceActivity.this).getDistictId();
            MixpanelMemberAnalyticsClient.getInstance(PushInterfaceActivity.this, distictId).trackMemberSingingIn();

            moveMessageListActivity();
        } else {
            moveIntroActivity();
        }

    }


    @UiThread
    void moveIntroActivity() {
        IntroActivity_.intent(PushInterfaceActivity.this)
                .flags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .start();
        finish();
    }

    @UiThread
    void moveMessageListActivity() {


        FormattedEntity entity = EntityManager.getInstance(PushInterfaceActivity.this).getEntityById(entityId);

        if (entity == null) {
            moveIntroActivity();
            return;
        }

        boolean isUser = entity.isUser();
        int roomId;
        if (!isUser) {
            roomId = entityId;
        } else {
            roomId = ChatRepository.getRepository().getChat(entityId).getEntityId();
        }

        MainTabActivity_.intent(PushInterfaceActivity.this)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .start();

        MessageListV2Activity_.intent(PushInterfaceActivity.this)
                .teamId(teamId)
                .roomId(roomId)
                .entityId(entityId)
                .entityType(entityType)
                .isFromPush(true)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .startForResult(MainTabActivity_.REQ_START_MESSAGE);

        finish();
    }
}

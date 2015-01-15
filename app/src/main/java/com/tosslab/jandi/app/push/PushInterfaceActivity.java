package com.tosslab.jandi.app.push;

import android.app.Activity;
import android.content.Intent;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.local.database.entity.JandiEntityDatabaseManager;
import com.tosslab.jandi.app.network.client.JandiEntityClient;
import com.tosslab.jandi.app.network.client.JandiEntityClient_;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.ui.intro.IntroActivity_;
import com.tosslab.jandi.app.ui.message.MessageListActivity_;
import com.tosslab.jandi.app.utils.JandiNetworkException;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;

/**
 * Created by Steve SeongUg Jung on 15. 1. 15..
 */
@EActivity(R.layout.activity_intro)
public class PushInterfaceActivity extends Activity {

    @Extra(JandiConstants.EXTRA_ENTITY_ID)
    int entityId;

    @Extra(JandiConstants.EXTRA_ENTITY_TYPE)
    int entityType;

    @Extra(JandiConstants.EXTRA_IS_FROM_PUSH)
    boolean isFromPush;

    @Extra(JandiConstants.EXTRA_TEAM_ID)
    int teamId;

    @AfterInject
    void initObject() {

        checkTeamInfo();

    }

    @Background
    void checkTeamInfo() {
        ResAccountInfo.UserTeam teamInfo = JandiAccountDatabaseManager.getInstance(PushInterfaceActivity.this).getTeamInfo(teamId);
        ResAccountInfo.UserTeam selectedTeamInfo = JandiAccountDatabaseManager.getInstance(PushInterfaceActivity.this).getSelectedTeamInfo();

        if (teamInfo != null) {
            if ((selectedTeamInfo == null || selectedTeamInfo.getTeamId() != teamId)) {

                JandiAccountDatabaseManager.getInstance(PushInterfaceActivity.this).updateSelectedTeam(teamId);
                JandiEntityClient jandiEntityClient = JandiEntityClient_.getInstance_(PushInterfaceActivity.this);

                try {
                    ResLeftSideMenu totalEntitiesInfo = jandiEntityClient.getTotalEntitiesInfo();
                    JandiEntityDatabaseManager.getInstance(PushInterfaceActivity.this).upsertLeftSideMenu(totalEntitiesInfo);
                    EntityManager.getInstance(PushInterfaceActivity.this).refreshEntity(PushInterfaceActivity.this);

                    moveMessageListActivity();

                } catch (JandiNetworkException e) {
                    moveIntroActivity();
                }

            } else {
                moveMessageListActivity();
            }
        } else {
            moveIntroActivity();
        }


    }

    @UiThread
    void moveIntroActivity() {
        IntroActivity_.intent(PushInterfaceActivity.this)
                .flags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .start();
        finish();
    }

    @UiThread
    void moveMessageListActivity() {
        Intent intent = new Intent(PushInterfaceActivity.this, MessageListActivity_.class);
        intent.putExtra(JandiConstants.EXTRA_ENTITY_ID, entityId);
        intent.putExtra(JandiConstants.EXTRA_ENTITY_TYPE, entityType);
        intent.putExtra(JandiConstants.EXTRA_IS_FROM_PUSH, true);
        intent.putExtra(JandiConstants.EXTRA_TEAM_ID, teamId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}

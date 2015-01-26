package com.tosslab.jandi.app.push;

import android.app.Activity;
import android.content.Intent;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
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
import org.apache.log4j.Logger;

/**
 * Created by Steve SeongUg Jung on 15. 1. 15..
 */
@EActivity(R.layout.activity_intro)
public class PushInterfaceActivity extends Activity {

    private static final Logger logger = Logger.getLogger(PushInterfaceActivity.class);
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

        checkTeamInfo();

    }

    @Background
    void checkTeamInfo() {

        if (!jandiInterfaceModel.hasTeamInfo(teamId)) {
            moveIntroActivity();
        }

        if (jandiInterfaceModel.setupSelectedTeam(teamId)) {
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

        if (!jandiInterfaceModel.hasBackStackActivity()) {
            MainTabActivity_.intent(PushInterfaceActivity.this)
                    .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .start();
        }

        MessageListV2Activity_.intent(PushInterfaceActivity.this)
                .teamId(teamId)
                .entityId(entityId)
                .entityType(entityType)
                .isFromPush(true)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .start();

        finish();
    }
}

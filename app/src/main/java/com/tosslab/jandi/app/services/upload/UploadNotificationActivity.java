package com.tosslab.jandi.app.services.upload;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.push.model.JandiInterfaceModel;
import com.tosslab.jandi.app.push.model.JandiInterfaceModel_;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.intro.IntroActivity;
import com.tosslab.jandi.app.ui.maintab.MainTabActivity_;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class UploadNotificationActivity extends BaseAppCompatActivity {

    public static String EXTRA_TEAM_ID = "teamId";
    public static String EXTRA_ENTITY_ID = "entityId";

    private long teamId;
    private long entityId;

    public static void startActivity(Context context, long teamId, long entityId) {
        Intent intent = new Intent(context, UploadNotificationActivity.class);
        intent.putExtra(EXTRA_TEAM_ID, teamId);
        intent.putExtra(EXTRA_ENTITY_ID, entityId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    public static Intent getIntent(Context context, long teamId, long entityId) {
        Intent intent = new Intent(context, UploadNotificationActivity.class);
        intent.putExtra(EXTRA_TEAM_ID, teamId);
        intent.putExtra(EXTRA_ENTITY_ID, entityId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        Intent intent = getIntent();

        if (isEmptyInfo(intent)) {
            moveToIntro();
            return;
        }
        boolean used = (getIntent().getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0;

        if (used) {
            moveToIntro();
            return;
        }

        JandiInterfaceModel jandiInterfaceModel = JandiInterfaceModel_.getInstance_(JandiApplication.getContext());

        teamId = intent.getLongExtra(EXTRA_TEAM_ID, -1);
        entityId = intent.getLongExtra(EXTRA_ENTITY_ID, -1);

        Observable
                .create((Subscriber<? super Boolean> subscriber) -> {
                    boolean success = jandiInterfaceModel.setupSelectedTeam(teamId);
                    subscriber.onNext(success);
                    subscriber.onCompleted();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success -> {
                    if (success) {
                        moveToRoom(teamId, entityId);
                    } else {
                        moveToIntro();
                    }
                });
    }

    private void moveToRoom(long teamId, long entityId) {
        MainTabActivity_.intent(UploadNotificationActivity.this)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .fromPush(true)
                .start();

        int entityType;


        if (TeamInfoLoader.getInstance().isTopic(entityId)) {
            if (TeamInfoLoader.getInstance().isPublicTopic(entityId)) {

                entityType = JandiConstants.TYPE_PUBLIC_TOPIC;
            } else {
                entityType = JandiConstants.TYPE_PRIVATE_TOPIC;

            }
        } else {
            entityType = JandiConstants.TYPE_DIRECT_MESSAGE;
        }

        MessageListV2Activity_.intent(UploadNotificationActivity.this)
                .teamId(teamId)
                .entityId(entityId)
                .entityType(entityType)
                .isFromPush(false)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK)
                .start();

        finish();

    }

    private void moveToIntro() {
        IntroActivity.startActivity(UploadNotificationActivity.this, false);
        finish();
    }

    private boolean isEmptyInfo(Intent intent) {

        return intent == null || intent.getLongExtra(EXTRA_TEAM_ID, -1) == -1 || intent.getLongExtra(EXTRA_ENTITY_ID, -1) == -1;
    }
}

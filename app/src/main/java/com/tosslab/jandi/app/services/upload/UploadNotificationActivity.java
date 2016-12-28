package com.tosslab.jandi.app.services.upload;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.tosslab.jandi.app.Henson;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.push.model.JandiInterfaceModel;
import com.tosslab.jandi.app.services.upload.dagger.DaggerUploadNotificationComponent;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.intro.IntroActivity;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class UploadNotificationActivity extends BaseAppCompatActivity {

    public static String EXTRA_TEAM_ID = "teamId";
    public static String EXTRA_ENTITY_ID = "entityId";

    @Inject
    JandiInterfaceModel jandiInterfaceModel;

    private long teamId;
    private long entityId;

    public static void startActivity(Context context, long teamId, long entityId) {
        Intent intent = new Intent(context, UploadNotificationActivity.class);
        intent.putExtra(EXTRA_TEAM_ID, teamId);
        intent.putExtra(EXTRA_ENTITY_ID, entityId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        if (context instanceof Activity) {
            ((Activity) context).overridePendingTransition(0, 0);
        }
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

        DaggerUploadNotificationComponent.create().inject(this);

        teamId = intent.getLongExtra(EXTRA_TEAM_ID, -1);
        entityId = intent.getLongExtra(EXTRA_ENTITY_ID, -1);

        Observable
                .defer(() -> Observable.just(jandiInterfaceModel.setupSelectedTeam(teamId)))
                .doOnNext(success -> {
                    try {
                        if (success && TeamInfoLoader.getInstance().getTeamId() != teamId) {
                            jandiInterfaceModel.refreshTeamInfo();
                        }
                    } catch (Exception e) {
                        jandiInterfaceModel.refreshTeamInfo();
                    }
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

        startActivity(Henson.with(this)
                .gotoMainTabActivity()
                .build()
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

        int entityType;

        long roomId;

        if (TeamInfoLoader.getInstance().isTopic(entityId)) {
            if (TeamInfoLoader.getInstance().isPublicTopic(entityId)) {
                entityType = JandiConstants.TYPE_PUBLIC_TOPIC;
            } else {
                entityType = JandiConstants.TYPE_PRIVATE_TOPIC;
            }
            roomId = entityId;
        } else {
            entityType = JandiConstants.TYPE_DIRECT_MESSAGE;
            roomId = TeamInfoLoader.getInstance().getChatId(entityId);
        }

        startActivity(Henson.with(UploadNotificationActivity.this)
                .gotoMessageListV2Activity()
                .teamId(teamId)
                .entityId(entityId)
                .entityType(entityType)
                .roomId(roomId)
                .isFromPush(false)
                .build()
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
        overridePendingTransition(0, 0);

        finish();

    }

    private void moveToIntro() {
        IntroActivity.startActivity(UploadNotificationActivity.this, false);
        overridePendingTransition(0, 0);
        finish();
    }

    private boolean isEmptyInfo(Intent intent) {

        return intent == null || intent.getLongExtra(EXTRA_TEAM_ID, -1) == -1 || intent.getLongExtra(EXTRA_ENTITY_ID, -1) == -1;
    }
}

package com.tosslab.jandi.app.ui.socketevent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.services.socket.JandiSocketServiceModel;
import com.tosslab.jandi.app.services.socket.dagger.SocketServiceModule;
import com.tosslab.jandi.app.ui.maintab.MainTabActivity_;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.ui.socketevent.dagger.DaggerSocketInitComponent;
import com.tosslab.jandi.app.utils.UnLockPassCodeManager;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@Deprecated
public class SocketInitActivity extends AppCompatActivity {

    private static final String EXTRA_FROM = "from";
    private static final String EXTRA_ROOM_ID = "roomId";
    private static final String EXTRA_TEAM_ID = "teamId";
    private static final String EXTRA_ENTITY_ID = "entityId";
    private static final String EXTRA_ENTITY_TYPE = "entityType";
    private static final int EXTRA_FROM_LAUNCHER = 0;
    private static final int EXTRA_FROM_PUSH = 1;
    @Inject
    JandiSocketServiceModel model;
    private int from = EXTRA_FROM_LAUNCHER;
    private long teamId;
    private long roomId;
    private long entityId;
    private int entityType;

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, SocketInitActivity.class));
    }

    public static void startActivity(Context context, long teamId, long roomId, long entityId, int entityType) {
        Intent intent = new Intent(context, SocketInitActivity.class);
        intent.putExtra(EXTRA_FROM, EXTRA_FROM_PUSH);
        intent.putExtra(EXTRA_TEAM_ID, teamId);
        intent.putExtra(EXTRA_ROOM_ID, roomId);
        intent.putExtra(EXTRA_ENTITY_ID, entityId);
        intent.putExtra(EXTRA_ENTITY_TYPE, entityType);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket_init);

        initExtras();

        DaggerSocketInitComponent.builder().socketServiceModule(new SocketServiceModule(JandiApplication.getContext()))
                .build().inject(this);

        initEvents();
    }

    private void initEvents() {
        Observable.just(new Object())
                .observeOn(Schedulers.io())
                .doOnNext(it -> {
//                    model.updateEventHistory();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnCompleted(() -> {
                    model.stopEventPublisher();
                    model.stopMessageCreatePublisher();
                    if (from == EXTRA_FROM_LAUNCHER) {
                        moveMainTab();
                    } else {
                        moveMessage();
                    }
                })
                .subscribe();
    }

    private void initExtras() {
        Intent data = getIntent();
        if (data == null) {
            return;
        }

        from = data.getIntExtra(EXTRA_FROM, EXTRA_FROM_LAUNCHER);
        if (from != EXTRA_FROM_LAUNCHER) {
            teamId = data.getLongExtra(EXTRA_TEAM_ID, -1);
            roomId = data.getLongExtra(EXTRA_ROOM_ID, -1);
            entityId = data.getLongExtra(EXTRA_ENTITY_ID, -1);
            entityType = data.getIntExtra(EXTRA_ENTITY_TYPE, -1);
        }
    }

    private void moveMessage() {
        MainTabActivity_.intent(SocketInitActivity.this)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .fromPush(true)
                .start();

        Intent intent = MessageListV2Activity_.intent(SocketInitActivity.this)
                .teamId(teamId)
                .roomId(roomId)
                .entityId(entityId)
                .entityType(entityType)
                .isFromPush(true)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .get();

        UnLockPassCodeManager.getInstance().unLockPassCodeFirstIfNeed(this, intent);

        finish();

    }

    private void moveMainTab() {
        MainTabActivity_.intent(SocketInitActivity.this)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .start();
        finish();
    }
}

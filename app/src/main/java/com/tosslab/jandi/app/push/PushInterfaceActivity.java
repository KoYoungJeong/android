package com.tosslab.jandi.app.push;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.tosslab.jandi.app.Henson;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.push.dagger.DaggerPushInterfaceComponent;
import com.tosslab.jandi.app.push.model.JandiInterfaceModel;
import com.tosslab.jandi.app.services.socket.monitor.SocketServiceStarter;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.intro.IntroActivity;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.UnLockPassCodeManager;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.parse.PushUtil;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class PushInterfaceActivity extends BaseAppCompatActivity {
    public static final String TAG = "JANDI.PushInterfaceActivity";

    public static final String EXTRA_ROOM_TYPE = "roomType";
    public static final String EXTRA_ROOM_ID = "entityId";
    // Push로 부터 넘어온 MainActivity의 Extra
    public static final String EXTRA_ENTITY_TYPE = "entityType";
    public static final String EXTRA_IS_FROM_PUSH = "isFromPush";
    public static final String EXTRA_TEAM_ID = "teamId";
    // Push -> 선택된 엔티티 설정이 안됨에 따라...

    public static long selectedEntityId;

    long roomId;
    int entityType;
    boolean isFromPush;
    long temId;
    String roomType = "";
    @Inject
    JandiInterfaceModel jandiInterfaceModel;

    public static void startActivity(Context context,
                                     long roomId,
                                     int entityType,
                                     boolean isFromPush,
                                     long teamId, String roomType) {
        Intent intent = new Intent(context, PushInterfaceActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(EXTRA_ROOM_ID, roomId);
        intent.putExtra(EXTRA_ENTITY_TYPE, entityType);
        intent.putExtra(EXTRA_IS_FROM_PUSH, isFromPush);
        intent.putExtra(EXTRA_TEAM_ID, teamId);
        intent.putExtra(EXTRA_ROOM_TYPE, roomType);

        context.startActivity(intent);
    }

    public static Intent getIntent(Context context,
                                   long roomId,
                                   int entityType,
                                   boolean isFromPush,
                                   long teamId, String roomType) {
        Intent intent = new Intent(context, PushInterfaceActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(EXTRA_ROOM_ID, roomId);
        intent.putExtra(EXTRA_ENTITY_TYPE, entityType);
        intent.putExtra(EXTRA_IS_FROM_PUSH, isFromPush);
        intent.putExtra(EXTRA_TEAM_ID, teamId);
        intent.putExtra(EXTRA_ROOM_TYPE, roomType);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        DaggerPushInterfaceComponent.create().inject(this);

        initObject();
        checkTeamAndMoveToNextActivity();

        AnalyticsUtil.sendScreenName(AnalyticsValue.Screen.PushNotification);
        setNeedUnLockPassCode(false);

    }

    void initObject() {

        getExtra();

        if (!JandiPreference.isPutVersionCodeStamp()) {
            MessageRepository.getRepository().deleteAllLink();
            JandiPreference.putVersionCodeStamp();
        }

        if (jandiInterfaceModel.hasNotRegisteredAtNewPushService()) {
            PushUtil.registPush();
        }
    }

    void getExtra() {
        Intent intent = getIntent();
        roomId = intent.getLongExtra(EXTRA_ROOM_ID, 0);
        entityType = intent.getIntExtra(EXTRA_ENTITY_TYPE, 0);
        isFromPush = intent.getBooleanExtra(EXTRA_IS_FROM_PUSH, false);
        teamId = intent.getLongExtra(EXTRA_TEAM_ID, 0);
        roomType = intent.getStringExtra(EXTRA_ROOM_TYPE);
    }

    public void checkTeamAndMoveToNextActivity() {
        LogUtil.i(TAG, "Upgrade is not necessary.");

        boolean used = (getIntent().getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0;

        if (!used) {
            selectedEntityId = roomId;

            checkTeamInfo();
        } else {
            selectedEntityId = -1;
            moveIntroActivity();
        }
    }

    void checkTeamInfo() {
        if (!jandiInterfaceModel.hasTeamInfo(teamId)) {
            moveIntroActivity();
            return;
        }

        Observable<Boolean> share = Observable.defer(() -> {
            return Observable.just(jandiInterfaceModel.setupSelectedTeam(teamId));
        }).share();

        // 팀 정보가 있다면
        share.filter(it -> it)
                .map(it -> jandiInterfaceModel.getEntityInfo(roomId, roomType))
                .doOnNext(it -> {
                    if (it > 0) {
                        sendBroadcast(new Intent(SocketServiceStarter.START_SOCKET_SERVICE));
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(entityId -> {
                    if (entityId > 0) {
                        moveMessageListActivity(roomId, entityId);
                    } else {
                        // entity 정보가 없으면 인트로로 이동하도록 지정
                        moveIntroActivity();
                    }

                }, t -> moveIntroActivity());

        // 팀 정보가 없다면
        share.filter(it -> !it)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(it -> moveIntroActivity(), t -> moveIntroActivity());

    }

    void moveIntroActivity() {
        IntroActivity.startActivity(PushInterfaceActivity.this, false);

        finish();
    }

    void moveMessageListActivity(long roomId, long targetEntityId) {

        startActivity(Henson.with(this)
                .gotoMainTabActivity()
                .tabIndex(TeamInfoLoader.getInstance().isTopic(roomId) ? 0 : 1)
                .build()
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

        Intent intent = MessageListV2Activity_.intent(PushInterfaceActivity.this)
                .teamId(teamId)
                .roomId(roomId)
                .entityId(targetEntityId)
                .entityType(entityType)
                .isFromPush(true)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .get();

        UnLockPassCodeManager.getInstance().unLockPassCodeFirstIfNeed(this, intent);

        finish();
    }

}

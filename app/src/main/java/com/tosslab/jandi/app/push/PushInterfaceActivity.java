package com.tosslab.jandi.app.push;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.util.Pair;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.EntitiesUpdatedEvent;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.network.client.account.AccountApi;
import com.tosslab.jandi.app.network.client.events.EventsApi;
import com.tosslab.jandi.app.network.client.main.LoginApi;
import com.tosslab.jandi.app.network.client.messages.MessageApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ResConfig;
import com.tosslab.jandi.app.push.model.JandiInterfaceModel;
import com.tosslab.jandi.app.services.socket.JandiSocketServiceModel;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.intro.IntroActivity_;
import com.tosslab.jandi.app.ui.maintab.MainTabActivity_;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.utils.AlertUtil;
import com.tosslab.jandi.app.utils.ApplicationUtil;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.UnLockPassCodeManager;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.parse.PushUtil;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.schedulers.Schedulers;


/**
 * Created by Steve SeongUg Jung on 15. 1. 15..
 */
@EActivity(R.layout.activity_intro)
public class PushInterfaceActivity extends BaseAppCompatActivity {
    public static final String TAG = "JANDI.PushInterfaceActivity";

    public static final String EXTRA_ROOM_TYPE = "roomType";
    public static final String EXTRA_ENTITY_ID = "entityId";
    // Push로 부터 넘어온 MainActivity의 Extra
    public static final String EXTRA_ENTITY_TYPE = "entityType";
    public static final String EXTRA_IS_FROM_PUSH = "isFromPush";
    public static final String EXTRA_TEAM_ID = "teamId";
    // Push -> 선택된 엔티티 설정이 안됨에 따라...

    public static long selectedEntityId;

    @Extra(PushInterfaceActivity.EXTRA_ENTITY_ID)
    long entityId;
    @Extra(PushInterfaceActivity.EXTRA_ENTITY_TYPE)
    int entityType;
    @Extra(PushInterfaceActivity.EXTRA_IS_FROM_PUSH)
    boolean isFromPush;
    @Extra(PushInterfaceActivity.EXTRA_TEAM_ID)
    long teamId;
    @Extra(PushInterfaceActivity.EXTRA_ROOM_TYPE)
    String roomType = "";

    @Bean
    JandiInterfaceModel jandiInterfaceModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AnalyticsUtil.sendScreenName(AnalyticsValue.Screen.PushNotification);
        setNeedUnLockPassCode(false);

    }

    @AfterInject
    void initObject() {

        if (!JandiPreference.getPrefVersion214()) {
            MessageRepository.getRepository().deleteAllLink();
            JandiPreference.setPrefVersion214();
        }

        if (jandiInterfaceModel.hasNotRegisteredAtNewPushService()) {
            PushUtil.registPush();
        }

        checkTeamAndMoveToNextActivity();
    }

    @Deprecated
    @Background(serial = "push_interface_activity_background")
    public void checkNewVersion() {
        try {
            ResConfig config = jandiInterfaceModel.getConfigInfo();

            int installedAppVersion = jandiInterfaceModel.getInstalledAppVersion();

            LogUtil.i(TAG, "installedAppVersion - " + installedAppVersion
                    + " config.versions.android - " + config.versions.android);

            if (config.maintenance != null && config.maintenance.status) {
                showMaintenanceDialog();
            } else if (installedAppVersion < config.versions.android) {
                showUpdateDialog();
            } else {
                checkTeamAndMoveToNextActivity();
            }
        } catch (RetrofitException e) {
            if (e.getStatusCode() >= 500) {
                showCheckNetworkDialog();
            } else {
                checkTeamAndMoveToNextActivity();
            }
        } catch (Exception e) {
            checkTeamAndMoveToNextActivity();
        }
    }

    @UiThread
    void showMaintenanceDialog() {
        AlertUtil.showConfirmDialog(this,
                R.string.jandi_service_maintenance, (dialog, which) -> finish(),
                false);
    }

    @UiThread
    void showCheckNetworkDialog() {
        AlertUtil.showCheckNetworkDialog(this, (dialog, which) -> finish());
    }

    @UiThread
    public void showUpdateDialog() {
        AlertUtil.showConfirmDialog(this, R.string.jandi_update_title,
                R.string.jandi_update_message, (dialog, which) -> {
                    ApplicationUtil.startAppMarketAndFinish(PushInterfaceActivity.this);
                },
                false);
    }

    @Background(serial = "push_interface_activity_background")
    public void checkTeamAndMoveToNextActivity() {
        LogUtil.i(TAG, "Upgrade is not necessary.");

        boolean used = (getIntent().getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0;

        if (!used) {
            selectedEntityId = entityId;

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

        if (jandiInterfaceModel.setupSelectedTeam(teamId)) {

            long roomId = entityId;
            Pair<Boolean, Long> entityInfo = jandiInterfaceModel.getEntityInfo(entityId, roomType);

            if (entityInfo.second > 0) {
                if (!entityInfo.first) {
                    Observable.just(jandiInterfaceModel.getEntityInfo())
                            .subscribeOn(Schedulers.io())
                            .filter(success -> success)
                            .subscribe(entityRefreshed -> {
                                EventBus eventBus = EventBus.getDefault();
                                if (eventBus.hasSubscriberForEvent(EntitiesUpdatedEvent.class)) {
                                    eventBus.post(new EntitiesUpdatedEvent());
                                }
                            });

                }
                // 메세지 갱신
                new JandiSocketServiceModel(PushInterfaceActivity.this,
                        () -> new AccountApi(RetrofitBuilder.getInstance()),
                        () -> new MessageApi(RetrofitBuilder.getInstance()),
                        () -> new LoginApi(RetrofitBuilder.getInstance()),
                        () -> new EventsApi(RetrofitBuilder.getInstance()))
                        .updateEventHistory();
                moveMessageListActivity(roomId, entityInfo.second);
            } else {
                // entity 정보가 없으면 인트로로 이동하도록 지정
                moveIntroActivity();
            }

        } else {
            moveIntroActivity();
        }
    }

    @UiThread
    void moveIntroActivity() {
        IntroActivity_.intent(PushInterfaceActivity.this)
                .flags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .start();
        finish();
    }

    @UiThread
    void moveMessageListActivity(long roomId, long targetEntityId) {

        MainTabActivity_.intent(PushInterfaceActivity.this)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .fromPush(true)
                .start();

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

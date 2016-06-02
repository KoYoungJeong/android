package com.tosslab.jandi.app.ui.message.detail.model;

import android.content.Context;
import android.support.v7.app.AlertDialog;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.TopicLeaveEvent;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.client.chat.ChatApi;
import com.tosslab.jandi.app.network.dagger.DaggerApiClientComponent;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;
import org.json.JSONException;

import javax.inject.Inject;

import dagger.Lazy;
import de.greenrobot.event.EventBus;


/**
 * Created by Steve SeongUg Jung on 14. 12. 10..
 */
@EBean
public class LeaveViewModel {

    @Bean
    EntityClientManager entityClientManager;

    @Inject
    Lazy<ChatApi> chatApi;
    private Context context;

    private long entityId;

    @AfterInject
    void initObject() {
        DaggerApiClientComponent.create().inject(this);
    }

    public void initData(Context context, long entityId) {
        this.context = context;
        this.entityId = entityId;
    }

    public void leave() {
        if (TeamInfoLoader.getInstance().isPublicTopic(entityId)
                || TeamInfoLoader.getInstance().isUser(entityId)) {
            leaveEntityInBackground();
        } else {
            showPrivateTopicLeaveDialog();
        }
    }

    private void showPrivateTopicLeaveDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context,
                R.style.JandiTheme_AlertDialog_FixWidth_300);
        builder.setTitle(TeamInfoLoader.getInstance().getName(entityId))
                .setMessage(R.string.jandi_message_leave_private_topic)
                .setNegativeButton(R.string.jandi_cancel, null)
                .setPositiveButton(R.string.jandi_action_leave, (dialog, which) -> leaveEntityInBackground())
                .create()
                .show();
    }


    @Background
    void leaveEntityInBackground() {
        try {
            int type = -1;
            if (TeamInfoLoader.getInstance().isTopic(entityId)) {
                if (TeamInfoLoader.getInstance().isPublicTopic(entityId)) {
                    entityClientManager.leaveChannel(entityId);
                    type = JandiConstants.TYPE_PUBLIC_TOPIC;
                } else {
                    entityClientManager.leavePrivateGroup(entityId);
                    type = JandiConstants.TYPE_PRIVATE_TOPIC;
                }
            } else if (TeamInfoLoader.getInstance().isUser(entityId)) {
                long memberId = TeamInfoLoader.getInstance().getMyId();
                chatApi.get().deleteChat(memberId, entityId);
                type = JandiConstants.TYPE_DIRECT_MESSAGE;
            }
            trackLeavingEntity(type);

            trackTopicLeaveSuccess(entityId);

            EventBus.getDefault().post(new TopicLeaveEvent());

        } catch (RetrofitException e) {
            int errorCode = e.getStatusCode();
            trackTopicLeaveFail(errorCode);
            e.printStackTrace();
            leaveEntityFailed(context.getString(R.string.err_entity_leave));
        } catch (Exception e) {
            trackTopicLeaveFail(-1);
            e.printStackTrace();
            leaveEntityFailed(context.getString(R.string.err_entity_leave));
        }
    }

    private void trackLeavingEntity(int entityType) {
        String distictId = TeamInfoLoader.getInstance().getMyId()
                + "-" + TeamInfoLoader.getInstance().getTeamId();
        try {
            MixpanelMemberAnalyticsClient
                    .getInstance(context, distictId)
                    .trackLeavingEntity(entityType == JandiConstants.TYPE_PUBLIC_TOPIC);
        } catch (JSONException e) {
            LogUtil.e("CANNOT MEET", e);
        }
    }

    private void trackTopicLeaveSuccess(long entityId) {
        AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                .event(Event.TopicLeave)
                .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                .property(PropertyKey.ResponseSuccess, true)
                .property(PropertyKey.TopicId, entityId)
                .build());

    }

    private void trackTopicLeaveFail(int errorCode) {
        AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                .event(Event.TopicLeave)
                .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                .property(PropertyKey.ResponseSuccess, false)
                .property(PropertyKey.ErrorCode, errorCode)
                .build());

    }

    @UiThread
    void leaveEntityFailed(String errMessage) {
        ColoredToast.show(errMessage);
    }
}

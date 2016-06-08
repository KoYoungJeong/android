package com.tosslab.jandi.app.ui.message.detail.model;

import android.content.Context;
import android.support.v7.app.AlertDialog;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.TopicLeaveEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.client.chat.ChatApi;
import com.tosslab.jandi.app.network.dagger.DaggerApiClientComponent;
import com.tosslab.jandi.app.network.exception.RetrofitException;
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
        FormattedEntity entity = EntityManager.getInstance().getEntityById(entityId);
        if (entity.isPublicTopic() || entity.isUser() || EntityManager.getInstance().isBot(entityId)) {
            leaveEntityInBackground(entity);
        } else {
            showPrivateTopicLeaveDialog(entity);
        }
    }

    private void showPrivateTopicLeaveDialog(FormattedEntity entity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context,
                R.style.JandiTheme_AlertDialog_FixWidth_300);
        builder.setTitle(entity.getName())
                .setMessage(R.string.jandi_message_leave_private_topic)
                .setNegativeButton(R.string.jandi_cancel, null)
                .setPositiveButton(R.string.jandi_action_leave, (dialog, which) -> leaveEntityInBackground(entity))
                .create()
                .show();
    }


    @Background
    void leaveEntityInBackground(FormattedEntity entity) {
        try {
            long entityId = entity.getId();
            if (entity.isPublicTopic()) {
                entityClientManager.leaveChannel(entityId);
            } else if (entity.isPrivateGroup()) {
                entityClientManager.leavePrivateGroup(entityId);
            } else if (entity.isUser() || EntityManager.getInstance().isBot(entityId)) {
                long memberId = EntityManager.getInstance().getMe().getId();
                chatApi.get().deleteChat(memberId, entityId);
            }

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

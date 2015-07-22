package com.tosslab.jandi.app.ui.message.detail.model;

import android.content.Context;
import android.support.v7.app.AlertDialog;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.TopicLeaveEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;
import org.json.JSONException;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;

/**
 * Created by Steve SeongUg Jung on 14. 12. 10..
 */
@EBean
public class LeaveViewModel {

    @Bean
    EntityClientManager entityClientManager;

    private Context context;

    private int entityId;

    public void initData(Context context, int entityId) {
        this.context = context;
        this.entityId = entityId;
    }

    public void leave() {
        FormattedEntity entity = EntityManager.getInstance(context).getEntityById(entityId);
        if (entity.isPublicTopic() || entity.isUser()) {
            leaveEntityInBackground(entity);
        } else {
            showPrivateTopicLeaveDialog(entity);
        }
    }

    private void showPrivateTopicLeaveDialog(FormattedEntity entity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
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
            int entityId = entity.getId();
            if (entity.isPublicTopic()) {
                entityClientManager.leaveChannel(entityId);
            } else if (entity.isPrivateGroup()) {
                entityClientManager.leavePrivateGroup(entityId);
            } else if (entity.isUser()) {
                int memberId = EntityManager.getInstance(context).getMe().getId();
                RequestApiManager.getInstance().deleteChatByChatApi(memberId, entityId);
            }
            trackLeavingEntity(entity.isPublicTopic() ? JandiConstants.TYPE_PUBLIC_TOPIC : entity
                    .isPrivateGroup() ? JandiConstants.TYPE_PRIVATE_TOPIC : JandiConstants.TYPE_DIRECT_MESSAGE);

            EventBus.getDefault().post(new TopicLeaveEvent());

        } catch (RetrofitError e) {
            e.printStackTrace();
            leaveEntityFailed(context.getString(R.string.err_entity_leave));
        } catch (Exception e) {
            e.printStackTrace();
            leaveEntityFailed(context.getString(R.string.err_entity_leave));
        }
    }

    private void trackLeavingEntity(int entityType) {
        String distictId = EntityManager.getInstance(context).getDistictId();
        try {
            MixpanelMemberAnalyticsClient
                    .getInstance(context, distictId)
                    .trackLeavingEntity(entityType == JandiConstants.TYPE_PUBLIC_TOPIC);
        } catch (JSONException e) {
            LogUtil.e("CANNOT MEET", e);
        }
    }

    @UiThread
    void leaveEntityFailed(String errMessage) {
        ColoredToast.showError(context, errMessage);
    }
}
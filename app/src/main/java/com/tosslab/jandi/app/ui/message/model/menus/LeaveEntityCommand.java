package com.tosslab.jandi.app.ui.message.model.menus;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.client.JandiEntityClient;
import com.tosslab.jandi.app.network.manager.RequestManager;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.ui.maintab.chat.model.ChatDeleteRequest;
import com.tosslab.jandi.app.ui.message.to.ChattingInfomations;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;
import org.json.JSONException;

/**
 * Created by Steve SeongUg Jung on 14. 12. 10..
 */
@EBean
class LeaveEntityCommand implements MenuCommand {

    private Activity activity;
    private JandiEntityClient mJandiEntityClient;
    private ChattingInfomations chattingInfomations;

    void initData(Activity activity, JandiEntityClient mJandiEntityClient, ChattingInfomations chattingInfomations) {
        this.activity = activity;
        this.mJandiEntityClient = mJandiEntityClient;
        this.chattingInfomations = chattingInfomations;
    }

    @Override
    public void execute(MenuItem menuItem) {
        if (chattingInfomations.isPublicTopic() || chattingInfomations.isDirectMessage()) {
            leaveEntityInBackground();
        } else {
            showPrivateTopicLeaveDialog(chattingInfomations.entityId, chattingInfomations.entityName);
        }
    }

    private void showPrivateTopicLeaveDialog(final int entityId, String entityName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(entityName)
                .setMessage(R.string.jandi_message_leave_private_topic)
                .setNegativeButton(R.string.jandi_cancel, null)
                .setPositiveButton(R.string.jandi_action_leave, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        leaveEntityInBackground();
                    }
                }).create().show();
    }


    @Background
    public void leaveEntityInBackground() {
        try {
            if (chattingInfomations.isPublicTopic()) {
                mJandiEntityClient.leaveChannel(chattingInfomations.entityId);
            } else if (chattingInfomations.isPrivateTopic()) {
                mJandiEntityClient.leavePrivateGroup(chattingInfomations.entityId);
            } else if (chattingInfomations.isDirectMessage()) {
                int memberId = EntityManager.getInstance(activity).getMe().getId();
                RequestManager.newInstance(activity, ChatDeleteRequest.create(activity, memberId, chattingInfomations.entityId)).request();
            }
            trackLeavingEntity(chattingInfomations.entityType);
            leaveEntitySucceed();
        } catch (JandiNetworkException e) {
            LogUtil.e("fail to leave cdp");
            leaveEntityFailed(activity.getString(R.string.err_entity_leave));
        } catch (Exception e) {
            LogUtil.e("fail to leave cdp");
            leaveEntityFailed(activity.getString(R.string.err_entity_leave));
        }
    }

    private void trackLeavingEntity(int entityType) {
        String distictId = EntityManager.getInstance(activity).getDistictId();
        try {
            MixpanelMemberAnalyticsClient
                    .getInstance(activity, distictId)
                    .trackLeavingEntity(entityType == JandiConstants.TYPE_PUBLIC_TOPIC);
        } catch (JSONException e) {
            LogUtil.e("CANNOT MEET", e);
        }
    }

    @UiThread
    public void leaveEntitySucceed() {
        activity.finish();
    }

    @UiThread
    public void leaveEntityFailed(String errMessage) {
        ColoredToast.showError(activity, errMessage);
    }
}

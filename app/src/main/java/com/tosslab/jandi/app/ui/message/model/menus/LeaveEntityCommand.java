package com.tosslab.jandi.app.ui.message.model.menus;

import android.app.Activity;
import android.view.MenuItem;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.network.client.JandiEntityClient;
import com.tosslab.jandi.app.ui.BaseAnalyticsActivity;
import com.tosslab.jandi.app.ui.message.to.ChattingInfomations;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiNetworkException;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;
import org.apache.log4j.Logger;

/**
 * Created by Steve SeongUg Jung on 14. 12. 10..
 */
@EBean
class LeaveEntityCommand implements MenuCommand {

    private static final Logger log = Logger.getLogger(LeaveEntityCommand.class);


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
        leaveEntityInBackground();
    }

    @Background
    public void leaveEntityInBackground() {
        try {
            if (chattingInfomations.isPublicTopic()) {
                mJandiEntityClient.leaveChannel(chattingInfomations.entityId);
            } else if (chattingInfomations.isPrivateTopic()) {
                mJandiEntityClient.leavePrivateGroup(chattingInfomations.entityId);
            }
            leaveEntitySucceed();
        } catch (JandiNetworkException e) {
            log.error("fail to leave cdp");
            leaveEntityFailed(activity.getString(R.string.err_entity_leave));
        }
    }

    @UiThread
    public void leaveEntitySucceed() {
        ((BaseAnalyticsActivity) activity).trackLeavingEntity(EntityManager.getInstance(activity), chattingInfomations.entityType);
        activity.finish();
    }

    @UiThread
    public void leaveEntityFailed(String errMessage) {
        ColoredToast.showError(activity, errMessage);
    }
}

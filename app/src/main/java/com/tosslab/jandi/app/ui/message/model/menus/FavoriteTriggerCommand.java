package com.tosslab.jandi.app.ui.message.model.menus;

import android.app.Activity;
import android.view.MenuItem;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.TopicInfoUpdateEvent;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.client.JandiEntityClient;
import com.tosslab.jandi.app.ui.message.to.ChattingInfomations;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;
import org.springframework.web.client.RestClientException;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 14. 12. 10..
 */
@EBean
class FavoriteTriggerCommand implements MenuCommand {

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
        triggerFavorite(mJandiEntityClient, chattingInfomations, menuItem);
    }

    private void triggerFavorite(JandiEntityClient mJandiEntityClient, ChattingInfomations chattingInfomations, MenuItem item) {
        if (chattingInfomations.isFavorite) {
            chattingInfomations.isFavorite = false;
        } else {
            chattingInfomations.isFavorite = true;
        }

        EntityManager.getInstance(activity).getEntityById(chattingInfomations.entityId).isStarred = chattingInfomations.isFavorite;
        EventBus.getDefault().post(new TopicInfoUpdateEvent(chattingInfomations.entityId));


        if (chattingInfomations.isFavorite) {
            enableFavoriteInBackground(mJandiEntityClient, chattingInfomations);
        } else {
            disableFavoriteInBackground(mJandiEntityClient, chattingInfomations);
        }

    }

    @Background
    void enableFavoriteInBackground(JandiEntityClient mJandiEntityClient, ChattingInfomations chattingInfomations) {
        try {
            if (chattingInfomations.entityId > 0) {
                mJandiEntityClient.enableFavorite(chattingInfomations.entityId);
            }
            enableFavoriteSucceed();
        } catch (RestClientException e) {
            LogUtil.e("enable favorite failed", e);
        } catch (Exception e) {
            LogUtil.e("enable favorite failed", e);
        }
    }

    @UiThread
    void enableFavoriteSucceed() {
        ColoredToast.show(activity, activity.getString(R.string.jandi_message_starred));
    }

    @Background
    void disableFavoriteInBackground(JandiEntityClient mJandiEntityClient, ChattingInfomations chattingInfomations) {
        try {
            if (chattingInfomations.entityId > 0) {
                mJandiEntityClient.disableFavorite(chattingInfomations.entityId);
            }
        } catch (RestClientException e) {
            LogUtil.e("disable favorite failed", e);
        } catch (Exception e) {
            LogUtil.e("disable favorite failed", e);
        }
    }
}

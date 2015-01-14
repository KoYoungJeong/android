package com.tosslab.jandi.app.ui;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.ui.message.MessageListActivity_;
import com.tosslab.jandi.app.utils.ProgressWheel;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 10. 3..
 */
public class BaseChatListFragment extends Fragment {
    protected ProgressWheel mProgressWheel;
    protected Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        mContext = getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setProgressWheel();
    }

    @Override
    public void onStop() {
        if (mProgressWheel != null)
            mProgressWheel.dismiss();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    private void setProgressWheel() {
        mProgressWheel = new ProgressWheel(getActivity());
        mProgressWheel.init();
    }

    protected void moveToPublicTopicMessageActivity(int channelId) {
        moveToMessageActivity(channelId, JandiConstants.TYPE_PUBLIC_TOPIC, false);
    }

    protected void moveToPrivateTopicMessageActivity(int privateGroupId) {
        moveToMessageActivity(privateGroupId, JandiConstants.TYPE_PRIVATE_TOPIC, false);
    }

    protected void moveToMessageActivity(FormattedEntity entity) {
        int type = (entity.isPublicTopic())
                ? JandiConstants.TYPE_PUBLIC_TOPIC
                : (entity.isPrivateGroup())
                    ? JandiConstants.TYPE_PRIVATE_TOPIC
                    : JandiConstants.TYPE_DIRECT_MESSAGE;
        moveToMessageActivity(entity.getId(), type, entity.isStarred);
    }

    private void moveToMessageActivity(final int entityId, final int entityType, final boolean isStarred) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                MessageListActivity_.intent(mContext)
                        .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        .entityType(entityType)
                        .entityId(entityId)
                        .isFavorite(isStarred)
                        .start();
            }
        }, 250);
    }
}

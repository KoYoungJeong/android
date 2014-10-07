package com.tosslab.jandi.app.ui.main;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.ui.MessageListActivity_;
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

    protected void moveToChannelMessageActivity(int channelId) {
        ((JandiApplication)getActivity().getApplication()).setEntityManager(null);
        moveToMessageActivity(channelId, JandiConstants.TYPE_CHANNEL);
    }

    protected void moveToPrivateGroupMessageActivity(int privateGroupId) {
        ((JandiApplication)getActivity().getApplication()).setEntityManager(null);
        moveToMessageActivity(privateGroupId, JandiConstants.TYPE_PRIVATE_GROUP);
    }

    protected void moveToMessageActivity(FormattedEntity entity) {
        int type = (entity.isChannel())
                ? JandiConstants.TYPE_CHANNEL
                : (entity.isPrivateGroup())
                    ? JandiConstants.TYPE_PRIVATE_GROUP
                    : JandiConstants.TYPE_DIRECT_MESSAGE;
        moveToMessageActivity(entity.getId(), type);
    }

    private void moveToMessageActivity(final int entityId, final int entityType) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                MessageListActivity_.intent(mContext)
                        .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        .entityType(entityType)
                        .entityId(entityId)
                        .start();
            }
        }, 250);
    }
}

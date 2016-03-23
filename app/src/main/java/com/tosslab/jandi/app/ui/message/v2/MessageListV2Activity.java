package com.tosslab.jandi.app.ui.message.v2;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.Menu;

import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.services.socket.JandiSocketService;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.message.v2.search.view.MessageSearchListFragment_;

import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

/**
 * Created by Steve SeongUg Jung on 15. 1. 20..
 */
@EActivity
public class MessageListV2Activity extends BaseAppCompatActivity {

    public static final String TAG_LIST = "list";
    @Extra
    int entityType;
    @Extra
    long entityId;
    @Extra
    boolean isFavorite = false;
    @Extra
    boolean isFromPush = false;
    @Extra
    long teamId;
    @Extra
    boolean isFromSearch = false;
    @Extra
    long lastReadLinkId = -1;
    @Extra
    long roomId;
    @Extra
    long firstCursorLinkId = -1;

    private OnBackPressedListener onBackPressedListener;
    private OnKeyPressListener onKeyPressListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        JandiPreference.setSocketReconnectDelay(0l);
        JandiSocketService.startServiceIfNeed(this);

        initViews();

    }

    void initViews() {
        if (teamId <= 0) {
            teamId = EntityManager.getInstance().getTeamId();
        }

        Fragment messageListFragment = getSupportFragmentManager()
                .findFragmentByTag(TAG_LIST);

        if (messageListFragment == null) {
            if (!isFromSearch) {
                messageListFragment = MessageListV2Fragment_.builder()
                        .entityId(entityId)
                        .roomId(roomId)
                        .entityType(entityType)
                        .isFavorite(isFavorite)
                        .isFromPush(isFromPush)
                        .teamId(teamId)
                        .lastReadLinkId(lastReadLinkId)
                        .firstCursorLinkId(firstCursorLinkId)
                        .build();
            } else {
                messageListFragment = MessageSearchListFragment_.builder()
                        .entityId(entityId)
                        .roomId(roomId)
                        .entityType(entityType)
                        .isFavorite(isFavorite)
                        .teamId(teamId)
                        .lastMarker(lastReadLinkId)
                        .build();
            }

            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, messageListFragment, TAG_LIST)
                    .commit();
        }

        if (messageListFragment instanceof OnBackPressedListener) {
            onBackPressedListener = (OnBackPressedListener) messageListFragment;
        }

        if (messageListFragment instanceof OnKeyPressListener) {
            onKeyPressListener = ((OnKeyPressListener) messageListFragment);
        }
    }

    @Override
    public void onBackPressed() {
        if (onBackPressedListener != null && onBackPressedListener.onBackPressed()) {
            // Do Nothing
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        LogUtil.d("dispatchKeyEvent : " + event.getKeyCode() + ", Action : " + event.getAction());

        if (onKeyPressListener != null) {
            boolean consumed = onKeyPressListener.onKey(event.getKeyCode(), event);
            if (consumed) {
                return true;
            }
        }

        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return false;
    }

    public interface OnBackPressedListener {
        boolean onBackPressed();
    }

    public interface OnKeyPressListener {
        boolean onKey(int keyCode, KeyEvent event);
    }

}

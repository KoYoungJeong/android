package com.tosslab.jandi.app.ui.message.v2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;

import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.services.socket.JandiSocketService;
import com.tosslab.jandi.app.services.socket.monitor.SocketServiceStarter;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

/**
 * Created by Steve SeongUg Jung on 15. 1. 20..
 */
@EActivity
public class MessageListV2Activity extends AppCompatActivity {

    @Extra
    int entityType;
    @Extra
    int entityId;
    @Extra
    boolean isFavorite = false;
    @Extra
    boolean isFromPush = false;
    @Extra
    int teamId;
    @Extra
    boolean isFromSearch = false;
    @Extra
    int lastMarker = -1;
    @Extra
    int roomId;

    private OnBackPressedListener onBackPressedListener;
    private OnKeyPressListener onKeyPressListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        JandiSocketService.stopService(this);
        sendBroadcast(new Intent(SocketServiceStarter.START_SOCKET_SERVICE));

        initViews();
    }

    void initViews() {
        if (teamId <= 0) {
            teamId = EntityManager.getInstance().getTeamId();
        }

        Fragment messageListFragment = getSupportFragmentManager()
                .findFragmentByTag(MessageListFragment.class.getName());

        if (messageListFragment == null) {
            messageListFragment = MessageListFragment_.builder()
                    .entityId(entityId)
                    .roomId(roomId)
                    .entityType(entityType)
                    .isFavorite(isFavorite)
                    .isFromPush(isFromPush)
                    .teamId(teamId)
                    .lastMarker(lastMarker)
                    .isFromSearch(isFromSearch)
                    .build();
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content,
                            messageListFragment,
                            MessageListFragment.class.getName())
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

    public interface OnBackPressedListener {
        boolean onBackPressed();
    }

    public interface OnKeyPressListener {
        boolean onKey(int keyCode, KeyEvent event);
    }
}

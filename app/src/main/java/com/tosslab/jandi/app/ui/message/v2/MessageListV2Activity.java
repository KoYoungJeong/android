package com.tosslab.jandi.app.ui.message.v2;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.Menu;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.tosslab.jandi.app.Henson;
import com.tosslab.jandi.app.services.socket.JandiSocketService;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.message.v2.search.view.MessageSearchListFragment;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.logger.LogUtil;

public class MessageListV2Activity extends BaseAppCompatActivity {

    public static final String TAG_LIST = "list";
    public static final String KEY_ENTITY_ID = "entityId";
    @Nullable
    @InjectExtra
    int entityType;
    @Nullable
    @InjectExtra
    long entityId;
    @Nullable
    @InjectExtra
    boolean isFavorite = false;
    @Nullable
    @InjectExtra
    boolean isFromPush = false;
    @Nullable
    @InjectExtra
    long teamId;
    @Nullable
    @InjectExtra
    boolean isFromSearch = false;
    @Nullable
    @InjectExtra
    long lastReadLinkId = -1;
    @Nullable
    @InjectExtra
    long roomId;
    @Nullable
    @InjectExtra
    long firstCursorLinkId = -1;

    private OnBackPressedListener onBackPressedListener;
    private OnKeyPressListener onKeyPressListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        JandiPreference.setSocketReconnectDelay(0l);
        JandiSocketService.startServiceIfNeed(this);

        Dart.inject(this);

        initViews();
    }

    void initViews() {
        if (teamId <= 0) {
            teamId = TeamInfoLoader.getInstance().getTeamId();
        }

        Fragment messageListFragment = getSupportFragmentManager()
                .findFragmentByTag(TAG_LIST);

        if (messageListFragment == null) {
            if (!isFromSearch) {
                messageListFragment = new MessageListV2Fragment();
                messageListFragment.setArguments(Henson.with(this)
                        .gotoMessageListV2Fragment()
                        .entityId(entityId)
                        .roomId(roomId)
                        .entityType(entityType)
                        .isFromPush(isFromPush)
                        .teamId(teamId)
                        .lastReadLinkId(lastReadLinkId)
                        .firstCursorLinkId(firstCursorLinkId)
                        .build()
                        .getExtras());
            } else {
                messageListFragment = new MessageSearchListFragment();
                messageListFragment.setArguments(Henson.with(this)
                        .gotoMessageSearchListFragment()
                        .entityId(entityId)
                        .roomId(roomId)
                        .entityType(entityType)
                        .teamId(teamId)
                        .lastMarker(lastReadLinkId)
                        .build().getExtras());
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

    @Override
    public void finish() {
        Intent data = new Intent();
        data.putExtra(KEY_ENTITY_ID, entityId);
        setResult(RESULT_OK, data);
        super.finish();
    }

    public interface OnBackPressedListener {
        boolean onBackPressed();
    }

    public interface OnKeyPressListener {
        boolean onKey(int keyCode, KeyEvent event);
    }

}

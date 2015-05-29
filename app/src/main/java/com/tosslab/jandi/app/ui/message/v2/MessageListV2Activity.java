package com.tosslab.jandi.app.ui.message.v2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.ui.maintab.MainTabActivity_;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
    }

    void initViews() {
        if (teamId <= 0) {
            teamId = EntityManager.getInstance(MessageListV2Activity.this).getTeamId();
        }

        Fragment messageListFragment = getSupportFragmentManager()
                .findFragmentByTag(MessageListFragment.class.getName());

        if (messageListFragment == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content,
                            MessageListFragment_.builder()
                                    .entityId(entityId)
                                    .roomId(roomId)
                                    .entityType(entityType)
                                    .isFavorite(isFavorite)
                                    .isFromPush(isFromPush)
                                    .teamId(teamId)
                                    .lastMarker(lastMarker)
                                    .isFromSearch(isFromSearch)
                                    .build(),
                            MessageListFragment.class.getName())
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        if (isFromPush) {
            MainTabActivity_.intent(MessageListV2Activity.this)
                    .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .start();
        } else {
            super.onBackPressed();
        }
    }
}

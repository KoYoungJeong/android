package com.tosslab.jandi.app.ui.message.v2;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

/**
 * Created by Steve SeongUg Jung on 15. 1. 20..
 */
@EActivity
public class MessageListV2Activity extends ActionBarActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
    }

    void initViews() {

        getSupportFragmentManager()
                .beginTransaction()
                .add(
                        android.R.id.content,
                        MessageListFragment_
                                .builder()
                                .entityId(entityId)
                                .entityType(entityType)
                                .isFavorite(isFavorite)
                                .isFromPush(isFromPush)
                                .teamId(teamId)
                                .build(),
                        MessageListFragment.class.getName()

                )
                .commit();

    }
}

package com.tosslab.jandi.app.ui.entities;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;

import com.tosslab.jandi.app.ui.entities.chats.ChatsChooseFragment;
import com.tosslab.jandi.app.ui.entities.chats.ChatsChooseFragment_;

/**
 * Created by Steve SeongUg Jung on 15. 1. 14..
 */
public class EntityPagerAdapter extends FragmentPagerAdapter {


    private final EntityChooseActivity.Type type;
    private FragmentManager fragmentManager;
    private View[] titleView;

    public EntityPagerAdapter(FragmentManager fragmentManager, View[] titleView, EntityChooseActivity.Type type) {
        super(fragmentManager);
        this.fragmentManager = fragmentManager;
        this.titleView = titleView;
        this.type = type;
    }

    @Override
    public Fragment getItem(int position) {

        ChatsChooseFragment fragment = ChatsChooseFragment_.builder()
                .build();
        switch (type) {
            case ALL:

                break;
            case TOPIC:
                break;
            case MESSAGES:
                break;
        }

        return fragment;
    }

    @Override
    public int getCount() {
        return titleView.length;
    }

}

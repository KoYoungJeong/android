package com.tosslab.jandi.app.ui.search.main.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.tosslab.jandi.app.ui.search.messages.view.MessageSearchFragment_;

/**
 * Created by Steve SeongUg Jung on 15. 3. 10..
 */
public class SearchAdapter extends FragmentStatePagerAdapter {

    public SearchAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        Fragment fragment;

        switch (position) {
            case 1:
                fragment = MessageSearchFragment_.builder().build();
                break;
            default:
            case 0:
                fragment = MessageSearchFragment_.builder().build();
                break;
        }

        return fragment;
    }

    @Override
    public int getCount() {
        return 2;
    }
}

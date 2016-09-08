package com.tosslab.jandi.app.ui.search.file.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.tosslab.jandi.app.ui.maintab.tabs.file.FileListFragment;
import com.tosslab.jandi.app.ui.search.messages.view.MessageSearchFragment_;

/**
 * Created by Steve SeongUg Jung on 15. 3. 10..
 */
public class SearchAdapter extends FragmentPagerAdapter {

    private Fragment[] fragments;

    public SearchAdapter(FragmentManager fm) {
        super(fm);
        fragments = new Fragment[2];
    }

    @Override
    public Fragment getItem(int position) {

        Fragment fragment = fragments[position];

        if (fragment == null) {
            switch (position) {
                default:
                case 0:
                    fragment = MessageSearchFragment_.builder().build();
                    break;
                case 1:
                    fragment = new FileListFragment();
                    break;
            }

            fragments[position] = fragment;
        }

        return fragment;
    }

    @Override
    public int getCount() {
        return fragments.length;
    }
}

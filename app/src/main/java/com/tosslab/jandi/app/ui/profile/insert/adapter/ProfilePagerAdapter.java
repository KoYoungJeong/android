package com.tosslab.jandi.app.ui.profile.insert.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.tosslab.jandi.app.ui.profile.insert.views.SetProfileFirstPageFragment_;
import com.tosslab.jandi.app.ui.profile.insert.views.SetProfileSecondPageFragment_;


/**
 * Created by tee on 16. 3. 16..
 */

public class ProfilePagerAdapter extends FragmentPagerAdapter {

    final int NUM_PAGE = 2;

    public ProfilePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return SetProfileFirstPageFragment_.builder().build();
        }
        return SetProfileSecondPageFragment_.builder().build();
    }

    @Override
    public int getCount() {
        return NUM_PAGE;
    }

}

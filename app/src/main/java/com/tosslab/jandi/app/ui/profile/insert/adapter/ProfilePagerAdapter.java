package com.tosslab.jandi.app.ui.profile.insert.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.tosslab.jandi.app.ui.profile.insert.views.InsertProfileFirstPageFragment;
import com.tosslab.jandi.app.ui.profile.insert.views.InsertProfileSecondPageFragment;

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
        Bundle bundle = new Bundle();
        bundle.putString(InsertProfileFirstPageFragment.MODE,
                InsertProfileFirstPageFragment.MODE_INSERT_PROFILE);
        if (position == 0) {
            Fragment setProfileFirstPageFragment = new InsertProfileFirstPageFragment();
            setProfileFirstPageFragment.setArguments(bundle);
            return setProfileFirstPageFragment;
        }
        Fragment setProfileSecondPageFragment = new InsertProfileSecondPageFragment();
        setProfileSecondPageFragment.setArguments(bundle);
        return setProfileSecondPageFragment;
    }

    @Override
    public int getCount() {
        return NUM_PAGE;
    }

}
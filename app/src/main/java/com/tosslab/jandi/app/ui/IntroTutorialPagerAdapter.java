package com.tosslab.jandi.app.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

/**
 * Created by justinygchoi on 14. 10. 23..
 */
public class IntroTutorialPagerAdapter extends FragmentPagerAdapter {

    public IntroTutorialPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return IntroTutorialFragment_.builder().pageType(position).build();
    }

    @Override
    public int getCount() {
        return IntroTutorialFragment.NUM_OF_PAGES;
    }
}

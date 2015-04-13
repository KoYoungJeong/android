package com.tosslab.jandi.app.ui.login.adapter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

import com.tosslab.jandi.app.ui.login.login.IntroLoginFragment_;
import com.tosslab.jandi.app.ui.login.tutorial.IntroTutorialFragment;
import com.tosslab.jandi.app.ui.login.tutorial.IntroTutorialFragment_;

/**
 * Created by justinygchoi on 14. 10. 23..
 */
public class IntroMainPagerAdapter extends FragmentPagerAdapter {
    final int NUM_OF_PAGES_WITH_TUTORIAL = 4;

    public IntroMainPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        // 튜토리얼을 읽었거나, 튜토리얼 이후의 페이지는 로그인 fragment
        if (position > IntroTutorialFragment.LAST_PAGE) {
            return IntroLoginFragment_.builder().build();
        }

        return IntroTutorialFragment_.builder().pageType(position).build();
    }

    @Override
    public int getCount() {
        return NUM_OF_PAGES_WITH_TUTORIAL;
    }
}

package com.tosslab.jandi.app.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

/**
 * Created by justinygchoi on 14. 10. 23..
 */
public class IntroMainPagerAdapter extends FragmentPagerAdapter {
    final int NUM_OF_PAGES_WITHOUT_TUTORIAL = 1;
    final int NUM_OF_PAGES_WITH_TUTORIAL = 4;
    private boolean mDidReadTutorial;

    public IntroMainPagerAdapter(FragmentManager fm, boolean didReadTutorial) {
        super(fm);
        mDidReadTutorial = didReadTutorial;
    }

    @Override
    public Fragment getItem(int position) {
        // 튜토리얼을 읽었거나, 튜토리얼 이후의 페이지는 로그인 fragment
        if (mDidReadTutorial || (position > IntroTutorialFragment.LAST_PAGE)) {
            return IntroLoginFragment_.builder().build();
        }

        return IntroTutorialFragment_.builder().pageType(position).build();
    }

    @Override
    public int getCount() {
        return (mDidReadTutorial) ? NUM_OF_PAGES_WITHOUT_TUTORIAL : NUM_OF_PAGES_WITH_TUTORIAL;
    }
}

package com.tosslab.jandi.app.ui.maintab.tabs.mypage;

import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.MentionListFragment;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.poll.PollListFragment;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.starred.StarredListFragment;

/**
 * Created by tonyjs on 2016. 8. 19..
 */
public class MyPagePagerAdapter extends FragmentPagerAdapter {

    private final SparseArray<Fragment> fragmentCache;

    public MyPagePagerAdapter(FragmentManager fm) {
        super(fm);
        fragmentCache = new SparseArray<>(3);
        fragmentCache.put(0, new MentionListFragment());
        fragmentCache.put(1, new StarredListFragment());
        fragmentCache.put(2, new PollListFragment());
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentCache.get(position);
    }

    @Override
    public int getCount() {
        return fragmentCache.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Resources resources = JandiApplication.getContext()
                .getResources();

        switch (position) {
            case 0:
                return resources.getString(R.string.jandi_mention_mentions);
            case 1:
                return resources.getString(R.string.jandi_starred_stars);
            case 2:
                return resources.getString(R.string.jandi_poll);
        }
        return "";
    }
}

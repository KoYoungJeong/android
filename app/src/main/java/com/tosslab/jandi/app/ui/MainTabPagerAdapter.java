package com.tosslab.jandi.app.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;

import com.tosslab.jandi.app.R;

/**
 * Created by justinygchoi on 2014. 8. 11..
 */
public class MainTabPagerAdapter extends FragmentStatePagerAdapter {
    private final String[] TITLES = { "채널", "유저", "비밀그룹", "검색", "셋팅" };
    private final int[] ICONS = {
            R.drawable.jandi_icon_channel,
            R.drawable.jandi_icon_directmsg,
            R.drawable.jandi_icon_privategroup,
            R.drawable.jandi_icon_rm_file,
            R.drawable.jandi_icon_rm_setting
    };

    public MainTabPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = new MainChannelListFragment();
        return fragment;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return TITLES[position];
    }

    public int getIcon(int position) {
        return ICONS[position];
    }

    @Override
    public int getCount() {
        return TITLES.length;
    }
}

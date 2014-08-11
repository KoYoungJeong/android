package com.tosslab.jandi.app.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

import com.tosslab.jandi.app.R;

/**
 * Created by justinygchoi on 2014. 8. 11..
 */
public class MainTabPagerAdapter extends FragmentPagerAdapter {
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
        switch (position) {
            case 0:
                return MainChannelListFragment_.builder().build();
            case 1:
                return MainUserListFragment_.builder().build();
            case 2:
                return MainPrivateGroupListFragment_.builder().build();
//            case 4:
//                return SettingsFragment_.builder().build();
            default:
                return MainChannelListFragment_.builder().build();
        }
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

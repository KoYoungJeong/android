package com.tosslab.jandi.app.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v13.app.FragmentPagerAdapter;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.main.MainPrivateListFragment_;
import com.tosslab.jandi.app.ui.main.MainPublicListFragment_;

/**
 * Created by justinygchoi on 2014. 8. 11..
 */
public class MainTabPagerAdapter extends FragmentPagerAdapter {
    private final int[] mTitleRes = {
            R.string.jandi_tab_channel,
            R.string.jandi_tab_private_group,
            R.string.jandi_tab_file
    };

    private Context mContext;
    public MainTabPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return MainPublicListFragment_
                        .builder()
                        .build();
            case 1:
                return MainPrivateListFragment_
                        .builder()
                        .build();
            case 2:
                return FileListFragment_
                        .builder()
                        .build();
            default:
                return MainUserEntityListFragment_.builder().build();
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getString(mTitleRes[position]);
    }

    @Override
    public int getCount() {
        return mTitleRes.length;
    }
}

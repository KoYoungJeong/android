package com.tosslab.jandi.app.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v13.app.FragmentPagerAdapter;
import android.view.View;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.main.MainPrivateListFragment_;
import com.tosslab.jandi.app.ui.main.MainPublicListFragment_;
import com.tosslab.jandi.app.utils.PagerSlidingTabStrip;

/**
 * Created by justinygchoi on 2014. 8. 11..
 */
public class MainTabPagerAdapter
        extends FragmentPagerAdapter
        implements PagerSlidingTabStrip.ViewTabProvider {
    private View[] mTabs;

    private final int[] mTitleRes = {
            R.string.jandi_tab_topic,
            R.string.jandi_tab_chat,
            R.string.jandi_tab_file
    };

    private Context mContext;
    public MainTabPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    public MainTabPagerAdapter(FragmentManager fm, View[] tabs) {
        super(fm);

//        View[] Tabs = new View[2];
//        Tabs[0] = getLayoutInflater().inflate(R.layout.someLayout, null);
//        Tabs[1] = getLayoutInflater().inflate(R.layout.someOtherLayout, null);
//
        mTabs = tabs;
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
                return MainPublicListFragment_.builder().build();
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

    @Override
    public View getPageView(int position) {
        return mTabs[position];
    }

    public void setTabs(View[] tabs) {
        mTabs = tabs;
    }
}

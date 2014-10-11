package com.tosslab.jandi.app.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v13.app.FragmentPagerAdapter;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.utils.PagerSlidingTabStrip;

/**
 * Created by justinygchoi on 2014. 8. 11..
 */
public class MainTabPagerAdapter
        extends FragmentPagerAdapter
        implements PagerSlidingTabStrip.IconTabProvider {
    private static final int TAB_TOPIC  = 0;
    private static final int TAB_CHAT   = 1;
    private static final int TAB_FILE   = 2;
    private static final int TAB_MORE   = 3;

    private final int[] mIconRes = {
            R.drawable.jandi_tab_selector_topic,
            R.drawable.jandi_tab_selector_chat,
            R.drawable.jandi_tab_selector_file,
            R.drawable.jandi_tab_selector_more
    };

    private final int[] mTitleRes = {
            R.string.jandi_tab_topic,
            R.string.jandi_tab_chat,
            R.string.jandi_tab_file,
            R.string.jandi_tab_more

    };

    private Context mContext;
    public MainTabPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

//    public MainTabPagerAdapter(FragmentManager fm, View[] tabs) {
//        super(fm);

//        View[] Tabs = new View[2];
//        Tabs[0] = getLayoutInflater().inflate(R.layout.someLayout, null);
//        Tabs[1] = getLayoutInflater().inflate(R.layout.someOtherLayout, null);
//
//        mTabs = tabs;
//    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case TAB_TOPIC:
                return MainPublicListFragment_
                        .builder()
                        .build();
            case TAB_CHAT:
                return MainPrivateListFragment_
                        .builder()
                        .build();
            case TAB_FILE:
                return MainFileListFragment_
                        .builder()
                        .build();
            case TAB_MORE:
                return MainMoreFragment_
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

//    @Override
//    public View getPageView(int position) {
//        return mTabs[position];
//    }
//
//    public void setTabs(View[] tabs) {
//        mTabs = tabs;
//    }

    @Override
    public int getPageIconResId(int position) {
        return mIconRes[position];
    }
}

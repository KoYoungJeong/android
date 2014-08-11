package com.tosslab.jandi.app.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.tosslab.jandi.app.R;

/**
 * Created by justinygchoi on 2014. 8. 11..
 */
public class MainTabActivity extends Activity {
    MainTabPagerAdapter mMainTabPagerAdapter;
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tab);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
            }

            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
            }
        };

        // Create the dapter and ViewPager
        mMainTabPagerAdapter = new MainTabPagerAdapter(getFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager_main_tab);
        mViewPager.setAdapter(mMainTabPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // add a tab to the action bar
        addTabToActionBar(actionBar, tabListener);
    }

    private void addTabToActionBar(ActionBar actionBar, ActionBar.TabListener tabListener) {
        actionBar.addTab(
                actionBar.newTab()
                        .setIcon(R.drawable.jandi_icon_channel)
                        .setTabListener(tabListener)
        );
        actionBar.addTab(
                actionBar.newTab()
                        .setIcon(R.drawable.jandi_icon_directmsg)
                        .setTabListener(tabListener)
        );
        actionBar.addTab(
                actionBar.newTab()
                        .setIcon(R.drawable.jandi_icon_privategroup)
                        .setTabListener(tabListener)
        );
        actionBar.addTab(
                actionBar.newTab()
                        .setIcon(R.drawable.jandi_icon_rm_file)
                        .setTabListener(tabListener)
        );
        actionBar.addTab(
                actionBar.newTab()
                        .setIcon(R.drawable.jandi_icon_rm_setting)
                        .setTabListener(tabListener)
        );
    }
}

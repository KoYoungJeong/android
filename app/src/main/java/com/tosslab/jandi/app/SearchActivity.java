package com.tosslab.jandi.app;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.res.Resources;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.tosslab.jandi.app.lists.CdpItemManager;
import com.tosslab.jandi.app.utils.ViewGroupUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.apache.log4j.Logger;

import java.util.Locale;

import de.greenrobot.event.EventBus;

@EActivity(R.layout.activity_search)
public class SearchActivity extends BaseActivity implements ActionBar.TabListener {
    private final Logger log = Logger.getLogger(BaseActivity.class);
    @Extra
    public int searchMode;    // 서치 모드

    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;

    public CdpItemManager cdpItemManager = null;

    @AfterViews
    void initView() {
        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(true);

        int titleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
        View titleView = findViewById(titleId);

        // attach listener to this spinnerView for handling spinner selection change
        Spinner spinnerView = (Spinner) getLayoutInflater().inflate(R.layout.spinner_search_type, null);
        spinnerView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // All tab으로 이동하여 다시 서치를 수행
                log.debug(i + " selected");
            }
        });
        ViewGroupUtils.replaceView(titleView, spinnerView);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this)
            );
        }

        // 특정인 검색으로 시작한다면 두번째 텝으로 이동
        if (searchMode == JandiConstants.TYPE_SEARCH_MODE_USER) {
            mViewPager.setCurrentItem(1);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().registerSticky(this);
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    /**
     * Sticky Event from MainRightFragment
     * SearchListFragment에서 cdpItemManager를 FileDetailActivity로 넘겨주기 위해 사용
     * @param event
     */
    public void onEvent(CdpItemManager event) {
        cdpItemManager = event;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_search:
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                // Everyone
                return SearchListFragment_
                        .builder()
                        .whichTab(JandiConstants.TYPE_SEARCH_USER_ALL)
                        .searchMode(searchMode)
                        .build();
            } else {
                // Certain user
                return SearchListFragment_
                        .builder()
                        .whichTab(JandiConstants.TYPE_SEARCH_USER_SPECIFIC)
                        .searchMode(searchMode)
                        .build();
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section_everyone).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section_me).toUpperCase(l);
            }
            return null;
        }
    }
}

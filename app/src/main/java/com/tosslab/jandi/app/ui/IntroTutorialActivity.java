package com.tosslab.jandi.app.ui;

import android.app.Activity;
import android.support.v4.view.ViewPager;
import android.widget.Button;

import com.tosslab.jandi.app.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Fullscreen;
import org.androidannotations.annotations.ViewById;

/**
 * Created by justinygchoi on 14. 10. 23..
 */
@Fullscreen
@EActivity(R.layout.activity_tutorial)
public class IntroTutorialActivity extends Activity {
    private ViewPager mViewPager;
    private IntroTutorialPagerAdapter mAdapter;
    @ViewById(R.id.btn_tutorial_first)
    Button buttonTutorialFirst;
    @ViewById(R.id.btn_tutorial_second)
    Button buttonTutorialSecond;
    @ViewById(R.id.btn_tutorial_last)
    Button buttonTutorialLast;

    @AfterViews
    void init() {
        setUpView();
        setTab();
    }

    private void setUpView(){
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mAdapter = new IntroTutorialPagerAdapter(getFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(IntroTutorialFragment.FIRST_PAGE);
        buttonTutorialFirst.setSelected(true);
    }

    private void setTab(){
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener(){

            @Override
            public void onPageScrollStateChanged(int position) {}
            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {}
            @Override
            public void onPageSelected(int position) {
                btnAction(position);
            }
        });
    }

    private void btnAction(int position) {
        boolean selectedOnFirst = (position == IntroTutorialFragment.FIRST_PAGE) ? true : false;
        boolean selectedOnSecond = (position == IntroTutorialFragment.SECOND_PAGE) ? true : false;
        boolean selectedOnLast = (position == IntroTutorialFragment.LAST_PAGE) ? true : false;
        buttonTutorialFirst.setSelected(selectedOnFirst);
        buttonTutorialSecond.setSelected(selectedOnSecond);
        buttonTutorialLast.setSelected(selectedOnLast);
    }
}

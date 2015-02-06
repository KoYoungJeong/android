package com.tosslab.jandi.app.ui.login;

import android.app.Activity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.login.adapter.IntroMainPagerAdapter;
import com.tosslab.jandi.app.ui.login.tutorial.IntroTutorialFragment;
import com.tosslab.jandi.app.utils.JandiPreference;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Fullscreen;
import org.androidannotations.annotations.ViewById;

/**
 * Created by justinygchoi on 14. 10. 23..
 */
@Fullscreen
@EActivity(R.layout.activity_intro_tutorial)
public class IntroMainActivity extends Activity {

    @ViewById(R.id.btn_tutorial_first)
    Button buttonTutorialFirst;
    @ViewById(R.id.btn_tutorial_second)
    Button buttonTutorialSecond;
    @ViewById(R.id.btn_tutorial_third)
    Button buttonTutorialThird;
    @ViewById(R.id.btn_tutorial_last)
    Button buttonTutorialLast;
    private ViewPager mViewPager;
    private IntroMainPagerAdapter mAdapter;

    @AfterViews
    void init() {
        setUpView();
        setTab();

        if (JandiPreference.isFirstLogin(IntroMainActivity.this)) {
            // If Log in User, then move last page
            mViewPager.setCurrentItem(mAdapter.getCount() - 1);
        }
    }

    private void setUpView() {
        buttonTutorialFirst.setSelected(true);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mViewPager.setOffscreenPageLimit(3);
        mAdapter = new IntroMainPagerAdapter(getFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(IntroTutorialFragment.FIRST_PAGE);

    }

    void hideIndicators() {
        buttonTutorialFirst.setVisibility(View.GONE);
        buttonTutorialSecond.setVisibility(View.GONE);
        buttonTutorialThird.setVisibility(View.GONE);
        buttonTutorialLast.setVisibility(View.GONE);
    }

    private void setTab() {
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrollStateChanged(int position) {
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageSelected(int position) {
                btnAction(position);
            }
        });
    }

    private void btnAction(int position) {
        buttonTutorialFirst.setSelected(position == IntroTutorialFragment.FIRST_PAGE);
        buttonTutorialSecond.setSelected(position == IntroTutorialFragment.SECOND_PAGE);
        buttonTutorialThird.setSelected(position == IntroTutorialFragment.LAST_PAGE);
        buttonTutorialLast.setSelected(position > IntroTutorialFragment.LAST_PAGE);
    }
}

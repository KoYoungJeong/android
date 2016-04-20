package com.tosslab.jandi.app.ui.login;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.services.socket.JandiSocketService;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.login.adapter.IntroMainPagerAdapter;
import com.tosslab.jandi.app.ui.login.tutorial.IntroTutorialFragment;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.UiUtils;
import com.tosslab.jandi.app.utils.parse.ParseUpdateUtil;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

/**
 * Created by justinygchoi on 14. 10. 23..
 */
@EActivity(R.layout.activity_intro_tutorial)
public class IntroMainActivity extends BaseAppCompatActivity {
    @ViewById(R.id.btn_tutorial_first)
    Button buttonTutorialFirst;
    @ViewById(R.id.btn_tutorial_second)
    Button buttonTutorialSecond;
    @ViewById(R.id.btn_tutorial_third)
    Button buttonTutorialThird;
    @ViewById(R.id.btn_tutorial_last)
    Button buttonTutorialLast;
    @ViewById(R.id.layout_footer_tutorial)
    LinearLayout tutorialFooterLayout;
    private ViewPager mViewPager;
    private IntroMainPagerAdapter mAdapter;
    private UiUtils.KeyboardHandler keyboardHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNeedUnLockPassCode(false);
        ParseUpdateUtil.registPush();

    }

    @AfterViews
     void init() {
        setUpView();
        setTab();

        if (JandiPreference.isFirstLogin(IntroMainActivity.this)) {
            // If Log in User, then move last page
            mViewPager.setCurrentItem(mAdapter.getCount() - 1);
        }

        JandiSocketService.stopService(IntroMainActivity.this);

        BadgeUtils.clearBadge(IntroMainActivity.this);

    }

    private void setUpView() {
        buttonTutorialFirst.setSelected(true);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mViewPager.setOffscreenPageLimit(3);
        mAdapter = new IntroMainPagerAdapter(getFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(IntroTutorialFragment.FIRST_PAGE);
    }

    private void setTab() {
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (position > IntroTutorialFragment.LAST_PAGE) {
                    tutorialFooterLayout.setVisibility(View.GONE);
                } else {
                    tutorialFooterLayout.setVisibility(View.VISIBLE);
                    btnAction(position);
                }

                hideKeyboardIfNeed(position);
            }
        });
    }

    protected void btnAction(int position) {
        buttonTutorialFirst.setSelected(position == IntroTutorialFragment.FIRST_PAGE);
        buttonTutorialSecond.setSelected(position == IntroTutorialFragment.SECOND_PAGE);
        buttonTutorialThird.setSelected(position == IntroTutorialFragment.LAST_PAGE);
        buttonTutorialLast.setSelected(position > IntroTutorialFragment.LAST_PAGE);
    }

    protected void hideKeyboardIfNeed(int position) {
        if (keyboardHandler == null) {
            return;
        }
        if (position <= IntroTutorialFragment.LAST_PAGE) {
            keyboardHandler.hideKeyboard();
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof UiUtils.KeyboardHandler) {
            keyboardHandler = (UiUtils.KeyboardHandler) fragment;
        }
    }
}

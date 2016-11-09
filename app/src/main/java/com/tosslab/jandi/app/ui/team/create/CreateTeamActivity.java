package com.tosslab.jandi.app.ui.team.create;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.profile.insert.views.InsertProfileFirstPageFragment;
import com.tosslab.jandi.app.ui.profile.insert.views.InsertProfileSecondPageFragment;
import com.tosslab.jandi.app.ui.team.create.adapter.CreateTeamPagerAdapter;
import com.tosslab.jandi.app.ui.team.create.teaminfo.InsertTeamInfoFragment;
import com.tosslab.jandi.app.utils.NonSwipeableViewPager;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by tee on 16. 6. 21..
 */

public class CreateTeamActivity extends BaseAppCompatActivity
        implements ViewPager.OnPageChangeListener,
        InsertTeamInfoFragment.OnChangePageClickListener,
        InsertProfileFirstPageFragment.OnChangePageClickListener,
        InsertProfileSecondPageFragment.OnChangePageClickListener {

    @Bind(value = {R.id.iv_page_icon_first, R.id.iv_page_icon_second})
    ImageView[] ivPageIndicator;

    @Bind(R.id.viewPager)
    NonSwipeableViewPager viewPager;
    private CreateTeamPagerAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_team);
        ButterKnife.bind(this);
        viewPager.setOffscreenPageLimit(4);
        adapter = new CreateTeamPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(this);
        viewPager.setCurrentItem(0);
        onPageSelected(0);
    }

    @Override
    public void onPageSelected(int position) {
        for (int idx = 0; idx < ivPageIndicator.length; idx++) {
            if (position == idx) {
                ivPageIndicator[idx].setSelected(true);
            } else {
                ivPageIndicator[idx].setSelected(false);
            }
        }
        final InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(viewPager.getWindowToken(), 0);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onClickMoveInsertProfileFirstPage() {
        if (viewPager != null) {
            viewPager.setCurrentItem(1);
            viewPager.getAdapter().notifyDataSetChanged();
        }
    }

    @Override
    public void onClickMoveProfileNextPage() {
        if (viewPager != null) {
            viewPager.setCurrentItem(2);
        }
    }

    @Override
    public void onClickMoveFinalPage() {
        if (viewPager != null) {
            viewPager.setCurrentItem(3);
        }
    }

    @Override
    public void onClickMovePrevPage() {
        if (viewPager != null) {
            viewPager.setCurrentItem(1);
        }
    }

    @Override
    public void onBackPressed() {
        if (viewPager != null && viewPager.getCurrentItem() == 0) {
            super.onBackPressed();
        }
    }
}

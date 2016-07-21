package com.tosslab.jandi.app.ui.profile.insert;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.widget.ImageView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.profile.insert.adapter.ProfilePagerAdapter;
import com.tosslab.jandi.app.ui.profile.insert.views.InsertProfileFirstPageFragment;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by tee on 16. 3. 15..
 */

public class InsertProfileActivity extends BaseAppCompatActivity
        implements ViewPager.OnPageChangeListener,
        InsertProfileFirstPageFragment.OnChangePageClickListener {

    @Bind(R.id.iv_page_icon_first)
    ImageView ivPageIconFirst;
    @Bind(R.id.iv_page_icon_second)
    ImageView ivPageIconSecond;

    private ViewPager viewPager;
    private ProfilePagerAdapter adapter;

    private Drawable pageNationNomal;
    private Drawable pageNationFocus;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_new_profile);
        pageNationNomal = getResources().getDrawable(R.drawable.pagenation_normal);
        pageNationFocus = getResources().getDrawable(R.drawable.pagenation_focus);
        ButterKnife.bind(this);
        setUpView();
    }

    private void setUpView() {
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setOffscreenPageLimit(2);
        adapter = new ProfilePagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(this);
        viewPager.setCurrentItem(0);
    }

    @Override
    public void onPageSelected(int position) {
        switch (position) {
            case 0:
                ivPageIconFirst.setImageDrawable(pageNationFocus);
                ivPageIconSecond.setImageDrawable(pageNationNomal);
                break;
            case 1:
                ivPageIconFirst.setImageDrawable(pageNationNomal);
                ivPageIconSecond.setImageDrawable(pageNationFocus);
                break;
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onClickMoveProfileNextPage() {
        if (viewPager != null) {
            viewPager.setCurrentItem(1);
        }
    }

}

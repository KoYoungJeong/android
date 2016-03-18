package com.tosslab.jandi.app.ui.profile.insert;

import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.widget.ImageView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.profile.insert.adapter.ProfilePagerAdapter;
import com.tosslab.jandi.app.ui.profile.insert.presenter.SetProfileFirstPagePresenter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

/**
 * Created by tee on 16. 3. 15..
 */

@EActivity(R.layout.activity_set_new_profile)
public class SetProfileActivity extends BaseAppCompatActivity
        implements ViewPager.OnPageChangeListener {

    @Bean(SetProfileFirstPagePresenter.class)
    SetProfileFirstPagePresenter setNewProfilePresenter;

    @ViewById(R.id.iv_page_icon_first)
    ImageView ivPageIconFirst;
    @ViewById(R.id.iv_page_icon_second)
    ImageView ivPageIconSecond;

    private ViewPager viewPager;
    private ProfilePagerAdapter adapter;

    private Drawable pageNationNomal;
    private Drawable pageNationFocus;

    @AfterViews
    void init() {
        pageNationNomal = getResources().getDrawable(R.drawable.pagenation_normal, null);
        pageNationFocus = getResources().getDrawable(R.drawable.pagenation_focus, null);
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

    public void goNextPage() {
        if (viewPager != null) {
            viewPager.setCurrentItem(1);
        }
    }

}

package com.tosslab.jandi.app.ui.entities;

import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.utils.activity.ActivityHelper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Steve SeongUg Jung on 15. 1. 14..
 */
@EActivity(R.layout.activity_entity_choose)
public class EntityChooseActivity extends BaseAppCompatActivity {

    @Extra
    String type = Type.ALL.name();

    @ViewById(R.id.pager_tab)
    ViewPager viewPager;

    @AfterViews
    void initViews() {

        setUpActionBar();

        View[] titleView = getTitleView();

        Type activityType = Type.valueOf(type);
        EntityPagerAdapter entityPagerAdapter = new EntityPagerAdapter(getSupportFragmentManager(), titleView, activityType);
        viewPager.setAdapter(entityPagerAdapter);
        viewPager.setOnPageChangeListener(getPageChangeListener(activityType));

        initActionBarTitle(activityType, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ActivityHelper.setOrientation(this);
    }

    private ViewPager.OnPageChangeListener getPageChangeListener(Type type) {
        return new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                initActionBarTitle(type, position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        };
    }

    private void initActionBarTitle(Type type, int position) {

        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_search_bar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        toolbar.setNavigationIcon(R.drawable.actionbar_icon_back);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));

        switch (type) {
            case ALL:
                if (position == 0) {
                    actionBar.setTitle(R.string.jandi_tab_topic);
                } else {
                    actionBar.setTitle(R.string.jandi_team_member);
                }
                break;
            case TOPIC:
                actionBar.setTitle(R.string.jandi_tab_topic);
                break;
            case MESSAGES:
                actionBar.setTitle(R.string.jandi_team_member);
                break;
        }
    }

    @OptionsItem(android.R.id.home)
    void onHomeOptionClick() {
        finish();
    }

    private void setUpActionBar() {


        Toolbar toolbar = ((Toolbar) findViewById(R.id.layout_search_bar));
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));

    }

    private View[] getTitleView() {

        Type chooseType = Type.valueOf(type);
        View[] tabViews;
        switch (chooseType) {
            case TOPIC:
                tabViews = new View[1];
                tabViews[0] = getLayoutInflater().inflate(R.layout.tab_topic, null);
                break;
            case MESSAGES:
                tabViews = new View[1];
                tabViews[0] = getLayoutInflater().inflate(R.layout.tab_chat, null);
                break;
            default:
            case ALL:
                tabViews = new View[2];
                tabViews[0] = getLayoutInflater().inflate(R.layout.tab_topic, null);
                tabViews[1] = getLayoutInflater().inflate(R.layout.tab_chat, null);
                break;

        }

        return tabViews;
    }

    public enum Type {
        ALL, TOPIC, MESSAGES
    }
}

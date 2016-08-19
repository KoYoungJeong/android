package com.tosslab.jandi.app.ui.maintab.team.adapter;


import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.tosslab.jandi.app.ui.maintab.team.filter.dept.DeptJobFragment;
import com.tosslab.jandi.app.ui.maintab.team.filter.member.TeamMemberFragment;
import com.tosslab.jandi.app.ui.maintab.team.filter.search.KeywordObservable;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

public class TeamViewPagerAdapter extends FragmentPagerAdapter {

    private static final String[] TEAM_TITLES = {
            "Members", "Departments", "Job Titles"
    };
    private final Context context;

    List<Fragment> fragments;


    public TeamViewPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.context = context;
        fragments = new ArrayList<>(TEAM_TITLES.length);
        initFragments(false);
    }

    public TeamViewPagerAdapter(Context context, FragmentManager fm, Observable<String> keywordObservable, boolean isSelectMode) {
        super(fm);
        this.context = context;
        fragments = new ArrayList<>(TEAM_TITLES.length);
        initFragments(isSelectMode);

        for (int idx = 0; idx < fragments.size(); idx++) {
            Fragment fragment = fragments.get(idx);
            if (fragment instanceof KeywordObservable) {
                ((KeywordObservable) fragment).setKeywordObservable(keywordObservable);
            }
        }
    }

    private void initFragments(boolean selectMode) {
        for (int position = 0; position < TEAM_TITLES.length; position++) {
            switch (position) {
                case 1:
                    fragments.add(DeptJobFragment.create(context, DeptJobFragment.EXTRA_TYPE_DEPT, selectMode));
                    break;
                case 2:
                    fragments.add(DeptJobFragment.create(context, DeptJobFragment.EXTRA_TYPE_JOB, selectMode));
                    break;
                default:
                case 0:
                    fragments.add(TeamMemberFragment.create(context, selectMode));
                    break;
            }
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return TEAM_TITLES[position];
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return TEAM_TITLES.length;
    }
}

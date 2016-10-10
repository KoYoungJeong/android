package com.tosslab.jandi.app.ui.team.create.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.tosslab.jandi.app.ui.profile.insert.views.InsertProfileFirstPageFragment;
import com.tosslab.jandi.app.ui.profile.insert.views.InsertProfileSecondPageFragment;
import com.tosslab.jandi.app.ui.team.create.invite.InviteTeamMemberFragment;
import com.tosslab.jandi.app.ui.team.create.teaminfo.InsertTeamInfoFragment;

public class CreateTeamPagerAdapter extends FragmentStatePagerAdapter {

    final int NUM_PAGE = 4;

    public CreateTeamPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Bundle bundle = new Bundle();
        Fragment fragment;
        switch (position) {
            case 0:
                bundle.putInt(InsertTeamInfoFragment.MODE, InsertTeamInfoFragment.MODE_FROM_MAIN_LIST);
                fragment = new InsertTeamInfoFragment();
                fragment.setArguments(bundle);
                return fragment;
            case 1:
                bundle.putString(InsertProfileFirstPageFragment.MODE, InsertProfileFirstPageFragment.MODE_TEAM_CREATE);
                fragment = new InsertProfileFirstPageFragment();
                fragment.setArguments(bundle);
                return fragment;
            case 2:
                bundle.putString(InsertProfileFirstPageFragment.MODE, InsertProfileFirstPageFragment.MODE_TEAM_CREATE);
                fragment = new InsertProfileSecondPageFragment();
                fragment.setArguments(bundle);
                return fragment;
            case 3:
                return new InviteTeamMemberFragment();
        }
        return new InsertTeamInfoFragment();
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return NUM_PAGE;
    }

}
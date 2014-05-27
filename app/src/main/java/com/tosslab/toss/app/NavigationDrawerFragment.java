package com.tosslab.toss.app;

import com.tosslab.toss.app.events.ChooseNaviActionEvent;

import org.androidannotations.annotations.EFragment;

@EFragment(R.layout.fragment_navigation_drawer)
public class NavigationDrawerFragment extends BaseFragment {
    ChooseNaviActionEvent mChooseNaviActionEvent;

    @Override
    public int getTitleResourceId() {
        return R.string.app_name;
    }

}

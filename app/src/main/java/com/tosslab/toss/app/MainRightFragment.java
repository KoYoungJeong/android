package com.tosslab.toss.app;


import android.content.Intent;
import android.widget.LinearLayout;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

@EFragment(R.layout.fragment_main_right)
public class MainRightFragment extends BaseFragment {
    @ViewById(R.id.ly_main_right_action_files)
    LinearLayout lyActionFiles;

    @Override
    public String getTitleForThisFragment() {
        return getActivity().getString(R.string.app_name);
    }

    @Click(R.id.ly_main_right_action_files)
    public void moveToSearchFiles() {
        Intent i = new Intent(getActivity(), SearchActivity_.class);
        startActivity(i);
        getActivity().overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);

    }
}

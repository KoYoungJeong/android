package com.tosslab.toss.app;


import android.widget.LinearLayout;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

@EFragment(R.layout.fragment_main_right)
public class MainRightFragment extends BaseFragment {
    @ViewById(R.id.ly_main_right_action_files)
    LinearLayout lyActionFiles;

    @Click(R.id.ly_main_right_action_files)
    public void moveToSearchFiles() {
        SearchActivity_.intent(this).searchMode(JandiConstants.TYPE_SEARCH_EVERYONE).start();
        getActivity().overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);

    }

    @Click(R.id.ly_main_right_action_my_files)
    public void moveToSearchFilesMine() {
        SearchActivity_.intent(this).searchMode(JandiConstants.TYPE_SEARCH_SPECIFIC).start();
        getActivity().overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
    }

    @Click(R.id.ly_main_right_action_all_files)
    public void moveToSearchFilesAll() {
        SearchActivity_.intent(this).searchMode(JandiConstants.TYPE_SEARCH_EVERYONE).start();
        getActivity().overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
    }

    @Click(R.id.ly_main_right_action_images)
    public void moveToSearchFilesImages() {
        SearchActivity_.intent(this).searchMode(JandiConstants.TYPE_SEARCH_IMAGES).start();
        getActivity().overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
    }
}

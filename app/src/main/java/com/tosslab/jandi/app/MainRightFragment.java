package com.tosslab.jandi.app;


import android.widget.LinearLayout;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import de.greenrobot.event.EventBus;

@EFragment(R.layout.fragment_main_right)
public class MainRightFragment extends BaseFragment {
    @ViewById(R.id.ly_main_right_action_files)
    LinearLayout lyActionFiles;

    @Click(R.id.ly_main_right_action_files)
    public void moveToSearchFiles() {
        goToSearchActivity(JandiConstants.TYPE_SEARCH_EVERYONE);
//        SearchActivity_.intent(this).searchMode(JandiConstants.TYPE_SEARCH_EVERYONE).start();
//        EventBus.getDefault().postSticky(((MainActivity)getActivity()).cdpItemManager);
//        getActivity().overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);

    }

    @Click(R.id.ly_main_right_action_my_files)
    public void moveToSearchFilesMine() {
        goToSearchActivity(JandiConstants.TYPE_SEARCH_SPECIFIC);
//        SearchActivity_.intent(this).searchMode(JandiConstants.TYPE_SEARCH_SPECIFIC).start();
//        EventBus.getDefault().postSticky(((MainActivity)getActivity()).cdpItemManager);
//        getActivity().overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
    }

    @Click(R.id.ly_main_right_action_all_files)
    public void moveToSearchFilesAll() {
        goToSearchActivity(JandiConstants.TYPE_SEARCH_EVERYONE);
//        SearchActivity_.intent(this).searchMode(JandiConstants.TYPE_SEARCH_EVERYONE).start();
//        EventBus.getDefault().postSticky(((MainActivity)getActivity()).cdpItemManager);
//        getActivity().overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
    }

    @Click(R.id.ly_main_right_action_images)
    public void moveToSearchFilesImages() {
        goToSearchActivity(JandiConstants.TYPE_SEARCH_IMAGES);
//        SearchActivity_.intent(this).searchMode(JandiConstants.TYPE_SEARCH_IMAGES).start();
//        EventBus.getDefault().postSticky(((MainActivity)getActivity()).cdpItemManager);
//        getActivity().overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
    }

    private void goToSearchActivity(int searchMode) {
        SearchActivity_.intent(this).searchMode(searchMode).start();
        EventBus.getDefault().postSticky(((MainActivity)getActivity()).cdpItemManager);
        getActivity().overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
    }
}

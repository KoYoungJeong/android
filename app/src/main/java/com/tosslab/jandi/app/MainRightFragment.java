package com.tosslab.jandi.app;


import android.content.Intent;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;

import de.greenrobot.event.EventBus;

@EFragment(R.layout.fragment_main_right)
public class MainRightFragment extends BaseFragment {
    @Click(R.id.ly_main_right_action_files)
    public void moveToSearchFiles() {
        goToSearchActivity(JandiConstants.TYPE_SEARCH_MODE_ALL);
    }

    @Click(R.id.ly_main_right_action_my_files)
    public void moveToSearchFilesMine() {
        goToSearchActivity(JandiConstants.TYPE_SEARCH_MODE_USER);
    }

    @Click(R.id.ly_main_right_action_all_files)
    public void moveToSearchFilesAll() {
        goToSearchActivity(JandiConstants.TYPE_SEARCH_MODE_ALL);
    }

    @Click(R.id.ly_main_right_action_images)
    public void moveToSearchFilesImages() {
        goToSearchActivity(JandiConstants.TYPE_SEARCH_MODE_IMAGES);
    }

    @Click(R.id.ly_main_right_action_pdf)
    public void moveToSearchFilesPdfs() {
        goToSearchActivity(JandiConstants.TYPE_SEARCH_MODE_PDF);
    }

    private void goToSearchActivity(int searchMode) {
        SearchMessageActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_CLEAR_TOP).mSearchMode(searchMode).start();
        EventBus.getDefault().postSticky(((MainActivity)getActivity()).cdpItemManager);
        getActivity().overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
    }

    @Click(R.id.ly_main_right_action_settings)
    public void moveToSetting() {
        SettingsActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_CLEAR_TOP) .start();
        getActivity().overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
    }
}

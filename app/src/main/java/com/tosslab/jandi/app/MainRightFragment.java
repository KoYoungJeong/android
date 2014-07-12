package com.tosslab.jandi.app;


import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;

import de.greenrobot.event.EventBus;

@EFragment(R.layout.fragment_main_right)
public class MainRightFragment extends BaseFragment {
    @Click(R.id.ly_main_right_action_files)
    public void moveToSearchFiles() {
        goToSearchActivity(JandiConstants.TYPE_SEARCH_EVERYONE);
    }

    @Click(R.id.ly_main_right_action_my_files)
    public void moveToSearchFilesMine() {
        goToSearchActivity(JandiConstants.TYPE_SEARCH_SPECIFIC);
    }

    @Click(R.id.ly_main_right_action_all_files)
    public void moveToSearchFilesAll() {
        goToSearchActivity(JandiConstants.TYPE_SEARCH_EVERYONE);
    }

    @Click(R.id.ly_main_right_action_images)
    public void moveToSearchFilesImages() {
        goToSearchActivity(JandiConstants.TYPE_SEARCH_IMAGES);
    }

    private void goToSearchActivity(int searchMode) {
        SearchActivity_.intent(this).searchMode(searchMode).start();
        EventBus.getDefault().postSticky(((MainActivity)getActivity()).mCdpItemManager);
        getActivity().overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
    }
}

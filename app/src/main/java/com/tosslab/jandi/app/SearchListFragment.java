package com.tosslab.jandi.app;

import android.app.Fragment;
import android.widget.ListView;

import com.tosslab.jandi.app.lists.SearchedFileItemListAdapter;
import com.tosslab.jandi.app.network.TossRestClient;
import com.tosslab.jandi.app.network.models.ReqSearchFile;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResSearchFile;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;
import org.apache.log4j.Logger;
import org.springframework.web.client.RestClientException;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 7. 4..
 */
@EFragment(R.layout.fragment_search)
public class SearchListFragment extends Fragment {
    private final Logger log = Logger.getLogger(SearchListFragment.class);

    @RestService
    TossRestClient tossRestClient;
    @FragmentArg
    int whichTab;
    @FragmentArg
    int searchMode;
    @ViewById(R.id.list_searched_file)
    ListView listSearchedFiles;
    @Bean
    SearchedFileItemListAdapter mAdapter;

    private ProgressWheel mProgressWheel;
    private String mMyToken;

    @AfterViews
    void bindAdapter() {
        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(getActivity());
        mProgressWheel.init();

        listSearchedFiles.setAdapter(mAdapter);
        mMyToken = JandiPreference.getMyToken(getActivity());
        log.debug("bind adapter for " + whichTab);

        doSearch();
    }

    @UiThread
    void doSearch() {
        mProgressWheel.show();
        doSearchInBackground();
    }

    @Background
    void doSearchInBackground() {
        tossRestClient.setHeader("Authorization", mMyToken);

        try {
            ReqSearchFile reqSearchFile = new ReqSearchFile();
            reqSearchFile.searchType = ReqSearchFile.SEARCH_TYPE_FILE;

            // 이미지 검색이라면...
            if (searchMode == JandiConstants.TYPE_SEARCH_IMAGES) {
                reqSearchFile.fileType = ReqSearchFile.FILE_TYPE_IMAGE;
            } else {
                reqSearchFile.fileType = ReqSearchFile.FILE_TYPE_ALL;
            }

            //
            if (whichTab == JandiConstants.TYPE_SEARCH_EVERYONE) {
                reqSearchFile.userId = ReqSearchFile.USER_ID_ALL;
            } else {
                reqSearchFile.userId = ReqSearchFile.USER_ID_MINE;
            }

            ResSearchFile resSearchFile = tossRestClient.searchFile(reqSearchFile);
            mAdapter.insert(resSearchFile);

            log.debug("success to find " + resSearchFile.fileCount + " files.");
            doSearchDone(true, null);
        } catch (RestClientException e) {
            log.error("fail to get searched files.", e);
            doSearchDone(false, "파일 검색에 실패했습니다");
        }
    }

    @UiThread
    void doSearchDone(boolean isOk, String message) {
        mProgressWheel.dismiss();

        if (isOk) {
            mAdapter.notifyDataSetChanged();
        } else {
            ColoredToast.showError(getActivity(), message);
        }
    }

    @ItemClick
    void list_searched_fileItemClicked(ResMessages.FileMessage searchedFile) {
        FileDetailActivity2_
                .intent(this)
                .fileId(searchedFile.id)
                .start();
        EventBus.getDefault().postSticky(((SearchActivity)getActivity()).cdpItemManager);
    }
}

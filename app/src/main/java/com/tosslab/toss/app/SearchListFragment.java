package com.tosslab.toss.app;

import android.app.Fragment;
import android.widget.ListView;

import com.tosslab.toss.app.lists.SearchedFileItemListAdapter;
import com.tosslab.toss.app.network.TossRestClient;
import com.tosslab.toss.app.network.models.ReqSearchFile;
import com.tosslab.toss.app.network.models.ResMessages;
import com.tosslab.toss.app.network.models.ResSearchFile;
import com.tosslab.toss.app.utils.ColoredToast;
import com.tosslab.toss.app.utils.ProgressWheel;

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

    @FragmentArg
    String myToken;
    @ViewById(R.id.list_searched_file)
    ListView listSearchedFiles;
    @Bean
    SearchedFileItemListAdapter mAdapter;

    private ProgressWheel mProgressWheel;

    @AfterViews
    void bindAdapter() {
        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(getActivity());
        mProgressWheel.init();

        listSearchedFiles.setAdapter(mAdapter);

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
        tossRestClient.setHeader("Authorization", myToken);

        try {
            ReqSearchFile reqSearchFile = new ReqSearchFile();
            reqSearchFile.searchType = ReqSearchFile.SEARCH_TYPE_FILE;

            // 이미지 검색이라면...
            if (searchMode == TossConstants.TYPE_SEARCH_IMAGES) {
                reqSearchFile.fileType = ReqSearchFile.FILE_TYPE_IMAGE;
            } else {
                reqSearchFile.fileType = ReqSearchFile.FILE_TYPE_ALL;
            }

            //
            if (whichTab == TossConstants.TYPE_SEARCH_EVERYONE) {
                reqSearchFile.userId = ReqSearchFile.USER_ID_ALL;
            } else {
                reqSearchFile.userId = ReqSearchFile.USER_ID_MINE;
            }

            ResSearchFile resSearchFile = tossRestClient.searchFile(reqSearchFile);
            mAdapter.insert(resSearchFile);

            log.debug("success to find " + resSearchFile.fileCount + " files.");
            doSearchDone();
        } catch (RestClientException e) {
            log.error("fail to get searched files.", e);
            doSearchError();
        }
    }

    @UiThread
    void doSearchDone() {
        mProgressWheel.dismiss();
        mAdapter.notifyDataSetChanged();
    }

    @UiThread
    void doSearchError() {
        mProgressWheel.dismiss();
        ColoredToast.showError(getActivity(), "파일 검색에 실패했습니다");
    }

    @ItemClick
    void list_searched_fileItemClicked(ResMessages.FileMessage searchedFile) {
        FileDetailActivity_
                .intent(this)
                .myToken(myToken)
                .fileId(searchedFile.id)
                .start();
    }
}

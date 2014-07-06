package com.tosslab.toss.app;

import android.app.Fragment;
import android.graphics.Color;
import android.widget.ListView;

import com.github.johnpersano.supertoasts.SuperToast;
import com.tosslab.toss.app.lists.SearchedFileItemListAdapter;
import com.tosslab.toss.app.network.MessageManipulator;
import com.tosslab.toss.app.network.TossRestClient;
import com.tosslab.toss.app.network.models.ReqSearchFile;
import com.tosslab.toss.app.network.models.ResMessages;
import com.tosslab.toss.app.network.models.ResSearchFile;
import com.tosslab.toss.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
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
    int searchType;
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

        log.debug("bind adapter for " + searchType);

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
            reqSearchFile.fileType = ReqSearchFile.FILE_TYPE_ALL;
            if (searchType == TossConstants.TYPE_SEARCH_EVERYONE) {
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
        showErrorToast("파일 검색에 실패했습니다");
    }

    @UiThread
    void showErrorToast(String message) {
        SuperToast superToast = new SuperToast(getActivity());
        superToast.setText(message);
        superToast.setDuration(SuperToast.Duration.VERY_SHORT);
        superToast.setBackground(SuperToast.Background.RED);
        superToast.setTextColor(Color.WHITE);
        superToast.show();
    }
}

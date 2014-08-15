package com.tosslab.jandi.app.ui;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.SearchedFileItemListAdapter;
import com.tosslab.jandi.app.network.TossRestClient;
import com.tosslab.jandi.app.network.models.ReqSearchFile;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResSearchFile;
import com.tosslab.jandi.app.ui.events.ChangeActionBarForFileList;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.ViewGroupUtils;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;
import org.apache.log4j.Logger;
import org.springframework.web.client.RestClientException;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 8. 14..
 */
@EFragment(R.layout.fragment_main_file_list)
public class FileListFragment extends BaseFragment {
    private final Logger log = Logger.getLogger(FileListFragment.class);

    @ViewById(R.id.list_searched_messages)
    ListView listSearchedMessages;
    @Bean
    SearchedFileItemListAdapter mAdapter;
    @RestService
    TossRestClient tossRestClient;

    private int mSearchMode;                // 서치 모드.   ALL || Images || PDFs
    private String mSearchUser  = "all";    // 사용자.     ALL || Mine || UserID

    private ProgressWheel mProgressWheel;
    private String mMyToken;
    private Context mContext;

    @AfterViews
    void bindAdapter() {
        setHasOptionsMenu(true);

        mContext = getActivity();

        // myToken 획득
        mMyToken = JandiPreference.getMyToken(mContext);

        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(mContext);
        mProgressWheel.init();

        // Empty View를 가진 ListView 설정
        View emptyView = getActivity().getLayoutInflater().inflate(R.layout.view_search_list_empty, null);
        listSearchedMessages.setEmptyView(emptyView);
        listSearchedMessages.setAdapter(mAdapter);

        // 서치 시작
        doSearch();
    }

    @AfterInject
    void calledAfterInjection() {
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    public void onEvent(ChangeActionBarForFileList event) {
        // Set up the action bar.
        final ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayUseLogoEnabled(true);

        // ActionBar의 타이틀 Text 를 강제로 Spinner 로 바꾼다.
        Spinner spinner = (Spinner) getActivity().getLayoutInflater().inflate(R.layout.spinner_search_type, null);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                if (isFirst) {
//                    return;
//                }
                // All tab으로 이동하여 다시 서치를 수행
                log.debug(i + " selected");
                mSearchMode = i;
//                setFlagSelectedAllFilesTab(true);
//                drawLayout();
                // 서치 시작
                doSearch();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
        int titleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
        View titleView = getActivity().findViewById(titleId);
        ViewGroupUtils.replaceView(titleView, spinner);
    }

    /************************************************************
     * 검색
     ************************************************************/
    @UiThread
    void doSearch() {
        mAdapter.clearAdapter();
        doSearchInBackground();
    }

    @Background
    void doSearchInBackground() {
        tossRestClient.setHeader("Authorization", mMyToken);

        try {
            ReqSearchFile reqSearchFile = new ReqSearchFile();
            reqSearchFile.searchType = ReqSearchFile.SEARCH_TYPE_FILE;

            if (mSearchMode == JandiConstants.TYPE_SEARCH_MODE_IMAGES) {
                reqSearchFile.fileType = ReqSearchFile.FILE_TYPE_IMAGE;
            } else if (mSearchMode == JandiConstants.TYPE_SEARCH_MODE_PDF) {
                reqSearchFile.fileType = ReqSearchFile.FILE_TYPE_PDF;
            } else {
                reqSearchFile.fileType = ReqSearchFile.FILE_TYPE_ALL;
            }

            reqSearchFile.userId = mSearchUser;

            ResSearchFile resSearchFile = tossRestClient.searchFile(reqSearchFile);
            searchSucceed(resSearchFile);
        } catch (RestClientException e) {
            log.error("fail to get searched files.", e);
            searchFailed("파일 검색에 실패했습니다");
        }
    }

    @UiThread
    void searchSucceed(ResSearchFile resSearchFile) {
        if (resSearchFile.fileCount > 0) {
            mAdapter.insert(resSearchFile);
        }

        log.debug("success to find " + resSearchFile.fileCount + " files.");
        mAdapter.notifyDataSetChanged();
    }

    @UiThread
    void searchFailed(String errMessage) {
        ColoredToast.showError(mContext, errMessage);
    }

    @ItemClick
    void list_searched_messagesItemClicked(ResMessages.FileMessage searchedFile) {

    }
}

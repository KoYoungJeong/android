package com.tosslab.jandi.app.ui;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ListView;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.CategorizedMenuOfFileType;
import com.tosslab.jandi.app.lists.files.SearchedFileItemListAdapter;
import com.tosslab.jandi.app.network.JandiRestClient;
import com.tosslab.jandi.app.network.models.ReqSearchFile;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResSearchFile;
import com.tosslab.jandi.app.events.CategorizingAsOwner;
import com.tosslab.jandi.app.events.StickyEntityManager;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.ProgressWheel;

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
    JandiRestClient jandiRestClient;

    private String mSearchMode  = "all";                // 서치 모드.   ALL || Images || PDFs
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

    public void onEvent(CategorizedMenuOfFileType event) {
        mSearchMode = event.getServerQuery();
        doSearch();
    }

    public void onEvent(CategorizingAsOwner event) {
        mSearchUser = event.userId + "";
        doSearch();
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
        jandiRestClient.setHeader(JandiConstants.AUTH_HEADER, mMyToken);

        try {
            ReqSearchFile reqSearchFile = new ReqSearchFile();
            reqSearchFile.searchType = ReqSearchFile.SEARCH_TYPE_FILE;
            reqSearchFile.fileType = mSearchMode;
            reqSearchFile.userId = mSearchUser;

            ResSearchFile resSearchFile = jandiRestClient.searchFile(reqSearchFile);
            searchSucceed(resSearchFile);
        } catch (RestClientException e) {
            log.error("fail to get searched files.", e);
            searchFailed(getString(R.string.err_file_search));
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
        moveToFileDetailActivity(searchedFile.id);
    }

    private void moveToFileDetailActivity(int fileId) {
        FileDetailActivity_
                .intent(this)
                .fileId(fileId)
                .startForResult(JandiConstants.TYPE_FILE_DETAIL_REFRESH);
        getActivity().overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
        Activity activity = getActivity();
        if (activity instanceof MainTabActivity_) {
            EntityManager entityManager = ((MainTabActivity_) activity).getEntityManager();
            EventBus.getDefault().postSticky(new StickyEntityManager(entityManager));
        }

    }
}

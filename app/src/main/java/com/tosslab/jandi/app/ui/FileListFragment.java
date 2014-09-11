package com.tosslab.jandi.app.ui;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.CategorizedMenuOfFileType;
import com.tosslab.jandi.app.events.CategorizingAsEntity;
import com.tosslab.jandi.app.events.CategorizingAsOwner;
import com.tosslab.jandi.app.events.StickyEntityManager;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.lists.files.SearchedFileItemListAdapter;
import com.tosslab.jandi.app.network.JandiRestClient;
import com.tosslab.jandi.app.network.models.ReqSearchFile;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResSearchFile;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;
import org.apache.log4j.Logger;
import org.springframework.http.converter.HttpMessageNotReadableException;
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
    @ViewById(R.id.et_file_list_search_text)
    EditText editTextSearchKeyword;
    @Bean
    SearchedFileItemListAdapter mAdapter;
    @RestService
    JandiRestClient jandiRestClient;

    private String mSearchFileType  = "all";    // 서치 모드.   ALL || Images || PDFs
    private String mSearchUser      = "all";    // 사용자.     ALL || Mine || UserID
    private String mKeyword         = "";
    private int mSearchEntity       = ReqSearchFile.ALL_ENTITIES;

    private ProgressWheel mProgressWheel;
    private String mMyToken;
    private Context mContext;
    private View mFooter;       // for infinite scroll
    private InputMethodManager imm;     // 메시지 전송 버튼 클릭시, 키보드 내리기를 위한 매니저.

    @AfterViews
    void bindAdapter() {
        setHasOptionsMenu(true);

        mContext = getActivity();

        // myToken 획득
        mMyToken = JandiPreference.getMyToken(mContext);

        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(mContext);
        mProgressWheel.init();

        imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        // Empty View를 가진 ListView 설정
        View emptyView = getActivity().getLayoutInflater().inflate(R.layout.view_search_list_empty, null);
        listSearchedMessages.setEmptyView(emptyView);
        listSearchedMessages.setAdapter(mAdapter);

        // Footer 설정
        mFooter = getActivity().getLayoutInflater().inflate(R.layout.fragment_main_file_list_footer, null, false);
        mFooter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

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
        mSearchFileType = event.getServerQuery();
        doSearch();
    }

    public void onEvent(CategorizingAsOwner event) {
        mSearchUser = event.userId + "";
        doSearch();
    }

    public void onEvent(CategorizingAsEntity event) {
        mSearchEntity = event.sharedEntityId;
        doSearch();
    }

    /************************************************************
     * 검색
     ************************************************************/
    @Click(R.id.btn_file_list_search)
    void doKeywordSearch() {
        mKeyword = editTextSearchKeyword.getEditableText().toString();
        imm.hideSoftInputFromWindow(editTextSearchKeyword.getWindowToken(),0);
        doSearch();
    }

    @UiThread
    void doSearch() {
        listSearchedMessages.removeFooterView(mFooter);
        mAdapter.clearAdapter();
        doSearchInBackground();
    }

    @Background
    void doSearchInBackground() {
        jandiRestClient.setHeader(JandiConstants.AUTH_HEADER, mMyToken);

        try {
            ReqSearchFile reqSearchFile = new ReqSearchFile();
            reqSearchFile.searchType = ReqSearchFile.SEARCH_TYPE_FILE;
            reqSearchFile.fileType = mSearchFileType;
            reqSearchFile.writerId = mSearchUser;
            reqSearchFile.sharedEntityId = mSearchEntity;

            reqSearchFile.listCount = ReqSearchFile.MAX;
            reqSearchFile.startMessageId = -1;
            reqSearchFile.keyword = mKeyword;

            ResSearchFile resSearchFile = jandiRestClient.searchFile(reqSearchFile);
            searchSucceed(resSearchFile);
        } catch (RestClientException e) {
            log.error("fail to get searched files.", e);
            searchFailed(getString(R.string.err_file_search));
        } catch (HttpMessageNotReadableException e) {
            log.error("fail to get searched files.", e);
            searchFailed(getString(R.string.err_file_search));
        }
    }

    @UiThread
    void searchSucceed(ResSearchFile resSearchFile) {
        if (resSearchFile.fileCount > 0) {
            mAdapter.insert(resSearchFile);
        }

        if (resSearchFile.fileCount >= ReqSearchFile.MAX) {
            listSearchedMessages.addFooterView(mFooter);
        }

        log.debug("success to find " + resSearchFile.fileCount + " files.");
        mAdapter.notifyDataSetChanged();

        listSearchedMessages.setSelectionFromTop(1, 0);
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

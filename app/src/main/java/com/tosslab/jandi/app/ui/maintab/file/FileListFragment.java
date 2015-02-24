package com.tosslab.jandi.app.ui.maintab.file;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ListView;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.files.CategorizedMenuOfFileType;
import com.tosslab.jandi.app.events.files.CategorizingAsEntity;
import com.tosslab.jandi.app.events.files.CategorizingAsOwner;
import com.tosslab.jandi.app.events.files.RefreshOldFileEvent;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.lists.files.SearchedFileItemListAdapter;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.models.ReqSearchFile;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResSearchFile;
import com.tosslab.jandi.app.ui.filedetail.FileDetailActivity_;
import com.tosslab.jandi.app.ui.maintab.file.model.FileListModel;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.apache.log4j.Logger;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 10. 13..
 */
@EFragment(R.layout.fragment_file_list)
public class FileListFragment extends Fragment {
    private final Logger log = Logger.getLogger(FileListFragment.class);

    @ViewById(R.id.list_searched_files)
    ListView actualListView;
    @Bean
    SearchedFileItemListAdapter searchedFileItemListAdapter;
    @FragmentArg
    int entityIdForCategorizing = -1;
    @FragmentArg
    String mCurrentEntityCategorizingAccodingBy = null;

    @Bean
    FileListModel fileListModel;

    @Bean
    FileListPresenter fileListPresenter;

    private MenuItem searchMenu;   // ActionBar의 검색뷰
    private SearchQuery mSearchQuery;
    private ProgressWheel mProgressWheel;
    private Context mContext;
    private EntityManager mEntityManager;
    private InputMethodManager imm;     // 메시지 전송 버튼 클릭시, 키보드 내리기를 위한 매니저.

    /**
     * File tab 을 위한 액션바와 카테고리 선택 다이얼로그, 이벤트 전달
     */
    private int selectedTeamId;

    @AfterInject
    void init() {
        mContext = getActivity();
        mSearchQuery = new SearchQuery();
        if (entityIdForCategorizing >= 0) {
            mSearchQuery.setSharedEntity(entityIdForCategorizing);
        }
        fileListPresenter.setEntityIdForCategorizing(entityIdForCategorizing);
        fileListPresenter.setCurrentEntityCategorizingAccodingBy(mCurrentEntityCategorizingAccodingBy);
    }

    @AfterViews
    void bindAdapter() {
        setHasOptionsMenu(true);

        // myToken 획득
        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(mContext);
        mProgressWheel.init();

        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        fileListModel.retrieveEntityManager();

        // Empty View를 가진 ListView 설정
        actualListView.setAdapter(searchedFileItemListAdapter);

        selectedTeamId = JandiAccountDatabaseManager.getInstance(getActivity()).getSelectedTeamInfo().getTeamId();

    }

    public void onEvent(RefreshOldFileEvent event) {
        new GetPreviousFilesTask().execute();

        getPreviousFile();
    }

    @Background
    void getPreviousFile() {

        int justGetFilesSize;

        try {
            ReqSearchFile reqSearchFile = mSearchQuery.getRequestQuery();
            reqSearchFile.teamId = selectedTeamId;
            ResSearchFile resSearchFile = fileListModel.searchFileList(reqSearchFile);

            justGetFilesSize = resSearchFile.fileCount;
            if (justGetFilesSize > 0) {

                mSearchQuery.setNext(resSearchFile.firstIdOfReceivedList);
                insertFiles(fileListModel.descSortByCreateTime(resSearchFile.files));
            }

            if (justGetFilesSize < ReqSearchFile.MAX) {

                fileListPresenter.showWarningToast(getString(R.string.warn_no_more_files));
                searchedFileItemListAdapter.setNoMoreLoad();
            } else {
                searchedFileItemListAdapter.setReadyMore();
            }

        } catch (JandiNetworkException e) {
            log.error("fail to get searched files.", e);
            fileListPresenter.showErrorToast(getString(R.string.err_file_search));
        }

    }

    @UiThread
    void insertFiles(List<ResMessages.OriginalMessage> files) {

        searchedFileItemListAdapter.insert(files);
        searchedFileItemListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        mSearchQuery.setToFirst();
        // 서치 시작
        searchedFileItemListAdapter.clearAdapter();
        doSearch();
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.file_list_actionbar_menu, menu);

        searchMenu = menu.findItem(R.id.action_file_list_search);
//        SearchView sv = (SearchView) MenuItemCompat.getActionView(searchMenu);
        SearchView sv = ((SearchView) searchMenu.getActionView());

        MenuItemCompat.setOnActionExpandListener(searchMenu, new MenuItemCompat.OnActionExpandListener() {

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                imm.hideSoftInputFromWindow(sv.getWindowToken(), 0);
                doKeywordSearch("");
                return true;
            }
        });

        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                imm.hideSoftInputFromWindow(sv.getWindowToken(), 0);
                doKeywordSearch(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

//        setSearchViewStyle(sv);
    }

    private void setSearchViewStyle(SearchView searchView) {
        // TODO Style 에서 설정이 안되서 코드에서 수정토록...
        // 글씨 색
        int searchSrcTextId
                = getResources().getIdentifier("id/search_src_text", null, null);
        AutoCompleteTextView searchEditText
                = (AutoCompleteTextView) searchView.findViewById(searchSrcTextId);
        searchEditText.setTextColor(Color.WHITE);
        searchEditText.setHintTextColor(Color.WHITE);

        // 닫기 버튼
        int closeButtonId
                = getResources().getIdentifier("id/search_close_btn", null, null);
        ImageView closeButtonImage = (ImageView) searchView.findViewById(closeButtonId);
        closeButtonImage.setImageResource(R.drawable.jandi_actionb_remove);

        // 검색 Editbox 라인
        int searchEditId
                = getResources().getIdentifier("id/search_plate", null, null);
        View searchEditView = searchView.findViewById(searchEditId);
        searchEditView.setBackgroundResource(R.drawable.jandi_textfield_activated_holo_dark);
    }


    /**
     * *********************************************************
     * 검색
     * **********************************************************
     */
    void doKeywordSearch(String s) {
        mSearchQuery.setKeyword(s);
        searchedFileItemListAdapter.clearAdapter();
        doSearch();
    }

    public void onEvent(CategorizedMenuOfFileType event) {
        mSearchQuery.setFileType(event.getServerQuery());
        searchedFileItemListAdapter.clearAdapter();
        doSearch();
    }

    public void onEvent(CategorizingAsOwner event) {
        mSearchQuery.setWriter(event.userId);
        searchedFileItemListAdapter.clearAdapter();
        doSearch();
    }

    public void onEvent(CategorizingAsEntity event) {
        mSearchQuery.setSharedEntity(event.sharedEntityId);
        searchedFileItemListAdapter.clearAdapter();
        doSearch();
    }

    @UiThread
    void doSearch() {

        doSearchInBackground();
    }

    @Background
    void doSearchInBackground() {

        fileListPresenter.setInitLoadingViewVisible(View.VISIBLE);
        fileListPresenter.setEmptyViewVisible(View.GONE);

        try {
            ReqSearchFile reqSearchFile = mSearchQuery.getRequestQuery();
            reqSearchFile.teamId = selectedTeamId;
            ResSearchFile resSearchFile = fileListModel.searchFileList(reqSearchFile);

            updateAdapter(resSearchFile);

            fileListPresenter.setInitLoadingViewVisible(View.GONE);
            if (resSearchFile.fileCount > 0) {
                fileListPresenter.setEmptyViewVisible(View.GONE);
            } else {
                fileListPresenter.setEmptyViewVisible(View.VISIBLE);
            }

            if (fileListModel.isAllTypeFirstSearch(reqSearchFile)) {
                fileListModel.saveOriginFirstItems(selectedTeamId, resSearchFile);
            }

            searchSucceed(resSearchFile);
        } catch (JandiNetworkException e) {
            log.error("fail to get searched files.", e);
            searchFailed(R.string.err_file_search);
        }
    }

    @UiThread
    void updateAdapter(ResSearchFile resSearchFile) {
        if (resSearchFile.fileCount > 0) {
            searchedFileItemListAdapter.insert(fileListModel.descSortByCreateTime(resSearchFile.files));
            mSearchQuery.setNext(resSearchFile.firstIdOfReceivedList);
        }

    }

    @UiThread
    void searchSucceed(ResSearchFile resSearchFile) {

        if (resSearchFile.fileCount < ReqSearchFile.MAX) {
            searchedFileItemListAdapter.setNoMoreLoad();
        } else {
            searchedFileItemListAdapter.setReadyMore();
        }

        log.debug("success to find " + resSearchFile.fileCount + " files.");
        searchedFileItemListAdapter.notifyDataSetChanged();
    }

    @UiThread
    void searchFailed(int errMessageRes) {
        ColoredToast.showError(mContext, getString(errMessageRes));
    }

    @ItemClick(R.id.list_searched_files)
    void list_searched_messagesItemClicked(ResMessages.FileMessage searchedFile) {
        moveToFileDetailActivity(searchedFile.id);
    }

    private void moveToFileDetailActivity(int fileId) {
        FileDetailActivity_
                .intent(this)
                .fileId(fileId)
                .startForResult(JandiConstants.TYPE_FILE_DETAIL_REFRESH);
        getActivity().overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
    }

    /**
     * Full To Refresh 전용
     * TODO 위에 거랑 합치기.
     */
    private class GetPreviousFilesTask extends AsyncTask<Void, Void, String> {
        private int justGetFilesSize;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                ReqSearchFile reqSearchFile = mSearchQuery.getRequestQuery();
                reqSearchFile.teamId = selectedTeamId;
                ResSearchFile resSearchFile = fileListModel.searchFileList(reqSearchFile);

                justGetFilesSize = resSearchFile.fileCount;
                if (justGetFilesSize > 0) {
                    searchedFileItemListAdapter.insert(fileListModel.descSortByCreateTime(resSearchFile.files));
                    mSearchQuery.setNext(resSearchFile.firstIdOfReceivedList);
                }
                return null;
            } catch (JandiNetworkException e) {
                log.error("fail to get searched files.", e);
                return getString(R.string.err_file_search);
            } catch (HttpMessageNotReadableException e) {
                log.error("fail to get searched files.", e);
                return getString(R.string.err_file_search);
            }
        }

        @Override
        protected void onPostExecute(String errMessage) {
            if (justGetFilesSize < ReqSearchFile.MAX) {
                ColoredToast.showWarning(mContext, getString(R.string.warn_no_more_files));
                searchedFileItemListAdapter.setNoMoreLoad();
            } else {
                searchedFileItemListAdapter.setReadyMore();
            }

            if (errMessage == null) {
                // Success
                searchedFileItemListAdapter.notifyDataSetChanged();
            } else {
                ColoredToast.showError(mContext, errMessage);
            }
        }
    }

    /**
     * *********************************************************
     * 파일 검색을 담당하는 쿼리 클래스
     * **********************************************************
     */
    private class SearchQuery {
        private final String CATEGORY_ALL = "all";
        private final int LATEST_MESSAGE = -1;

        private String mSearchFileType;
        private String mSearchUser;
        private String mKeyword;
        private int mSearchEntity;
        private int mStartMessageId;

        public SearchQuery() {
            mSearchEntity = ReqSearchFile.ALL_ENTITIES;
            mStartMessageId = LATEST_MESSAGE;
            mKeyword = "";
            mSearchFileType = CATEGORY_ALL;    // 서치 모드.   ALL || Images || PDFs
            mSearchUser = CATEGORY_ALL;        // 사용자.     ALL || Mine || UserID
        }

        public void setToFirst() {
            mStartMessageId = LATEST_MESSAGE;
        }

        public void setKeyword(String keyword) {
            setToFirst();
            mKeyword = keyword;
        }

        public void setFileType(String fileType) {
            setToFirst();
            mSearchFileType = fileType;
        }

        public void setWriter(String userEntityId) {
            setToFirst();
            mSearchUser = userEntityId;
        }

        public void setSharedEntity(int entityId) {
            setToFirst();
            mSearchEntity = entityId;
        }

        public void setNext(int startMessageId) {
            mStartMessageId = startMessageId;
        }

        public ReqSearchFile getRequestQuery() {
            ReqSearchFile reqSearchFile = new ReqSearchFile();
            reqSearchFile.searchType = ReqSearchFile.SEARCH_TYPE_FILE;
            reqSearchFile.listCount = ReqSearchFile.MAX;

            reqSearchFile.fileType = mSearchFileType;
            reqSearchFile.writerId = mSearchUser;
            reqSearchFile.sharedEntityId = mSearchEntity;

            reqSearchFile.startMessageId = mStartMessageId;
            reqSearchFile.keyword = mKeyword;
            return reqSearchFile;
        }
    }
}

package com.tosslab.jandi.app.ui.maintab.file;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.files.CategorizedMenuOfFileType;
import com.tosslab.jandi.app.events.files.CategorizingAsEntity;
import com.tosslab.jandi.app.events.files.CategorizingAsOwner;
import com.tosslab.jandi.app.events.files.RefreshOldFileEvent;
import com.tosslab.jandi.app.events.search.SearchResultScrollEvent;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.lists.files.SearchedFileItemListAdapter;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.models.ReqSearchFile;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResSearchFile;
import com.tosslab.jandi.app.ui.filedetail.FileDetailActivity_;
import com.tosslab.jandi.app.ui.maintab.file.model.FileListModel;
import com.tosslab.jandi.app.ui.search.main.view.SearchActivity;
import com.tosslab.jandi.app.ui.search.main.view.SearchActivity_;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 10. 13..
 */
@EFragment(R.layout.fragment_file_list)
@OptionsMenu(R.menu.main_activity_menu)
public class FileListFragment extends Fragment implements SearchActivity.SearchSelectView {
    private final Logger log = Logger.getLogger(FileListFragment.class);

    @ViewById(R.id.list_searched_files)
    RecyclerView actualListView;

    @ViewById(R.id.layout_file_list_header)
    View headerView;

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
    private boolean isSearchLayoutFirst = true;

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
        actualListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        actualListView.setAdapter(searchedFileItemListAdapter);

        selectedTeamId = JandiAccountDatabaseManager.getInstance(getActivity()).getSelectedTeamInfo().getTeamId();

        searchedFileItemListAdapter.setOnItemClickListener((view, adapter, position) -> moveToFileDetailActivity(((SearchedFileItemListAdapter) adapter).getItem(position).id));

    }

    @OptionsItem(R.id.action_main_search)
    void onSearchOptionSelect() {
        SearchActivity_.intent(getActivity())
                .start();
    }

    public void onEvent(RefreshOldFileEvent event) {
        new GetPreviousFilesTask(getActivity()).execute();

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

    private void moveToFileDetailActivity(int fileId) {
        FileDetailActivity_
                .intent(this)
                .fileId(fileId)
                .startForResult(JandiConstants.TYPE_FILE_DETAIL_REFRESH);
        getActivity().overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
    }

    @Override
    public void onNewQuery(String query) {
        doKeywordSearch(query);
    }

    @Override
    public void onSearchHeaderReset() {
        headerView.setY((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64, getResources().getDisplayMetrics()));
    }

    @Override
    public void initSearchLayoutIfFirst() {

        if (!isSearchLayoutFirst) {
            return;
        }

        isSearchLayoutFirst = false;

        RelativeLayout.LayoutParams headerViewLayoutParams = ((RelativeLayout.LayoutParams) headerView.getLayoutParams());
        headerViewLayoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics());
//        headerViewLayoutParams.topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64, getResources().getDisplayMetrics());
        headerView.setBackgroundColor(getResources().getColor(R.color.jandi_main));

        setHeaderTextViewColor(((ViewGroup) headerView), getResources().getColor(R.color.white));
        setHeaderImageViewImage(((ViewGroup) headerView), R.drawable.jandi_arrow_down);

        final int headerMaxY = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64, getResources().getDisplayMetrics());
        final int headerMinY = 0;

        actualListView.setPadding(actualListView.getPaddingLeft(), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48 + 64.5f, getResources().getDisplayMetrics()), actualListView.getPaddingRight(), actualListView.getPaddingBottom());
        actualListView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                final int offset = (int) (dy * .66f);

                final float futureScropViewPosY = headerView.getY() - offset;

                if (futureScropViewPosY <= headerMinY) {
                    headerView.setY(headerMinY);
                } else if (futureScropViewPosY >= headerMaxY) {
                    headerView.setY(headerMaxY);
                } else {
                    headerView.setY(futureScropViewPosY);
                }

                EventBus.getDefault().post(new SearchResultScrollEvent(offset));

            }
        });
    }

    private void setHeaderImageViewImage(ViewGroup parentView, int imageResourceId) {

        if (parentView == null) {
            return;
        }

        int childCount = parentView.getChildCount();

        for (int idx = 0; idx < childCount; ++idx) {
            View child = parentView.getChildAt(idx);

            if (child instanceof ViewGroup) {
                setHeaderImageViewImage(((ViewGroup) child), imageResourceId);
            } else if (child instanceof ImageView) {
                ((ImageView) child).setImageResource(imageResourceId);
            }
        }

    }

    private void setHeaderTextViewColor(ViewGroup parentView, int color) {
        if (parentView == null) {
            return;
        }

        int childCount = parentView.getChildCount();

        for (int idx = 0; idx < childCount; ++idx) {
            View child = parentView.getChildAt(idx);

            if (child instanceof ViewGroup) {
                setHeaderTextViewColor(((ViewGroup) child), color);
            } else if (child instanceof TextView) {
                ((TextView) child).setTextColor(color);
            }
        }

    }

    private static class OldFileResult {
        private final int fileCount;
        private final List<ResMessages.OriginalMessage> files;
        private final String resultMessage;

        private OldFileResult(int fileCount, List<ResMessages.OriginalMessage> files, String resultMessage) {
            this.fileCount = fileCount;
            this.files = files;
            this.resultMessage = resultMessage;
        }
    }

    /**
     * Full To Refresh 전용
     * TODO 위에 거랑 합치기.
     */
    private class GetPreviousFilesTask extends AsyncTask<Void, Void, OldFileResult> {
        private final Context context;

        private GetPreviousFilesTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected OldFileResult doInBackground(Void... voids) {
            try {
                ReqSearchFile reqSearchFile = mSearchQuery.getRequestQuery();
                reqSearchFile.teamId = selectedTeamId;
                ResSearchFile resSearchFile = fileListModel.searchFileList(reqSearchFile);

                List<ResMessages.OriginalMessage> files;
                if (resSearchFile.fileCount > 0) {
                    files = fileListModel.descSortByCreateTime(resSearchFile.files);
                    mSearchQuery.setNext(resSearchFile.firstIdOfReceivedList);
                } else {
                    files = new ArrayList<ResMessages.OriginalMessage>();
                }
                return new OldFileResult(resSearchFile.fileCount, files, null);
            } catch (JandiNetworkException e) {
                log.error("fail to get searched files.", e);
                return new OldFileResult(0, new ArrayList<ResMessages.OriginalMessage>(), context.getString(R.string.err_file_search));
            } catch (Exception e) {
                log.error("fail to get searched files.", e);
                return new OldFileResult(0, new ArrayList<ResMessages.OriginalMessage>(), context.getString(R.string.err_file_search));
            }
        }

        @Override
        protected void onPostExecute(OldFileResult oldFileResult) {

            if (oldFileResult.fileCount > 0) {
                searchedFileItemListAdapter.insert(oldFileResult.files);
            }


            if (oldFileResult.fileCount < ReqSearchFile.MAX) {
                ColoredToast.showWarning(mContext, context.getString(R.string.warn_no_more_files));
                searchedFileItemListAdapter.setNoMoreLoad();
            } else {
                searchedFileItemListAdapter.setReadyMore();
            }

            if (TextUtils.isEmpty(oldFileResult.resultMessage)) {
                // Success
                searchedFileItemListAdapter.notifyDataSetChanged();
            } else {
                ColoredToast.showError(mContext, oldFileResult.resultMessage);
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

package com.tosslab.jandi.app.ui.maintab.file;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.FileUploadTypeDialogFragment;
import com.tosslab.jandi.app.events.entities.TopicDeleteEvent;
import com.tosslab.jandi.app.events.files.CategorizedMenuOfFileType;
import com.tosslab.jandi.app.events.files.CategorizingAsEntity;
import com.tosslab.jandi.app.events.files.CategorizingAsOwner;
import com.tosslab.jandi.app.events.files.ConfirmFileUploadEvent;
import com.tosslab.jandi.app.events.files.DeleteFileEvent;
import com.tosslab.jandi.app.events.files.FileCommentRefreshEvent;
import com.tosslab.jandi.app.events.files.RefreshOldFileEvent;
import com.tosslab.jandi.app.events.files.RequestFileUploadEvent;
import com.tosslab.jandi.app.events.files.ShareFileEvent;
import com.tosslab.jandi.app.events.network.NetworkConnectEvent;
import com.tosslab.jandi.app.events.search.SearchResultScrollEvent;
import com.tosslab.jandi.app.files.upload.FileUploadController;
import com.tosslab.jandi.app.files.upload.MainFileUploadControllerImpl;
import com.tosslab.jandi.app.lists.files.SearchedFileItemListAdapter;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqSearchFile;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResSearchFile;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.file.upload.preview.FileUploadPreviewActivity;
import com.tosslab.jandi.app.ui.file.upload.preview.FileUploadPreviewActivity_;
import com.tosslab.jandi.app.ui.filedetail.FileDetailActivity_;
import com.tosslab.jandi.app.ui.maintab.MainTabActivity;
import com.tosslab.jandi.app.ui.maintab.file.model.FileListModel;
import com.tosslab.jandi.app.ui.search.main.view.SearchActivity;
import com.tosslab.jandi.app.ui.search.main.view.SearchActivity_;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;
import com.tosslab.jandi.app.views.decoration.SimpleDividerItemDecoration;
import com.tosslab.jandi.app.views.listeners.ListScroller;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.constant.property.ScreenViewProperty;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by justinygchoi on 2014. 10. 13..
 */
@EFragment(R.layout.fragment_file_list)
public class FileListFragment extends Fragment
        implements SearchActivity.SearchSelectView, ListScroller {

    public static final String KEY_COMMENT_COUNT = "comment_count";
    public static final String KEY_FILE_ID = "file_id";
    @ViewById(R.id.list_searched_files)
    RecyclerView lvSearchFiles;

    @ViewById(R.id.layout_file_list_header)
    View headerView;

    @Bean
    SearchedFileItemListAdapter searchedFileItemListAdapter;
    @FragmentArg
    long entityIdForCategorizing = -1;
    @FragmentArg
    String mCurrentEntityCategorizingAccodingBy = null;

    @Bean
    FileListModel fileListModel;

    @Bean
    FileListPresenter fileListPresenter;

    @Bean(value = MainFileUploadControllerImpl.class)
    FileUploadController filePickerViewModel;

    @SystemService
    InputMethodManager inputMethodManager;

    private SearchQuery mSearchQuery;

    /**
     * File tab 을 위한 액션바와 카테고리 선택 다이얼로그, 이벤트 전달
     */
    private long selectedTeamId;
    private boolean isSearchLayoutFirst = true;
    private boolean isForeground;
    private PublishSubject<Integer> initSearchSubject;
    private SearchActivity.OnSearchItemSelect onSearchItemSelect;
    private SearchActivity.OnSearchText onSearchText;

    @AfterInject
    void init() {
        mSearchQuery = new SearchQuery();
        if (entityIdForCategorizing >= 0) {
            mSearchQuery.setSharedEntity(entityIdForCategorizing);
            mCurrentEntityCategorizingAccodingBy = TeamInfoLoader.getInstance()
                    .getName(entityIdForCategorizing);
        }
        fileListPresenter.setEntityIdForCategorizing(entityIdForCategorizing);
        fileListPresenter.setCurrentEntityCategorizingAccodingBy(mCurrentEntityCategorizingAccodingBy);

        initSearchSubject = PublishSubject.create();
        initSearchSubject.observeOn(Schedulers.io())
                .subscribe(index -> {
                    mSearchQuery.setToFirst();
                    searchedFileItemListAdapter.clearAdapter();
                    doSearchInBackground(index);
                }, throwable -> LogUtil.d("Search Fail : " + throwable.getMessage()));
    }

    @AfterViews
    void bindAdapter() {
        if (getActivity() instanceof SearchActivity) {
            AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                    .event(Event.ScreenView)
                    .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                    .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                    .property(PropertyKey.ScreenView, ScreenViewProperty.FILE_SEARCH)
                    .build());

            AnalyticsUtil.sendScreenName(AnalyticsValue.Screen.FilesSearch);
        }

        setHasOptionsMenu(true);

        // Empty View를 가진 ListView 설정
        lvSearchFiles.setLayoutManager(new LinearLayoutManager(getActivity()));
        lvSearchFiles.addItemDecoration(new SimpleDividerItemDecoration());
        lvSearchFiles.setAdapter(searchedFileItemListAdapter);

        ResAccountInfo.UserTeam selectedTeamInfo = AccountRepository.getRepository().getSelectedTeamInfo();
        // TODO Why null?
        if (selectedTeamInfo == null) {
            return;
        }

        selectedTeamId = selectedTeamInfo.getTeamId();

        searchedFileItemListAdapter.setOnRecyclerItemClickListener((view, adapter, position) -> {
            moveToFileDetailActivity(((SearchedFileItemListAdapter) adapter).getItem(position)
                    .id, mSearchQuery.mSearchEntity);
            if (onSearchItemSelect != null) {
                onSearchItemSelect.onSearchItemSelect();
            }

            AnalyticsValue.Action action;
            if (isDefault(mSearchQuery)) {
                action = AnalyticsValue.Action.ChooseFile;
            } else {
                action = AnalyticsValue.Action.ChooseFilteredFile;
            }

            if (getActivity() instanceof SearchActivity) {
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FilesSearch, action);
            } else {
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FilesTab, action);
            }
        });

        resetFilterLayoutPosition();

        if (isInSearchActivity() && isSearchLayoutFirst) {
            initSearchLayoutIfFirst();
        }

        initSearchSubject.onNext(-1);

    }

    private boolean isDefault(SearchQuery mSearchQuery) {
        return mSearchQuery.mSearchEntity == ReqSearchFile.ALL_ENTITIES
                && mSearchQuery.mSearchFileType.equals("all")
                && mSearchQuery.mSearchUser.equals("all");
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        menu.clear();

        FragmentActivity activity = getActivity();
        if (activity instanceof MainTabActivity) {
            activity.getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        }

    }

    @OptionsItem(R.id.action_main_search)
    void onSearchOptionSelect() {
        SearchActivity_.intent(getActivity())
                .isFromFiles(true)
                .start();
    }

    public void onEvent(RefreshOldFileEvent event) {
        if (isForeground) {
            getPreviousFile();
        }
    }

    public void onEvent(ShareFileEvent event) {
        if (isInSearchActivity()) {
            return;
        }

        if (event.getTeamId() != AccountRepository.getRepository().getSelectedTeamId()) {
            return;
        }
        int itemCount = searchedFileItemListAdapter.getItemCount();
        initSearchSubject.onNext(itemCount);
    }

    @Background
    void getPreviousFile() {

        int justGetFilesSize;

        if (!NetworkCheckUtil.isConnected()) {
            return;
        }

        fileListPresenter.showMoreProgressBar();
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

                fileListPresenter.showWarningToast(JandiApplication.getContext().getString(R.string.warn_no_more_files));
                searchedFileItemListAdapter.setNoMoreLoad();
            } else {
                searchedFileItemListAdapter.setReadyMore();
            }

        } catch (RetrofitException e) {
            e.printStackTrace();
            LogUtil.e("fail to get searched files.", e);
            searchFailed(R.string.err_file_search);
        } catch (Exception e) {
            e.printStackTrace();
            searchFailed(R.string.err_file_search);
        } finally {
            fileListPresenter.dismissMoreProgressBar();
        }

    }

    @UiThread
    void insertFiles(List<ResMessages.OriginalMessage> files) {

        searchedFileItemListAdapter.insert(files);
        searchedFileItemListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        isForeground = true;
    }

    @Override
    public void onPause() {
        isForeground = false;
        super.onPause();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    void doKeywordSearch(String s) {
        mSearchQuery.setKeyword(s);
        searchedFileItemListAdapter.clearAdapter();
        initSearchSubject.onNext(-1);
        onSearchHeaderReset();
    }

    public void onEvent(CategorizedMenuOfFileType event) {
        if (!isForeground) {
            return;
        }
//        Log.d("INFO", "event setFileType" + event.getServerQuery());
        mSearchQuery.setFileType(event.getServerQuery());
        if (onSearchText != null) {
            mSearchQuery.setKeyword(onSearchText.getSearchText());
        }
        searchedFileItemListAdapter.clearAdapter();
        initSearchSubject.onNext(-1);
        onSearchHeaderReset();
    }

    public void onEvent(CategorizingAsOwner event) {
        if (!isForeground) {
            return;
        }
//        Log.d("INFO", "event setOwnerType");
        mSearchQuery.setWriter(event.userId);
        if (onSearchText != null) {
            mSearchQuery.setKeyword(onSearchText.getSearchText());
        }
        searchedFileItemListAdapter.clearAdapter();
        initSearchSubject.onNext(-1);
        onSearchHeaderReset();
    }

    public void onEvent(CategorizingAsEntity event) {
        if (!isForeground) {
            return;
        }
//        Log.d("INFO", "event setEntityType");
        mSearchQuery.setSharedEntity(event.sharedEntityId);
        if (onSearchText != null) {
            mSearchQuery.setKeyword(onSearchText.getSearchText());
        }
        searchedFileItemListAdapter.clearAdapter();
        initSearchSubject.onNext(-1);
        onSearchHeaderReset();
    }

    public void onEventMainThread(ConfirmFileUploadEvent event) {

        if (!isForeground) {
            return;
        }

        filePickerViewModel.startUpload(getActivity(), event.title, event.entityId, event.realFilePath, event.comment);

    }

    public void onEvent(DeleteFileEvent event) {

        if (isInSearchActivity()) {
            return;
        }

        if (event.getTeamId() != AccountRepository.getRepository().getSelectedTeamId()) {
            return;
        }

        long fileId = event.getId();
        int positionByFileId = searchedFileItemListAdapter.findPositionByFileId(fileId);
        if (positionByFileId >= 0) {
            removeItem(positionByFileId);
        }
    }

    public void onEvent(TopicDeleteEvent event) {
        if (isInSearchActivity()) {
            return;
        }

        if (event.getTeamId() != AccountRepository.getRepository().getSelectedTeamId()) {
            return;
        }
        // 토픽이 삭제되거나 나간 경우 해당 토픽의 파일 접근 여부를 알 수 없으므로
        // 리로드하도록 처리함
        int itemCount = searchedFileItemListAdapter.getItemCount();
        initSearchSubject.onNext(itemCount);
    }

    public void onEvent(NetworkConnectEvent event) {
        if (isInSearchActivity()) {
            return;
        }

        if (event.isConnected() && searchedFileItemListAdapter.getItemCount() <= 0) {
            initSearchSubject.onNext(-1);
        }
    }

    private boolean isInSearchActivity() {
        return getActivity() instanceof SearchActivity;
    }

    @UiThread
    void removeItem(int position) {
        searchedFileItemListAdapter.remove(position);
        searchedFileItemListAdapter.notifyDataSetChanged();
        if (searchedFileItemListAdapter.getItemCount() <= 0) {
            if (fileListModel.isDefaultSearchQueryIgnoreMessageId(mSearchQuery.getRequestQuery())) {
                fileListPresenter.setEmptyViewVisible(View.VISIBLE);
                fileListPresenter.setSearchEmptryViewVisible(View.GONE);
            } else {
                fileListPresenter.setEmptyViewVisible(View.GONE);
                fileListPresenter.setSearchEmptryViewVisible(View.VISIBLE);
            }
        }
    }

    void doSearchInBackground(int requestCount) {

        fileListPresenter.setInitLoadingViewVisible(View.VISIBLE);
        fileListPresenter.setEmptyViewVisible(View.GONE);
        fileListPresenter.setSearchEmptryViewVisible(View.GONE);

        if (!NetworkCheckUtil.isConnected()) {
            fileListPresenter.setInitLoadingViewVisible(View.GONE);
            fileListPresenter.setEmptyViewVisible(View.GONE);
            fileListPresenter.setSearchEmptryViewVisible(View.VISIBLE);
            return;
        }

        try {
            ReqSearchFile reqSearchFile = mSearchQuery.getRequestQuery();
            reqSearchFile.teamId = selectedTeamId;
            if (requestCount > ReqSearchFile.MAX) {
                reqSearchFile.listCount = requestCount;
            }
            ResSearchFile resSearchFile = fileListModel.searchFileList(reqSearchFile);

            String keyword = reqSearchFile.keyword;

            fileListModel.trackFileKeywordSearchSuccess(keyword);

            if (resSearchFile.fileCount < reqSearchFile.listCount) {
                searchedFileItemListAdapter.setNoMoreLoad();
            } else {
                searchedFileItemListAdapter.setReadyMore();
            }

            updateAdapter(resSearchFile);

            fileListPresenter.setInitLoadingViewVisible(View.GONE);
            if (fileListModel.isDefaultSearchQuery(mSearchQuery.getRequestQuery())) {
                if (resSearchFile.fileCount > 0) {
                    fileListPresenter.setEmptyViewVisible(View.GONE);
                } else {
                    fileListPresenter.setEmptyViewVisible(View.VISIBLE);
                }
                fileListPresenter.setSearchEmptryViewVisible(View.GONE);
            } else {
                if (resSearchFile.fileCount > 0) {
                    fileListPresenter.setSearchEmptryViewVisible(View.GONE);
                } else {
                    fileListPresenter.setSearchEmptryViewVisible(View.VISIBLE);
                }
                fileListPresenter.setEmptyViewVisible(View.GONE);
            }

            if (fileListModel.isAllTypeFirstSearch(reqSearchFile)) {
                fileListModel.saveOriginFirstItems(selectedTeamId, resSearchFile);
            }

            searchSucceed(resSearchFile);
        } catch (RetrofitException e) {
            int errorCode = e.getStatusCode();
            fileListModel.trackFileKeywordSearchFail(errorCode);
            e.printStackTrace();
            LogUtil.e("fail to get searched files.", e);
            searchFailed(R.string.err_file_search);
        } catch (Exception e) {
            e.printStackTrace();
            fileListModel.trackFileKeywordSearchFail(-1);
            searchFailed(R.string.err_file_search);
        }
    }

    @Click(value = {R.id.btn_file_empty_upload, R.id.iv_file_empty_upload})
    void onUploadClick() {
        DialogFragment fileUploadTypeDialog = new FileUploadTypeDialogFragment();
        fileUploadTypeDialog.show(getFragmentManager(), "dialog");
    }

    @Click(R.id.ly_file_list_where)
    void onEntityClick() {
        fileListPresenter.showEntityDialog();
    }

    @Click(R.id.ly_file_list_whom)
    void onUserClick() {
        fileListPresenter.showUsersDialog();
    }

    @Click(R.id.ly_file_list_type)
    void onFileTypeClick() {
        fileListPresenter.showFileTypeDialog();
    }

    public void onEvent(RequestFileUploadEvent event) {
        if (!isForeground) {
            return;
        }
        ((BaseAppCompatActivity) getActivity()).setNeedUnLockPassCode(false);
        filePickerViewModel.selectFileSelector(event.type, FileListFragment.this, mSearchQuery.mSearchEntity);
    }

    @OnActivityResult(FileUploadController.TYPE_UPLOAD_GALLERY)
    void onGalleryActivityResult(int resultCode, Intent intent) {
        // Do Nothing
    }

    @OnActivityResult(FileUploadController.TYPE_UPLOAD_TAKE_PHOTO)
    void onCameraActivityResult(int resultCode, Intent intent) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        List<String> filePath = filePickerViewModel.getFilePath(getActivity(), FileUploadController.TYPE_UPLOAD_TAKE_PHOTO, intent);
        if (filePath != null && filePath.size() > 0) {
            FileUploadPreviewActivity_.intent(this)
                    .singleUpload(true)
                    .realFilePathList(new ArrayList<>(filePath))
                    .startForResult(FileUploadPreviewActivity.REQUEST_CODE);
        }

    }

    @OnActivityResult(FileUploadController.TYPE_UPLOAD_EXPLORER)
    void onExplorerActivityResult(int resultCode, Intent intent) {

        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        List<String> filePath = filePickerViewModel.getFilePath(getActivity(), FileUploadController.TYPE_UPLOAD_EXPLORER, intent);
        if (filePath != null && filePath.size() > 0) {
            FileUploadPreviewActivity_.intent(this)
                    .singleUpload(true)
                    .realFilePathList(new ArrayList<>(filePath))
                    .startForResult(FileUploadPreviewActivity.REQUEST_CODE);
        }
    }

    @OnActivityResult(JandiConstants.TYPE_FILE_DETAIL_REFRESH)
    void onFileDetailShowResult(int resultCode, Intent intent) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        int fileId = intent.getIntExtra(KEY_FILE_ID, -1);
        int commentCount = intent.getIntExtra(KEY_COMMENT_COUNT, -1);
        if (fileId <= 0 || commentCount < 0) {
            return;
        }

        int position = searchedFileItemListAdapter.findPositionByFileId(fileId);
        if (position < 0) {
            return;
        }

        ResMessages.FileMessage item = searchedFileItemListAdapter.getItem(position);
        if (item != null) {
            item.commentCount = commentCount;
            searchedFileItemListAdapter.notifyDataSetChanged();
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

        LogUtil.d("success to find " + resSearchFile.fileCount + " files.");
        searchedFileItemListAdapter.notifyDataSetChanged();
    }

    @UiThread
    void searchFailed(int errMessageRes) {
        FragmentActivity activity = getActivity();
        if (activity != null && !(activity.isFinishing())) {
            ColoredToast.showError(activity.getString(errMessageRes));
        }
    }

    private void moveToFileDetailActivity(long fileId, long mSearchEntity) {
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
        if (!isForeground) {
            return;
        }

        resetFilterLayoutPosition();
    }

    private void resetFilterLayoutPosition() {
        if (!isInSearchActivity()) {
            return;
        }
        if (headerView == null) {
            return;
        }
        int offset = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 64, getResources().getDisplayMetrics());
        headerView.setY(offset);

        EventBus.getDefault().post(
                new SearchResultScrollEvent(FileListFragment.this.getClass(), -offset));
    }

    @Override
    public void initSearchLayoutIfFirst() {

        if (!isSearchLayoutFirst || headerView == null || lvSearchFiles == null) {
            return;
        }

        isSearchLayoutFirst = false;

        RelativeLayout.LayoutParams headerViewLayoutParams = ((RelativeLayout.LayoutParams) headerView.getLayoutParams());
        headerViewLayoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics());
//        headerViewLayoutParams.topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64, getResources().getDisplayMetrics());
        headerView.setBackgroundColor(getResources().getColor(R.color.jandi_primary_color));

        setHeaderTextViewColor(((ViewGroup) headerView), getResources().getColor(R.color.white));
        setHeaderImageViewImage(((ViewGroup) headerView), R.drawable.file_arrow_down);

        final int headerMaxY = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64, getResources().getDisplayMetrics());
        final int headerMinY = 0;

        int paddingTop = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48 + 64.5f, getResources().getDisplayMetrics());
        lvSearchFiles.setPadding(lvSearchFiles.getPaddingLeft(), paddingTop, lvSearchFiles.getPaddingRight(), lvSearchFiles.getPaddingBottom());

        View uploadEmptyView = getActivity().findViewById(R.id.layout_file_list_empty);
        View searchEmptyView = getActivity().findViewById(R.id.layout_file_list_search_empty);
        View loadingView = getActivity().findViewById(R.id.layout_file_list_loading);

        RelativeLayout.LayoutParams uploadLayoutParams = (RelativeLayout.LayoutParams) uploadEmptyView.getLayoutParams();
        uploadLayoutParams.topMargin = paddingTop;
        uploadEmptyView.setLayoutParams(uploadLayoutParams);
        RelativeLayout.LayoutParams searchLayoutParams = (RelativeLayout.LayoutParams) searchEmptyView.getLayoutParams();
        searchLayoutParams.topMargin = paddingTop;
        searchEmptyView.setLayoutParams(searchLayoutParams);
        RelativeLayout.LayoutParams loadingLayoutParams = (RelativeLayout.LayoutParams) loadingView.getLayoutParams();
        loadingLayoutParams.topMargin = paddingTop;
        loadingView.setLayoutParams(loadingLayoutParams);

        lvSearchFiles.setOnScrollListener(new RecyclerView.OnScrollListener() {
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

                EventBus.getDefault().post(new SearchResultScrollEvent(FileListFragment.this.getClass(),
                        offset));

            }
        });
    }

    @Override
    public void setOnSearchItemSelect(SearchActivity.OnSearchItemSelect onSearchItemSelect) {
        this.onSearchItemSelect = onSearchItemSelect;
    }

    @Override
    public void setOnSearchText(SearchActivity.OnSearchText onSearchText) {
        this.onSearchText = onSearchText;
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
            } else if (child instanceof TextView) {

                ((TextView) child).setCompoundDrawablesWithIntrinsicBounds(0, 0, imageResourceId, 0);
            }
        }

    }

    public void onEvent(FileCommentRefreshEvent event) {
        if (searchedFileItemListAdapter == null) {
            return;
        }
        long fileId = event.getFileId();
        int position = searchedFileItemListAdapter.findPositionByFileId(fileId);
        if (position < 0) {
            return;
        }

        boolean added = event.isAdded();

        ResMessages.FileMessage item = searchedFileItemListAdapter.getItem(position);
        if (added) {
            item.commentCount++;
        } else {
            item.commentCount--;
        }

        justRefresh();

    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void justRefresh() {
        searchedFileItemListAdapter.notifyDataSetChanged();
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

    @Override
    public void scrollToTop() {
        lvSearchFiles.scrollToPosition(0);
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
     * *********************************************************
     * 파일 검색을 담당하는 쿼리 클래스
     * **********************************************************
     */
    private static class SearchQuery {
        private final String CATEGORY_ALL = "all";
        private final int LATEST_MESSAGE = -1;

        private String mSearchFileType;
        private String mSearchUser;
        private String mKeyword;
        private long mSearchEntity;
        private long mStartMessageId;

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

        public void setSharedEntity(long entityId) {
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

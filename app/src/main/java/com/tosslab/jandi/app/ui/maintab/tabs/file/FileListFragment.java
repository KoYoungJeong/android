package com.tosslab.jandi.app.ui.maintab.tabs.file;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.tosslab.jandi.app.Henson;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.FileUploadTypeDialogFragment;
import com.tosslab.jandi.app.events.entities.TopicDeleteEvent;
import com.tosslab.jandi.app.events.files.CategorizedMenuOfFileType;
import com.tosslab.jandi.app.events.files.CategorizingAsEntity;
import com.tosslab.jandi.app.events.files.CategorizingAsOwner;
import com.tosslab.jandi.app.events.files.ConfirmFileUploadEvent;
import com.tosslab.jandi.app.events.files.DeleteFileEvent;
import com.tosslab.jandi.app.events.files.FileCreatedEvent;
import com.tosslab.jandi.app.events.files.FileUploadFinishEvent;
import com.tosslab.jandi.app.events.files.RefreshOldFileEvent;
import com.tosslab.jandi.app.events.files.RequestFileUploadEvent;
import com.tosslab.jandi.app.events.files.ShareFileEvent;
import com.tosslab.jandi.app.events.files.UnshareFileEvent;
import com.tosslab.jandi.app.events.network.NetworkConnectEvent;
import com.tosslab.jandi.app.files.upload.FileUploadController;
import com.tosslab.jandi.app.files.upload.MainFileUploadControllerImpl;
import com.tosslab.jandi.app.network.models.ReqSearchFile;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResSearchFile;
import com.tosslab.jandi.app.network.models.search.ResSearch;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.base.BaseLazyFragment;
import com.tosslab.jandi.app.ui.carousel.CarouselViewerActivity;
import com.tosslab.jandi.app.ui.file.upload.preview.FileUploadPreviewActivity;
import com.tosslab.jandi.app.ui.maintab.MainTabActivity;
import com.tosslab.jandi.app.ui.maintab.tabs.file.adapter.SearchedFilesAdapter;
import com.tosslab.jandi.app.ui.maintab.tabs.file.adapter.SearchedFilesAdapterView;
import com.tosslab.jandi.app.ui.maintab.tabs.file.controller.SearchSelectorViewController;
import com.tosslab.jandi.app.ui.maintab.tabs.file.dagger.DaggerFileListComponent;
import com.tosslab.jandi.app.ui.maintab.tabs.file.dagger.FileListModule;
import com.tosslab.jandi.app.ui.maintab.tabs.file.presenter.FileListPresenter;
import com.tosslab.jandi.app.ui.search.file.view.FileSearchActivity;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.analytics.sprinkler.ScreenViewProperty;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrScreenView;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.views.decoration.SimpleDividerItemDecoration;
import com.tosslab.jandi.app.views.listeners.ListScroller;
import com.tosslab.jandi.app.views.listeners.SimpleEndAnimationListener;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * Created by tee on 16. 6. 28..
 */
public class FileListFragment extends BaseLazyFragment implements FileListPresenter.View,
        FileSearchActivity.SearchSelectView, ListScroller {

    public static final String KEY_COMMENT_COUNT = "comment_count";
    public static final String KEY_FILE_ID = "file_id";
    public static final String PARAM_ENTITY_ID = "param_entity_id";
    public static final String KEY_FILE_DELETED = "file_deleted";

    @Inject
    FileListPresenter fileListPresenter;

    @Bind(R.id.list_searched_files)
    RecyclerView lvSearchFiles;

    @Bind(R.id.layout_file_list_header)
    View headerView;

    @Bind(R.id.tv_file_list_where)
    TextView tvFileListWhere;

    @Bind(R.id.tv_file_list_whom)
    TextView tvFileListWhom;

    @Bind(R.id.tv_file_list_type)
    TextView tvFileListType;

    @Bind(R.id.vg_file_list_empty)
    View uploadEmptyView;

    @Bind(R.id.vg_file_list_search_empty)
    View searchEmptyView;

    @Bind(R.id.vg_file_list_loading)
    View initLoadingView;

    @Bind(R.id.progress_file_list)
    ProgressBar moreLoadingProgressBar;
    @Nullable
    @InjectExtra
    long entityId = -1;
    @Nullable
    @InjectExtra
    long writerId = -1;
    @Nullable
    @InjectExtra
    String fileType = "all";
    private ProgressWheel progressWheel;
    private SearchSelectorViewController searchSelectorViewController;
    private FileUploadController filePickerViewModel;
    private SearchedFilesAdapterView searchedFilesAdapterView;
    private FileSearchActivity.OnSearchItemSelect onSearchItemSelect;
    private FileSearchActivity.OnSearchText onSearchText;
    private boolean isSearchLayoutFirst = true;
    private boolean focused = true; // maintab 에서 현재 화면인지 체크하기 위함

    public static FileListFragment create(Context context, long entityId, long writerId, String fileType) {
        Bundle bundle = new Bundle();
        bundle.putString("fileType", fileType);
        bundle.putLong("entityId", entityId);
        bundle.putLong("writerId", writerId);
        return (FileListFragment) Fragment.instantiate(context, FileListFragment.class.getName(), bundle);
    }

    public void setOnSearchItemSelect(FileSearchActivity.OnSearchItemSelect onSearchItemSelect) {
        this.onSearchItemSelect = onSearchItemSelect;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file_list, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (isInSearchActivity()) {
            onLazyLoad(savedInstanceState);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    protected void onLazyLoad(Bundle savedInstanceState) {
        super.onLazyLoad(savedInstanceState);
        Bundle bundle = this.getArguments();
        Dart.inject(this, bundle);

        DaggerFileListComponent.builder()
                .fileListModule(new FileListModule(this, entityId, isInSearchActivity()))
                .build()
                .inject(this);

        filePickerViewModel = new MainFileUploadControllerImpl();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        searchSelectorViewController = new SearchSelectorViewController(
                getContext(), tvFileListWhere, tvFileListWhom, tvFileListType);

        String entityName = TeamInfoLoader.getInstance().getName(entityId);

        searchSelectorViewController.setCurrentEntityNameText(entityName);

        setListView();

        setHasOptionsMenu(true);

        if (isInSearchActivity() && isSearchLayoutFirst) {
//            initSearchLayoutIfFirst();
        }

        if (entityId > 0) {
            fileListPresenter.onEntitySelection(entityId, "");
            searchSelectorViewController.setCurrentEntity(entityId);
        }

        if (writerId > 0) {
            fileListPresenter.onMemberSelection(writerId, "");
            searchSelectorViewController.setCurrentMember(writerId);
        }

        if (!TextUtils.equals(fileType, "all")) {
            fileListPresenter.onFileTypeSelection(fileType, "");
            searchSelectorViewController.setCurrentFileType(fileType);
        }
    }

    private void setListViewScroll() {
        if (getActivity() instanceof MainTabActivity) {
            MainTabActivity activity = (MainTabActivity) getActivity();
            lvSearchFiles.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (dy > 0) {
                        activity.setTabLayoutVisible(false);
                    } else {
                        activity.setTabLayoutVisible(true);
                    }
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        if (isLoadedAll()) {
            fileListPresenter.onDestory();
        }
        super.onDestroy();
    }


    @OnClick(R.id.ly_file_list_where)
    void onEntityClick() {
        searchSelectorViewController.showEntityDialog();
    }

    @OnClick(R.id.ly_file_list_whom)
    void onUserClick() {
        searchSelectorViewController.showUsersDialog();
    }

    @OnClick(R.id.ly_file_list_type)
    void onFileTypeClick() {
        searchSelectorViewController.showFileTypeDialog();
    }

    @OnClick(value = {R.id.btn_file_empty_upload, R.id.iv_file_empty_upload})
    void onUploadClick() {
        DialogFragment fileUploadTypeDialog = new FileUploadTypeDialogFragment();
        fileUploadTypeDialog.show(getFragmentManager(), "dialog");
    }

    private void setListView() {
        if (getActivity() instanceof FileSearchActivity) {
            SprinklrScreenView.sendLog(ScreenViewProperty.FILE_SEARCH);
            AnalyticsUtil.sendScreenName(AnalyticsValue.Screen.FilesSearch);
        }

        // Empty View를 가진 ListView 설정
        lvSearchFiles.setLayoutManager(new LinearLayoutManager(getActivity()));
        lvSearchFiles.addItemDecoration(new SimpleDividerItemDecoration());
        SearchedFilesAdapter searchedFilesAdapter = new SearchedFilesAdapter();
        lvSearchFiles.setAdapter(searchedFilesAdapter);
        searchedFilesAdapter.setOnRecyclerItemClickListener((view, adapter, position) -> {
            moveToFileDetailActivity(searchedFilesAdapter.getItem(position).getFile());

            if (onSearchItemSelect != null) {
                onSearchItemSelect.onSearchItemSelect();
            }

            AnalyticsValue.Action action;

            if (fileListPresenter.isDefaultSeachQuery()) {
                action = AnalyticsValue.Action.ChooseFile;
            } else {
                action = AnalyticsValue.Action.ChooseFilteredFile;
            }

            if (getActivity() instanceof FileSearchActivity) {
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FilesSearch, action);
            } else {
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FilesTab, action);
            }

        });
        searchedFilesAdapterView = searchedFilesAdapter;
        fileListPresenter.setSearchedFilesAdapterModel(searchedFilesAdapter);
        setListViewScroll();
    }

    private void moveToFileDetailActivity(ResSearch.File file) {
        if (file.getIcon().startsWith("image")) {
            fileListPresenter.getImageDetail(file.getId());
        } else {
            startActivityForResult(Henson.with(getActivity())
                    .gotoFileDetailActivity()
                    .fileId(file.getId())
                    .build(), JandiConstants.TYPE_FILE_DETAIL_REFRESH);
            getActivity().overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
        }
    }

    @Override
    public void moveToCarousel(ResMessages.FileMessage fileMessage) {
        Intent intent = CarouselViewerActivity.getImageViewerIntent(getActivity(), fileMessage)
                .build();
        startActivityForResult(intent, JandiConstants.TYPE_FILE_IMAGE_VIEW_REFRESH);
    }

    @Override
    public void setSearchEmptryViewVisible(int visible) {
        searchEmptyView.setVisibility(visible);
    }

    @Override
    public void dismissMoreProgressBar() {
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_to_bottom);

        animation.setAnimationListener(new SimpleEndAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                moreLoadingProgressBar.setVisibility(View.GONE);
            }
        });

        moreLoadingProgressBar.setAnimation(animation);
        animation.startNow();
    }

    @Override
    public void setInitLoadingViewVisible(int visible) {
        initLoadingView.setVisibility(visible);
    }

    @Override
    public void setEmptyViewVisible(int visible) {
        uploadEmptyView.setVisibility(visible);
    }

    @Override
    public void showWarningToast(String message) {
        if (focused) {
            ColoredToast.showWarning(message);
        }
    }

    @Override
    public void showMoreProgressBar() {
        moreLoadingProgressBar.setVisibility(View.VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_bottom);
        moreLoadingProgressBar.setAnimation(animation);
        animation.startNow();
    }

    @Override
    public void clearListView() {
        searchedFilesAdapterView.clearListView();
    }

    @Override
    public void searchFailed(int errMessageRes) {
        FragmentActivity activity = getActivity();
        if (activity != null
                && !(activity.isFinishing())
                && focused) {
            ColoredToast.showError(activity.getString(errMessageRes));
        }
    }

    @Override
    public void searchSucceed(ResSearchFile resSearchFile) {
        if (resSearchFile.fileCount < ReqSearchFile.MAX) {
            fileListPresenter.setListNoMoreLoad();
        } else {
            fileListPresenter.setListReadyLoadMore();
        }
        LogUtil.d("success to find " + resSearchFile.fileCount + " files.");
        searchedFilesAdapterView.refreshListView();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_main_search) {
            fileListPresenter.onMoveFileSearch();
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isInSearchActivity() {
        return getActivity() instanceof FileSearchActivity;
    }

    public void initSearchLayoutIfFirst() {
        if (!isSearchLayoutFirst || headerView == null || lvSearchFiles == null) {
            return;
        }

        isSearchLayoutFirst = false;

        RelativeLayout.LayoutParams headerViewLayoutParams = ((RelativeLayout.LayoutParams) headerView.getLayoutParams());
        headerViewLayoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics());
        headerView.setBackgroundColor(getResources().getColor(R.color.jandi_primary_color));

        setHeaderTextViewColor(((ViewGroup) headerView), getResources().getColor(R.color.white));
        setHeaderImageViewImage(((ViewGroup) headerView), R.drawable.file_arrow_down);

        final int headerMaxY = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64, getResources().getDisplayMetrics());
        final int headerMinY = 0;

        int paddingTop = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48 + 64.5f, getResources().getDisplayMetrics());
        lvSearchFiles.setPadding(lvSearchFiles.getPaddingLeft(), paddingTop, lvSearchFiles.getPaddingRight(), lvSearchFiles.getPaddingBottom());

        View uploadEmptyView = getActivity().findViewById(R.id.vg_file_list_empty);
        View searchEmptyView = getActivity().findViewById(R.id.vg_file_list_search_empty);
        View loadingView = getActivity().findViewById(R.id.vg_file_list_loading);

        RelativeLayout.LayoutParams uploadLayoutParams = (RelativeLayout.LayoutParams) uploadEmptyView.getLayoutParams();
        uploadLayoutParams.topMargin = paddingTop;
        uploadEmptyView.setLayoutParams(uploadLayoutParams);
        RelativeLayout.LayoutParams searchLayoutParams = (RelativeLayout.LayoutParams) searchEmptyView.getLayoutParams();
        searchLayoutParams.topMargin = paddingTop;
        searchEmptyView.setLayoutParams(searchLayoutParams);
        RelativeLayout.LayoutParams loadingLayoutParams = (RelativeLayout.LayoutParams) loadingView.getLayoutParams();
        loadingLayoutParams.topMargin = paddingTop;
        loadingView.setLayoutParams(loadingLayoutParams);
        loadingView.setVisibility(View.GONE);

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

    @Override
    public void justRefresh() {
        searchedFilesAdapterView.refreshListView();
    }

    @Override
    public void showProgress() {
        if (progressWheel == null) {
            progressWheel = new ProgressWheel(getActivity());
        }

        if (!progressWheel.isShowing()) {
            progressWheel.show();
        }
    }

    @Override
    public void dismissProgress() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    @Override
    public void moveFileSearch(long entity, long writer, String type) {
        FileSearchActivity.start(getActivity(), entity, writer, type);
    }

    @Override
    public void scrollToTop() {
        lvSearchFiles.scrollToPosition(0);
    }

    @Override
    public void onNewQuery(String query) {
        fileListPresenter.onNewQuery(query);
    }

    @Override
    public void setOnSearchText(FileSearchActivity.OnSearchText onSearchText) {
        this.onSearchText = onSearchText;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case FileUploadController.TYPE_UPLOAD_GALLERY:
                // Do Nothing
                break;
            case FileUploadController.TYPE_UPLOAD_TAKE_PHOTO:
                onCameraActivityResult(resultCode, data);
                break;
            case FileUploadController.TYPE_UPLOAD_EXPLORER:
                onExplorerActivityResult(resultCode, data);
                break;
            case JandiConstants.TYPE_FILE_DETAIL_REFRESH:
                onFileDetailShowResult(resultCode, data);
                break;
            case JandiConstants.TYPE_FILE_IMAGE_VIEW_REFRESH:
                onFileImageViewResult(resultCode, data);
                break;
        }
    }

    private void onFileImageViewResult(int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        boolean isDeleted = data.getBooleanExtra(KEY_FILE_DELETED, false);
        long fileId = data.getLongExtra(KEY_FILE_ID, -1);

        if (isDeleted && fileId >= 0) {
            long teamId = TeamInfoLoader.getInstance().getTeamId();
            fileListPresenter.onFileDeleted(teamId, fileId);
        }

    }

    private void onCameraActivityResult(int resultCode, Intent intent) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        List<String> filePath = filePickerViewModel.getFilePath(getActivity(), FileUploadController.TYPE_UPLOAD_TAKE_PHOTO, intent);
        if (filePath != null && filePath.size() > 0) {
            startActivityForResult(Henson.with(getActivity())
                    .gotoFileUploadPreviewActivity()
                    .realFilePathList(new ArrayList<>(filePath))
                    .singleUpload(true)
                    .build(), FileUploadPreviewActivity.REQUEST_CODE);
        }
    }

    private void onExplorerActivityResult(int resultCode, Intent intent) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        List<String> filePath = filePickerViewModel.getFilePath(getActivity(), FileUploadController.TYPE_UPLOAD_EXPLORER, intent);
        if (filePath != null && filePath.size() > 0) {
            startActivityForResult(Henson.with(getActivity())
                    .gotoFileUploadPreviewActivity()
                    .singleUpload(true)
                    .realFilePathList(new ArrayList<>(filePath))
                    .build(), FileUploadPreviewActivity.REQUEST_CODE);
        }
    }

    private void onFileDetailShowResult(int resultCode, Intent intent) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        long fileId = intent.getLongExtra(KEY_FILE_ID, -1);
        int commentCount = intent.getIntExtra(KEY_COMMENT_COUNT, -1);

        if ((fileId <= 0 || commentCount < 0)) {
            return;
        }

        fileListPresenter.onRefreshFileInfo(fileId, commentCount);
    }

    public void onEvent(RefreshOldFileEvent event) {
        if (getUserVisibleHint()) {
            fileListPresenter.getPreviousFile();
        }
    }

    public void onEventMainThread(ShareFileEvent event) {
        if (isInSearchActivity()) {
            return;
        }
        fileListPresenter.onFileShare(event.getTeamId());
    }

    public void onEventMainThread(CategorizedMenuOfFileType event) {
        if (onSearchText != null) {
            fileListPresenter.onFileTypeSelection(
                    event.getServerQuery(), onSearchText.getSearchText());
        } else {
            fileListPresenter.onFileTypeSelection(
                    event.getServerQuery(), null);
        }

        if (!getUserVisibleHint()) {
            searchSelectorViewController.setCurrentFileType(event.getServerQuery());
        }

    }

    public void onEventMainThread(CategorizingAsOwner event) {
        if (onSearchText != null) {
            fileListPresenter.onMemberSelection(
                    event.userId, onSearchText.getSearchText());
        } else {
            fileListPresenter.onMemberSelection(
                    event.userId, null);
        }

        if (!getUserVisibleHint()) {
            searchSelectorViewController.setCurrentMember(event.userId);
        }
    }

    public void onEventMainThread(CategorizingAsEntity event) {
        if (onSearchText != null) {
            fileListPresenter.onEntitySelection(
                    event.sharedEntityId, onSearchText.getSearchText());
        } else {
            fileListPresenter.onEntitySelection(
                    event.sharedEntityId, null);
        }

        if (!getUserVisibleHint()) {
            searchSelectorViewController.setCurrentEntity(event.sharedEntityId);
        }

    }

    public void onEventMainThread(FileCreatedEvent event) {
        fileListPresenter.doSearchAll();
    }

    public void onEvent(RequestFileUploadEvent event) {
        if (!getUserVisibleHint()) {
            return;
        }
        ((BaseAppCompatActivity) getActivity()).setNeedUnLockPassCode(false);
        filePickerViewModel.selectFileSelector(event.type,
                FileListFragment.this, fileListPresenter.getSearchedEntityId());
    }

    public void onEventMainThread(ConfirmFileUploadEvent event) {
        if (!getUserVisibleHint()) {
            return;
        }
        filePickerViewModel.startUpload(
                getActivity(), event.title, event.entityId, event.realFilePath, event.comment);
    }

    public void onEventMainThread(DeleteFileEvent event) {
        if (isInSearchActivity()) {
            return;
        }
        fileListPresenter.onFileDeleted(event.getTeamId(), event.getId());
    }

    public void onEventMainThread(TopicDeleteEvent event) {
        if (isInSearchActivity()) {
            return;
        }
        fileListPresenter.onTopicDeleted(event.getTeamId());
    }

    public void onEvent(UnshareFileEvent event) {
        fileListPresenter.onFileUnshared(event.getFileId(), event.getRoomId());
    }

    public void onEventMainThread(NetworkConnectEvent event) {
        if (isInSearchActivity()) {
            return;
        }

        if (event.isConnected()) {
            fileListPresenter.onNetworkConnection();
        }
    }

    public void onEventMainThread(FileUploadFinishEvent event) {
        fileListPresenter.doSearchAll();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        this.focused = isVisibleToUser;
        if (isVisibleToUser) {
            AnalyticsUtil.sendScreenName(AnalyticsValue.Screen.FilesTab);
        }
    }

}
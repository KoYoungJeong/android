package com.tosslab.jandi.app.ui.maintab.file;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.TopicDeleteEvent;
import com.tosslab.jandi.app.events.files.CategorizedMenuOfFileType;
import com.tosslab.jandi.app.events.files.CategorizingAsEntity;
import com.tosslab.jandi.app.events.files.CategorizingAsOwner;
import com.tosslab.jandi.app.events.files.ConfirmFileUploadEvent;
import com.tosslab.jandi.app.events.files.DeleteFileEvent;
import com.tosslab.jandi.app.events.files.RefreshOldFileEvent;
import com.tosslab.jandi.app.events.files.ShareFileEvent;
import com.tosslab.jandi.app.events.network.NetworkConnectEvent;
import com.tosslab.jandi.app.events.search.SearchResultScrollEvent;
import com.tosslab.jandi.app.files.upload.FileUploadController;
import com.tosslab.jandi.app.lists.files.SearchedFileItemListAdapter;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.models.ReqSearchFile;
import com.tosslab.jandi.app.network.models.ResSearchFile;
import com.tosslab.jandi.app.ui.filedetail.FileDetailActivity_;
import com.tosslab.jandi.app.ui.fileexplorer.FileExplorerActivity;
import com.tosslab.jandi.app.ui.maintab.MainTabActivity;
import com.tosslab.jandi.app.ui.maintab.file.adapter.SearchedFilesAdapter;
import com.tosslab.jandi.app.ui.maintab.file.adapter.SearchedFilesAdapterView;
import com.tosslab.jandi.app.ui.maintab.file.dagger.DaggerFileListComponent;
import com.tosslab.jandi.app.ui.maintab.file.dagger.FileListModule;
import com.tosslab.jandi.app.ui.maintab.file.presenter.FileListPresenterImpl;
import com.tosslab.jandi.app.ui.maintab.file.presenter.FileListPresenterV3;
import com.tosslab.jandi.app.ui.search.main.view.SearchActivity;
import com.tosslab.jandi.app.ui.search.main.view.SearchActivity_;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.views.decoration.SimpleDividerItemDecoration;
import com.tosslab.jandi.app.views.listeners.SimpleEndAnimationListener;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.constant.property.ScreenViewProperty;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

/**
 * Created by tee on 16. 6. 28..
 */
public class FileListFragmentV3 extends Fragment implements FileListPresenterImpl.View {

    @Inject
    FileListPresenterV3 fileListPresenter;

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

    private long entityId = -1;

    private String entityName = null;

    private SearchedFilesAdapterView searchedFilesAdapterView;

    private SearchActivity.OnSearchItemSelect onSearchItemSelect;

    private SearchActivity.OnSearchText onSearchText;

    private boolean isSearchLayoutFirst = true;
    private boolean isForeground;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file_list, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DaggerFileListComponent.builder()
                .fileListModule(new FileListModule(this, entityId))
                .build()
                .inject(this);
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


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setListView();
        setHasOptionsMenu(true);
        fileListPresenter.initSearchQuery();
        resetFilterLayoutPosition();
        if (isInSearchActivity() && isSearchLayoutFirst) {
            initSearchLayoutIfFirst();
        }
        fileListPresenter.doSearchAll();
    }

    public void setListView() {
        if (getActivity() instanceof SearchActivity) {
            AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                    .event(Event.ScreenView)
                    .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                    .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                    .property(PropertyKey.ScreenView, ScreenViewProperty.FILE_SEARCH)
                    .build());

            AnalyticsUtil.sendScreenName(AnalyticsValue.Screen.FilesSearch);
        }

        // Empty View를 가진 ListView 설정
        lvSearchFiles.setLayoutManager(new LinearLayoutManager(getActivity()));
        lvSearchFiles.addItemDecoration(new SimpleDividerItemDecoration());
        SearchedFilesAdapter searchedFilesAdapter = new SearchedFilesAdapter();
        lvSearchFiles.setAdapter(searchedFilesAdapter);
        searchedFilesAdapter.setOnRecyclerItemClickListener((view, adapter, position) -> {
            moveToFileDetailActivity(((SearchedFileItemListAdapter) adapter).getItem(position).id);

            if (onSearchItemSelect != null) {
                onSearchItemSelect.onSearchItemSelect();
            }

            AnalyticsValue.Action action;

            if (fileListPresenter.isDefaultSeachQuery()) {
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
        searchedFilesAdapterView = searchedFilesAdapter;
        fileListPresenter.setSearchedFilesAdapterModel(searchedFilesAdapter);
    }

    private void moveToFileDetailActivity(long fileId) {
        FileDetailActivity_
                .intent(this)
                .fileId(fileId)
                .startForResult(JandiConstants.TYPE_FILE_DETAIL_REFRESH);
        getActivity().overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
    }

    public void setSearchEmptryViewVisible(int visible) {
        searchEmptyView.setVisibility(visible);
    }

    public ProgressDialog getUploadProgress(ConfirmFileUploadEvent event) {
        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage(getString(R.string.jandi_file_uploading) + " " + event.realFilePath);
        progressDialog.show();

        return progressDialog;
    }

    public void showSuccessToast(String message) {
        ColoredToast.show(message);
    }

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

    public void dismissProgressBarDelay() {
        dismissProgressBar();
    }

    public void openAlbumForActivityResult(Fragment fragment) {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        fragment.startActivityForResult(intent, FileUploadController.TYPE_UPLOAD_GALLERY);
    }

    public void openCameraForActivityResult(Fragment fragment, Uri fileUri) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        fragment.startActivityForResult(intent, FileUploadController.TYPE_UPLOAD_TAKE_PHOTO);
    }

    public void openExplorerForActivityResult(Fragment fragment) {
        Intent intent = new Intent(getContext(), FileExplorerActivity.class);
        fragment.startActivityForResult(intent, FileUploadController.TYPE_UPLOAD_EXPLORER);
    }

    public void dismissProgressDialog(Dialog dialog) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public void exceedMaxFileSizeError() {
        ColoredToast.show(getString(R.string.jandi_file_size_large_error));
    }

    @Override
    public void setInitLoadingViewVisible(int visible) {
        initLoadingView.setVisibility(visible);
    }

    @Override
    public void setEmptyViewVisible(int visible) {
        uploadEmptyView.setVisibility(visible);
    }

    public void showWarningToast(String message) {
        ColoredToast.showWarning(message);
    }

    public void showErrorToast(String failMessage) {
        ColoredToast.show(failMessage);
    }

    public void showMoreProgressBar() {
        moreLoadingProgressBar.setVisibility(View.VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_bottom);
        moreLoadingProgressBar.setAnimation(animation);
        animation.startNow();
    }

    public void dismissProgressBar() {
        moreLoadingProgressBar.getAnimation().reset();
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_to_bottom);
        moreLoadingProgressBar.setAnimation(animation);
        animation.setAnimationListener(new SimpleEndAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                moreLoadingProgressBar.setVisibility(View.GONE);
            }
        });

        animation.startNow();
    }

    @Override
    public void clearListView() {
        searchedFilesAdapterView.clearListView();
    }

    @Override
    public void searchFailed(int errMessageRes) {
        FragmentActivity activity = getActivity();
        if (activity != null && !(activity.isFinishing())) {
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

    public void setOnSearchItemSelect(SearchActivity.OnSearchItemSelect onSearchItemSelect) {
        this.onSearchItemSelect = onSearchItemSelect;
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
            SearchActivity_.intent(getActivity())
                    .isFromFiles(true)
                    .start();
        }
        return super.onOptionsItemSelected(item);
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
                new SearchResultScrollEvent(FileListFragmentV3.this.getClass(), -offset));
    }

    private boolean isInSearchActivity() {
        return getActivity() instanceof SearchActivity;
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

                EventBus.getDefault().post(new SearchResultScrollEvent(FileListFragmentV3.this.getClass(),
                        offset));

            }
        });
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

}
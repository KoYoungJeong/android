package com.tosslab.jandi.app.ui.maintab.file;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.FileUploadDialogFragment;
import com.tosslab.jandi.app.dialogs.FileUploadTypeDialogFragment;
import com.tosslab.jandi.app.events.files.CategorizedMenuOfFileType;
import com.tosslab.jandi.app.events.files.CategorizingAsEntity;
import com.tosslab.jandi.app.events.files.CategorizingAsOwner;
import com.tosslab.jandi.app.events.files.ConfirmFileUploadEvent;
import com.tosslab.jandi.app.events.files.DeleteFileEvent;
import com.tosslab.jandi.app.events.files.RefreshOldFileEvent;
import com.tosslab.jandi.app.events.files.RequestFileUploadEvent;
import com.tosslab.jandi.app.events.files.ShareFileEvent;
import com.tosslab.jandi.app.events.search.SearchResultScrollEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.lists.files.SearchedFileItemListAdapter;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.models.ReqSearchFile;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResSearchFile;
import com.tosslab.jandi.app.ui.filedetail.FileDetailActivity_;
import com.tosslab.jandi.app.ui.maintab.MainTabActivity;
import com.tosslab.jandi.app.ui.maintab.file.model.FileListModel;
import com.tosslab.jandi.app.ui.search.main.view.SearchActivity;
import com.tosslab.jandi.app.ui.search.main.view.SearchActivity_;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.GoogleImagePickerUtil;
import com.tosslab.jandi.app.utils.ImageFilePath;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.views.SimpleDividerItemDecoration;

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
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.ExecutionException;

import de.greenrobot.event.EventBus;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by justinygchoi on 2014. 10. 13..
 */
@EFragment(R.layout.fragment_file_list)
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

    @SystemService
    InputMethodManager inputMethodManager;

    private SearchQuery mSearchQuery;
    private ProgressWheel mProgressWheel;
    private Context mContext;

    /**
     * File tab 을 위한 액션바와 카테고리 선택 다이얼로그, 이벤트 전달
     */
    private int selectedTeamId;
    private boolean isSearchLayoutFirst = true;
    private File photoFileByCamera;
    private boolean isForeground;
    private PublishSubject<Integer> initSearchSubject;

    @AfterInject
    void init() {
        mContext = getActivity();
        mSearchQuery = new SearchQuery();
        if (entityIdForCategorizing >= 0) {
            mSearchQuery.setSharedEntity(entityIdForCategorizing);
        }
        fileListPresenter.setEntityIdForCategorizing(entityIdForCategorizing);
        fileListPresenter.setCurrentEntityCategorizingAccodingBy(mCurrentEntityCategorizingAccodingBy);

        initSearchSubject = PublishSubject.create();
        initSearchSubject.observeOn(Schedulers.io())
                .subscribe(index -> {
                    mSearchQuery.setToFirst();
                    searchedFileItemListAdapter.clearAdapter();
                    doSearchInBackground(index);
                }, throwable -> log.debug("Search Fail : " + throwable.getMessage()));
    }

    @AfterViews
    void bindAdapter() {
        setHasOptionsMenu(true);

        // myToken 획득
        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(mContext);
        mProgressWheel.init();

        fileListModel.retrieveEntityManager();

        // Empty View를 가진 ListView 설정
        actualListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        actualListView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
        actualListView.setAdapter(searchedFileItemListAdapter);

        selectedTeamId = JandiAccountDatabaseManager.getInstance(getActivity()).getSelectedTeamInfo().getTeamId();

        searchedFileItemListAdapter.setOnRecyclerItemClickListener((view, adapter, position) -> moveToFileDetailActivity(((SearchedFileItemListAdapter) adapter).getItem(position).id));

        if (isInSearchActivity()) {
            resetFilterLayoutPosition();
            if (isSearchLayoutFirst) {
                initSearchLayoutIfFirst();
            }
        }

        initSearchSubject.onNext(-1);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        menu.clear();

        FragmentActivity activity = getActivity();
        if (activity instanceof MainTabActivity) {
            activity.getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        } else if (activity instanceof FileListActivity) {
            activity.getMenuInflater().inflate(R.menu.file_list_actionbar_menu, menu);

            MenuItem searchMenu = menu.findItem(R.id.action_file_list_search);
            SearchView searchView = ((SearchView) searchMenu.getActionView());

            MenuItemCompat.setOnActionExpandListener(searchMenu, new MenuItemCompat.OnActionExpandListener() {

                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    inputMethodManager.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
                    onNewQuery("");
                    return true;
                }
            });

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    inputMethodManager.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
                    onNewQuery(s);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    return false;
                }
            });

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

    public void onEventMainThread(ShareFileEvent event) {

        if (isInSearchActivity()) {
            return;
        }

        int itemCount = searchedFileItemListAdapter.getItemCount();
        searchedFileItemListAdapter.clearAdapter();
        initSearchSubject.onNext(itemCount + 1);
    }


    @Background
    void getPreviousFile() {

        int justGetFilesSize;

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

                fileListPresenter.showWarningToast(getString(R.string.warn_no_more_files));
                searchedFileItemListAdapter.setNoMoreLoad();
            } else {
                searchedFileItemListAdapter.setReadyMore();
            }

        } catch (JandiNetworkException e) {
            log.error("fail to get searched files.", e);
            fileListPresenter.showErrorToast(getString(R.string.err_file_search));
        } catch (Exception e) {
            fileListPresenter.showErrorToast(getString(R.string.err_file_search));
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
        searchedFileItemListAdapter.clearAdapter();
        initSearchSubject.onNext(-1);
        onSearchHeaderReset();
    }

    public void onEventMainThread(ConfirmFileUploadEvent event) {

        if (!isForeground) {
            return;
        }

        ProgressDialog uploadProgress = fileListPresenter.getUploadProgress(event);

        uploadFile(event, uploadProgress);

    }

    @Background
    void uploadFile(ConfirmFileUploadEvent event, ProgressDialog uploadProgressDialog) {

        int entityId = event.entityId;
        FormattedEntity entity = EntityManager.getInstance(getActivity()).getEntityById(entityId);

        boolean isPublicTopic = entity.isPublicTopic();
        try {
            JsonObject result = fileListModel.uploadFile(event, uploadProgressDialog, isPublicTopic);

            int entityType = entity.isPublicTopic() ? JandiConstants.TYPE_PUBLIC_TOPIC : entity.isPrivateGroup() ? JandiConstants.TYPE_PRIVATE_TOPIC : JandiConstants.TYPE_DIRECT_MESSAGE;
            fileListPresenter.showSuccessToast(getString(R.string.jandi_file_upload_succeed));
            fileListModel.trackUploadingFile(entityType, result);

            initSearchSubject.onNext(ReqSearchFile.MAX);

        } catch (ExecutionException e) {
            if (getActivity() != null) {
                fileListPresenter.showErrorToast(getString(R.string.jandi_canceled));
            }
        } catch (Exception e) {
            fileListPresenter.showErrorToast(getString(R.string.err_file_upload_failed));
        } finally {
            fileListPresenter.dismissProgressDialog(uploadProgressDialog);
        }
    }

    public void onEvent(DeleteFileEvent event) {

        if (isInSearchActivity()) {
            return;
        }

        int fileId = event.getId();
        int positionByFileId = searchedFileItemListAdapter.findPositionByFileId(fileId);
        if (positionByFileId >= 0) {
            removeItem(positionByFileId);
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

        try {
            ReqSearchFile reqSearchFile = mSearchQuery.getRequestQuery();
            reqSearchFile.teamId = selectedTeamId;
            if (requestCount > ReqSearchFile.MAX) {
                reqSearchFile.listCount = requestCount;
            }
            ResSearchFile resSearchFile = fileListModel.searchFileList(reqSearchFile);
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
        } catch (JandiNetworkException e) {
            log.error("fail to get searched files.", e);
            searchFailed(R.string.err_file_search);
        } catch (Exception e) {
            searchFailed(R.string.err_file_search);
        }
    }

    @Click(R.id.layout_file_list_empty)
    void onUploadClick() {
        DialogFragment fileUploadTypeDialog = new FileUploadTypeDialogFragment();
        fileUploadTypeDialog.show(getFragmentManager(), "dialog");
    }

    public void onEvent(RequestFileUploadEvent event) {
        if (!isForeground) {
            return;
        }
        switch (event.type) {
            case JandiConstants.TYPE_UPLOAD_GALLERY:
                fileListPresenter.openAlbumForActivityResult(FileListFragment.this);
                break;
            case JandiConstants.TYPE_UPLOAD_TAKE_PHOTO:
                try {
                    photoFileByCamera = File.createTempFile("camera", ".jpg", new File(GoogleImagePickerUtil.getDownloadPath()));
                    fileListPresenter.openCameraForActivityResult(FileListFragment.this, Uri.fromFile(photoFileByCamera));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case JandiConstants.TYPE_UPLOAD_EXPLORER:
                fileListPresenter.openExplorerForActivityResult(FileListFragment.this);
                break;
            default:
                break;
        }
    }

    @OnActivityResult(JandiConstants.TYPE_UPLOAD_GALLERY)
    void onGalleryActivityResult(int resultCode, Intent intent) {
        if (resultCode != Activity.RESULT_OK || intent == null) {
            return;
        }

        String realFilePath;
        Uri data = intent.getData();
        Bundle extras = intent.getExtras();


        if (data != null) {
            realFilePath = ImageFilePath.getPath(getActivity(), data);
            if (GoogleImagePickerUtil.isUrl(realFilePath)) {

                String downloadDir = GoogleImagePickerUtil.getDownloadPath();
                String downloadName = GoogleImagePickerUtil.getWebImageName();
                ProgressDialog downloadProgress = GoogleImagePickerUtil.getDownloadProgress(getActivity(), downloadDir, downloadName);
                downloadImageAndShowFileUploadDialog(downloadProgress, realFilePath, downloadDir, downloadName);
            } else {
                showFileUploadDialog(realFilePath);
            }
        } else if (extras != null && extras.containsKey("data")) {

            Object data1 = extras.get("data");

            if (data1 instanceof Bitmap) {
                Bitmap bitmap = (Bitmap) data1;
                saveAndShowFileUploadDialog(bitmap);
            }

        }
    }

    @Background
    void saveAndShowFileUploadDialog(Bitmap bitmap) {

        String path = GoogleImagePickerUtil.getDownloadPath() + "/camera.jpg";
        OutputStream stream = null;
        try {
            stream = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            showFileUploadDialog(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @UiThread
    void showFileUploadDialog(String realFilePath) {
        if (fileListModel.isOverSize(realFilePath)) {
            fileListPresenter.exceedMaxFileSizeError();
        } else {
            DialogFragment newFragment = FileUploadDialogFragment.newInstance(realFilePath, mSearchQuery.mSearchEntity);
            newFragment.show(getFragmentManager(), "dialog");

        }
    }

    @Background
    void downloadImageAndShowFileUploadDialog(ProgressDialog downloadProgress, String realFilePath, String downloadDir, String downloadName) {

        try {
            File file = GoogleImagePickerUtil.downloadFile(getActivity(), downloadProgress, realFilePath, downloadDir, downloadName);
            fileListPresenter.dismissProgressDialog(downloadProgress);
            showFileUploadDialog(file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnActivityResult(JandiConstants.TYPE_UPLOAD_TAKE_PHOTO)
    void onCameraActivityResult(int resultCode, Intent intent) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (photoFileByCamera != null && photoFileByCamera.exists()) {
            showFileUploadDialog(photoFileByCamera.getAbsolutePath());
        }

    }

    @OnActivityResult(JandiConstants.TYPE_UPLOAD_EXPLORER)
    void onExplorerActivityResult(int resultCode, Intent intent) {

        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        String realFilePath = intent.getStringExtra("GetPath") + File.separator + intent.getStringExtra("GetFileName");
        if (!TextUtils.isEmpty(realFilePath)) {
            showFileUploadDialog(realFilePath);
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
        if (!isForeground) {
            return;
        }

        resetFilterLayoutPosition();
    }

    private void resetFilterLayoutPosition() {
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

        if (!isSearchLayoutFirst || headerView == null || actualListView == null) {
            return;
        }

        isSearchLayoutFirst = false;

        RelativeLayout.LayoutParams headerViewLayoutParams = ((RelativeLayout.LayoutParams) headerView.getLayoutParams());
        headerViewLayoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics());
//        headerViewLayoutParams.topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64, getResources().getDisplayMetrics());
        headerView.setBackgroundColor(getResources().getColor(R.color.jandi_primary_color));

        setHeaderTextViewColor(((ViewGroup) headerView), getResources().getColor(R.color.white));
        setHeaderImageViewImage(((ViewGroup) headerView), R.drawable.jandi_arrow_down);

        ((ViewGroup) ((ViewGroup) headerView).getChildAt(0)).getChildAt(1).setVisibility(View.INVISIBLE);
        ((ViewGroup) ((ViewGroup) headerView).getChildAt(0)).getChildAt(3).setVisibility(View.INVISIBLE);

        final int headerMaxY = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64, getResources().getDisplayMetrics());
        final int headerMinY = 0;

        int paddingTop = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48 + 64.5f, getResources().getDisplayMetrics());
        actualListView.setPadding(actualListView.getPaddingLeft(), paddingTop, actualListView.getPaddingRight(), actualListView.getPaddingBottom());

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

                EventBus.getDefault().post(new SearchResultScrollEvent(FileListFragment.this.getClass(),
                        offset));

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

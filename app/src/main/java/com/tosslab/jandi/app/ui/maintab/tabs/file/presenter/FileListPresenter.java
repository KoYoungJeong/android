package com.tosslab.jandi.app.ui.maintab.tabs.file.presenter;

import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResSearchFile;
import com.tosslab.jandi.app.ui.maintab.tabs.file.adapter.SearchedFilesAdapterModel;

/**
 * Created by tee on 16. 6. 28..
 */
public interface FileListPresenter {
    void setSearchedFilesAdapterModel(SearchedFilesAdapterModel adapterModel);

    void setListNoMoreLoad();

    void setListReadyLoadMore();

    long getSearchedEntityId();

    void getPreviousFile();

    boolean isDefaultSeachQuery();

    void doSearchAll();

    void onFileShare(long teamId);

    void onFileTypeSelection(String fileTypeQuery, String searchText);

    void onMemberSelection(long userId, String searchText);

    void onEntitySelection(long sharedEntityId, String searchText);

    void onFileDeleted(long teamId, long fileId);

    void onTopicDeleted(long teamId);

    void onNetworkConnection();

    void onNewQuery(String s);

    void onRefreshFileInfo(long fileId, int commentCount);

    void onDestory();

    void getImageDetail(long fileId);

    void onMoveFileSearch();

    interface View {
        void clearListView();

        void searchFailed(int errMessageRes);

        void searchSucceed(ResSearchFile resSearchFile);

        void moveToCarousel(ResMessages.FileMessage fileMessage);

        void setSearchEmptryViewVisible(int visible);

        void setInitLoadingViewVisible(int gone);

        void setEmptyViewVisible(int gone);

        void showMoreProgressBar();

        void showWarningToast(String string);

        void dismissMoreProgressBar();

        void onSearchHeaderReset();

        void justRefresh();

        void showProgress();

        void dismissProgress();

        void moveFileSearch(long entity, long writer, String type);
    }
}

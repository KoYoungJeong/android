package com.tosslab.jandi.app.ui.maintab.file.presenter;

import com.tosslab.jandi.app.network.models.ResSearchFile;
import com.tosslab.jandi.app.ui.maintab.file.adapter.SearchedFilesAdapterModel;

/**
 * Created by tee on 16. 6. 28..
 */
public interface FileListPresenter {
    void setSearchedFilesAdapterModel(SearchedFilesAdapterModel adapterModel);

    void initSearchQuery();

    void setListNoMoreLoad();

    void setListReadyLoadMore();

    long getSearchedEntityId();

    void getPreviousFile();

    boolean isDefaultSeachQuery();

    void doSearchAll();

    void onFileShare(long teamId);

    void onFileTypeSelection(String query, String searchText);

    void onMemberSelection(String userId, String searchText);

    void onEntitySelection(long sharedEntityId, String searchText);

    void onFileDeleted(long teamId, long fileId);

    void onTopicDeleted(long teamId);

    void onNetworkConnection();

    void doKeywordSearch(String s);

    void onRefreshFileInfo(int fileId, int commentCount);

    interface View {
        void clearListView();

        void searchFailed(int errMessageRes);

        void searchSucceed(ResSearchFile resSearchFile);

        void setSearchEmptryViewVisible(int visible);

        void setInitLoadingViewVisible(int gone);

        void setEmptyViewVisible(int gone);

        void showMoreProgressBar();

        void showWarningToast(String string);

        void dismissMoreProgressBar();

        void onSearchHeaderReset();

        void justRefresh();
    }
}

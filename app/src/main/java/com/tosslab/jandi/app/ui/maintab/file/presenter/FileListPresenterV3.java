package com.tosslab.jandi.app.ui.maintab.file.presenter;

import com.tosslab.jandi.app.network.models.ResSearchFile;
import com.tosslab.jandi.app.ui.maintab.file.adapter.SearchedFilesAdapterModel;

/**
 * Created by tee on 16. 6. 28..
 */
public interface FileListPresenterV3 {
    void setSearchedFilesAdapterModel(SearchedFilesAdapterModel adapterModel);

    void initSearchQuery();

    void setListNoMoreLoad();

    void setListReadyLoadMore();

    long getSearchedEntityId();

    boolean isDefaultSeachQuery();

    void doSearchAll();

    void doSearchByCnt(int cnt);

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
    }
}

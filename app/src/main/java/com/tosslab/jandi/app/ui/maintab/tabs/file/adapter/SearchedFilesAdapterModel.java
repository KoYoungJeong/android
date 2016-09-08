package com.tosslab.jandi.app.ui.maintab.tabs.file.adapter;

import com.tosslab.jandi.app.network.models.search.ResSearch;

import java.util.List;

/**
 * Created by tee on 16. 6. 28..
 */
public interface SearchedFilesAdapterModel {

    void clearList();

    void add(List<ResSearch.SearchRecord> files);

    void setNoMoreLoad();

    void setReadyMore();

    ResSearch.SearchRecord getItem(int position);

    int getItemCount();

    int findPositionByFileId(long fileId);

    void remove(int position);

}

package com.tosslab.jandi.app.ui.maintab.file.adapter;

import com.tosslab.jandi.app.network.models.ResMessages;

import java.util.List;

/**
 * Created by tee on 16. 6. 28..
 */
public interface SearchedFilesAdapterModel {

    public void clearList();

    public void add(List<ResMessages.OriginalMessage> files);

    public void setNoMoreLoad();

    public void setReadyMore();

    public ResMessages.FileMessage getItem(int position);

    public int getItemCount();

    public int findPositionByFileId(long fileId);

    public void remove(int position);

}

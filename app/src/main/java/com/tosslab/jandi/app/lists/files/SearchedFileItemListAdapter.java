package com.tosslab.jandi.app.lists.files;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.tosslab.jandi.app.events.files.RefreshOldFileEvent;
import com.tosslab.jandi.app.network.models.ResMessages;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 7. 5..
 */
@EBean
public class SearchedFileItemListAdapter extends BaseAdapter {
    List<ResMessages.FileMessage> searedFiles;

    @RootContext
    Context mContext;

    MoreState moreState = MoreState.Idle;

    @AfterInject
    void initAdapter() {
        searedFiles = new ArrayList<ResMessages.FileMessage>();
    }

    public void clearAdapter() {
        searedFiles.clear();
        notifyDataSetChanged();
    }

    public void clearAdapterWithoutNotify() {
        searedFiles.clear();
    }

    public void insert(List<ResMessages.OriginalMessage> files) {
        for (ResMessages.OriginalMessage message : files) {
            if (message instanceof ResMessages.FileMessage && message.status.equals("created")) {
                searedFiles.add((ResMessages.FileMessage) message);
            }
        }
    }

    @Override
    public int getCount() {
        return searedFiles.size();
    }

    @Override
    public ResMessages.FileMessage getItem(int position) {
        return searedFiles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SearchedFileItemView searchedFileItemView;
        if (convertView == null) {
            searchedFileItemView = SearchedFileItemView_.build(mContext);
        } else {
            searchedFileItemView = (SearchedFileItemView) convertView;
        }

        searchedFileItemView.bind(getItem(position));

        if (position == getCount() - 1 && moreState == MoreState.Idle) {
            moreState = MoreState.Loading;

            EventBus.getDefault().post(new RefreshOldFileEvent());
        }

        return searchedFileItemView;
    }

    public void setNoMoreLoad() {
        moreState = MoreState.NoMore;
    }

    public void setReadyMore() {
        moreState = MoreState.Idle;

    }

    private enum MoreState {
        Idle, Loading, NoMore
    }
}

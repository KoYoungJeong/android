package com.tosslab.toss.app.lists;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.tosslab.toss.app.network.models.ResMessages;
import com.tosslab.toss.app.network.models.ResSearchFile;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by justinygchoi on 2014. 7. 5..
 */
@EBean
public class SearchedFileItemListAdapter extends BaseAdapter {
    List<ResMessages.FileMessage> searedFiles;

    @RootContext
    Context mContext;

    @AfterInject
    void initAdapter() {
        searedFiles = new ArrayList<ResMessages.FileMessage>();
    }

    public void clearAdapter() {
        searedFiles.clear();
        notifyDataSetChanged();
    }

    public void insert(ResSearchFile resSearchFile) {
        for (ResMessages.OriginalMessage message : resSearchFile.files) {
            if (message instanceof ResMessages.FileMessage) {
                searedFiles.add((ResMessages.FileMessage)message);
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

        return searchedFileItemView;
    }
}

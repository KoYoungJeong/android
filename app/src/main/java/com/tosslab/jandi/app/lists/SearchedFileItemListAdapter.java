package com.tosslab.jandi.app.lists;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResSearchFile;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
        List<ResMessages.OriginalMessage> sortedFiles = descSortByCreateTime(resSearchFile.files);
        for (ResMessages.OriginalMessage message : sortedFiles) {
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

    private List<ResMessages.OriginalMessage> descSortByCreateTime(List<ResMessages.OriginalMessage> links) {
        List<ResMessages.OriginalMessage> ret = new ArrayList<ResMessages.OriginalMessage>(links);

        Comparator<ResMessages.OriginalMessage> sort = new Comparator<ResMessages.OriginalMessage>() {
            @Override
            public int compare(ResMessages.OriginalMessage link, ResMessages.OriginalMessage link2) {
                if (link.createTime.getTime() > link2.createTime.getTime())
                    return -1;
                else if (link.createTime.getTime() == link2.createTime.getTime())
                    return 0;
                else
                    return 1;
            }
        };
        Collections.sort(ret, sort);
        return ret;
    }
}

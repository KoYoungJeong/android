package com.tosslab.jandi.app.lists;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.tosslab.jandi.app.network.models.ResFileDetail;
import com.tosslab.jandi.app.network.models.ResMessages;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by justinygchoi on 2014. 6. 24..
 */
@EBean
public class FileDetailListAdapter extends BaseAdapter {
    private final Logger log = Logger.getLogger(FileDetailListAdapter.class);
    List<ResMessages.OriginalMessage> mMessages;

    @RootContext
    Context mContext;

    @AfterInject
    void initAdapter() {
        mMessages = new ArrayList<ResMessages.OriginalMessage>();
    }

    @Override
    public int getCount() {
        return mMessages.size();
    }

    @Override
    public ResMessages.OriginalMessage getItem(int position) {
        return mMessages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        FileExplorerDetailView fileExplorerDetailView;
        if (convertView == null) {
            fileExplorerDetailView = FileExplorerDetailView_.build(mContext);
        } else {
            fileExplorerDetailView = (FileExplorerDetailView) convertView;
        }

        fileExplorerDetailView.bind(getItem(position));

        return fileExplorerDetailView;
    }

    public void updateFileDetails(ResFileDetail resFileDetail) {
        for (ResMessages.OriginalMessage fileDetail : resFileDetail.messageDetails) {
            if (fileDetail instanceof ResMessages.FileMessage) {
                mMessages.add(0, fileDetail);
            } else if (fileDetail instanceof ResMessages.CommentMessage) {
                mMessages.add(fileDetail);
            }
        }
        log.debug("Upload done : " + mMessages.size() + " items.");
    }

    public void clear() {
        mMessages.clear();
    }
}

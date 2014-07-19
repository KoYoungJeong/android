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
 * Created by justinygchoi on 2014. 7. 19..
 */
@EBean
public class FileDetailCommentListAdapter extends BaseAdapter {
    private final Logger log = Logger.getLogger(FileDetailCommentListAdapter.class);
    List<ResMessages.CommentMessage> mMessages;

    @RootContext
    Context mContext;

    @AfterInject
    void initAdapter() {
        mMessages = new ArrayList<ResMessages.CommentMessage>();
    }

    @Override
    public int getCount() {
        return mMessages.size();
    }

    @Override
    public ResMessages.CommentMessage getItem(int position) {
        return mMessages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        FileDetailCommentView fileDetailView;
        if (convertView == null) {
            fileDetailView = FileDetailCommentView_.build(mContext);
        } else {
            fileDetailView = (FileDetailCommentView) convertView;
        }

        fileDetailView.bind(getItem(position));

        return fileDetailView;
    }

    public void updateFileComments(ResFileDetail resFileDetail) {
        for (ResMessages.OriginalMessage fileDetail : resFileDetail.messageDetails) {
            if (fileDetail instanceof ResMessages.CommentMessage) {
                mMessages.add((ResMessages.CommentMessage)fileDetail);
            }
        }
        log.debug("Upload done : " + mMessages.size() + " items.");
    }

    public void clear() {
        mMessages.clear();
    }
}

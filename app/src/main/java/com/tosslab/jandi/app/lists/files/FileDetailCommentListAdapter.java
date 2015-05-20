package com.tosslab.jandi.app.lists.files;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.tosslab.jandi.app.network.models.ResFileDetail;
import com.tosslab.jandi.app.network.models.ResMessages;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by justinygchoi on 2014. 7. 19..
 */
@EBean
public class FileDetailCommentListAdapter extends BaseAdapter {
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

    /**
     * TODO : 로직을 언젠가는 MessageItemListAdapter와 합칠 필요가 있겠음.
     *
     * @param resFileDetail
     */
    public void updateFileComments(ResFileDetail resFileDetail) {
        for (ResMessages.OriginalMessage fileDetail : resFileDetail.messageDetails) {
            if (fileDetail instanceof ResMessages.CommentMessage) {
                if (fileDetail.status.equals("created") || fileDetail.status.equals("shared")) {
                    mMessages.add((ResMessages.CommentMessage) fileDetail);
                } else if (fileDetail.status.equals("edited")) {
                    int position = searchIndexOfMessages(fileDetail.id);
                    if (position >= 0) {
                        mMessages.set(position, (ResMessages.CommentMessage) fileDetail);
                    }
                } else if (fileDetail.status.equals("archived")) {
                    int position = searchIndexOfMessages(fileDetail.id);
                    if (position >= 0) {
                        mMessages.remove(position);
                    }
                }

            }
        }
    }

    // 현재 화면에 뿌려진 메시지들 중에 messageId와 동일한 놈의 index 반환
    private int searchIndexOfMessages(int commentId) {
        for (int i = 0; i < mMessages.size(); i++) {
            if (mMessages.get(i).id == commentId)
                return i;
        }
        return -1;
    }

    public void clear() {
        mMessages.clear();
    }
}

package com.tosslab.jandi.app.lists.files;

import android.animation.Animator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.tosslab.jandi.app.lists.files.viewholder.CommentViewHolder;
import com.tosslab.jandi.app.lists.files.viewholder.FileDetailCollapseCommentView;
import com.tosslab.jandi.app.lists.files.viewholder.FileDetailCollapseStickerCommentView;
import com.tosslab.jandi.app.lists.files.viewholder.FileDetailCommentStickerView;
import com.tosslab.jandi.app.lists.files.viewholder.FileDetailCommentView;
import com.tosslab.jandi.app.network.models.ResFileDetail;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.DateComparatorUtil;
import com.tosslab.jandi.app.views.listeners.SimpleEndAnimatorListener;

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
    List<ResMessages.OriginalMessage> mMessages;

    @RootContext
    Context mContext;
    private int selectMessageId;
    private AnimStat animStat = AnimStat.START;

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

        ViewHolder viewHolder = null;
        int itemViewType = getItemViewType(position);

        CommentViewType commentViewType = CommentViewType.values()[itemViewType];

        switch (commentViewType) {
            default:
            case Comment:
                if (convertView == null) {
                    viewHolder = new ViewHolder();
                    viewHolder.commentViewHolder = new FileDetailCommentView();
                }
                break;
            case PureComment:
                if (convertView == null) {
                    viewHolder = new ViewHolder();
                    viewHolder.commentViewHolder = new FileDetailCollapseCommentView();
                }
                break;
            case Sticker:
                if (convertView == null) {
                    viewHolder = new ViewHolder();
                    viewHolder.commentViewHolder = new FileDetailCommentStickerView();
                }
                break;
            case PureSticker:
                if (convertView == null) {
                    viewHolder = new ViewHolder();
                    viewHolder.commentViewHolder = new FileDetailCollapseStickerCommentView();
                }
                break;
        }

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(viewHolder.commentViewHolder.getLayoutResourceId(), parent, false);
            viewHolder.commentViewHolder.init(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        ResMessages.OriginalMessage item = getItem(position);
        viewHolder.commentViewHolder.bind(item);

        if (item.id == selectMessageId && animStat == AnimStat.START) {
            viewHolder.commentViewHolder.startAnimation(new SimpleEndAnimatorListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    animStat = AnimStat.END;
                }
            });

            animStat = AnimStat.ANIM;
        }

        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        ResMessages.OriginalMessage currentMessage = getItem(position);
        ResMessages.OriginalMessage beforeMessage;

        if (position > 0) {
            beforeMessage = getItem(position - 1);
        } else {

            if (currentMessage instanceof ResMessages.CommentMessage) {
                return CommentViewType.Comment.ordinal();
            } else {
                return CommentViewType.Sticker.ordinal();
            }

        }

        if (position > 0
                && currentMessage.writerId == beforeMessage.writerId
                && DateComparatorUtil.isSince5min(currentMessage.createTime, beforeMessage.createTime)) {
            if (currentMessage instanceof ResMessages.CommentMessage) {
                return CommentViewType.PureComment.ordinal();
            } else {
                return CommentViewType.PureSticker.ordinal();
            }
        } else {
            if (currentMessage instanceof ResMessages.CommentMessage) {
                return CommentViewType.Comment.ordinal();
            } else {
                return CommentViewType.Sticker.ordinal();
            }
        }
    }

    @Override
    public int getViewTypeCount() {
        return CommentViewType.values().length;
    }

    /**
     * TODO : 로직을 언젠가는 MessageItemListAdapter와 합칠 필요가 있겠음.
     *
     * @param resFileDetail
     */
    public void updateFileComments(ResFileDetail resFileDetail) {
        for (ResMessages.OriginalMessage fileDetail : resFileDetail.messageDetails) {
            if (fileDetail instanceof ResMessages.FileMessage) {
                continue;
            }

            if (fileDetail.status.equals("created") || fileDetail.status.equals("shared")) {
                mMessages.add(fileDetail);
            } else if (fileDetail.status.equals("edited")) {
                int position = searchIndexOfMessages(fileDetail.id);
                if (position >= 0) {
                    mMessages.set(position, fileDetail);
                }
            } else if (fileDetail.status.equals("archived")) {
                int position = searchIndexOfMessages(fileDetail.id);
                if (position >= 0) {
                    mMessages.remove(position);
                }
            }
        }
    }

    // 현재 화면에 뿌려진 메시지들 중에 messageId와 동일한 놈의 index 반환
    public int searchIndexOfMessages(int commentId) {
        for (int i = 0; i < mMessages.size(); i++) {
            if (mMessages.get(i).id == commentId)
                return i;
        }
        return -1;
    }

    public void modifyStarredStateByPosition(int position, boolean isStarred) {
        mMessages.get(position).isStarred = isStarred;
        notifyDataSetChanged();
    }

    public void clear() {
        mMessages.clear();
    }

    public int findMessagePosition(int messageId) {

        for (int idx = 0, size = getCount(); idx < size; ++idx) {
            if (getItem(idx).id == messageId) {
                return idx;
            }
        }

        return -1;
    }

    public void setSelectMessage(int selectMessageId) {
        this.selectMessageId = selectMessageId;
        animStat = AnimStat.START;
    }

    private enum AnimStat {
        START, ANIM, END
    }

    enum CommentViewType {
        Comment, PureComment, Sticker, PureSticker
    }

    private static class ViewHolder {
        CommentViewHolder commentViewHolder;
    }
}

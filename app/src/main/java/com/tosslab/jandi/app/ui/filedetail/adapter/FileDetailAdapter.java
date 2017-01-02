package com.tosslab.jandi.app.ui.filedetail.adapter;

import android.view.ViewGroup;

import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.base.adapter.MultiItemRecyclerAdapter;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.ui.comment.CellDividerUpdater;
import com.tosslab.jandi.app.ui.comment.OnCommentClickListener;
import com.tosslab.jandi.app.ui.comment.OnCommentLongClickListener;
import com.tosslab.jandi.app.ui.comment.StickerCommentViewHolder;
import com.tosslab.jandi.app.ui.comment.TextCommentViewHolder;
import com.tosslab.jandi.app.ui.filedetail.adapter.viewholder.DividerViewHolder;
import com.tosslab.jandi.app.ui.filedetail.adapter.viewholder.ImageFileViewHolder;
import com.tosslab.jandi.app.ui.filedetail.adapter.viewholder.NormalFileViewHolder;

/**
 * Created by tonyjs on 16. 1. 19..
 */
public class FileDetailAdapter extends MultiItemRecyclerAdapter {

    public static final int VIEW_TYPE_FILE = 0;
    public static final int VIEW_TYPE_IMAGE = 1;
    public static final int VIEW_TYPE_COMMENT_DIVIDER = 2;
    public static final int VIEW_TYPE_COMMENT = 3;
    public static final int VIEW_TYPE_STICKER = 4;
    public static final int VIEW_TYPE_COMMENT_NO_PROFILE = 5;
    public static final int VIEW_TYPE_STICKER_NO_PROFILE = 6;

    private OnCommentClickListener onCommentClickListener;
    private OnCommentLongClickListener onCommentLongClickListener;
    private ImageFileViewHolder.OnImageFileClickListener onImageFileClickListener;
    private NormalFileViewHolder.OnFileClickListener onFileClickListener;

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_FILE:
                return NormalFileViewHolder.newInstance(parent, onFileClickListener);
            case VIEW_TYPE_IMAGE:
                return ImageFileViewHolder.newInstance(parent, onImageFileClickListener);
            case VIEW_TYPE_COMMENT:
                return TextCommentViewHolder.newInstance(parent, onCommentClickListener, onCommentLongClickListener);
            case VIEW_TYPE_STICKER:
                return StickerCommentViewHolder.newInstance(parent, onCommentClickListener, onCommentLongClickListener);
            case VIEW_TYPE_COMMENT_NO_PROFILE:
                return TextCommentViewHolder.newInstanceNoProfile(parent, onCommentClickListener, onCommentLongClickListener);
            case VIEW_TYPE_STICKER_NO_PROFILE:
                return StickerCommentViewHolder.newInstanceNoProfile(parent, onCommentClickListener, onCommentLongClickListener);
            default:
            case VIEW_TYPE_COMMENT_DIVIDER:
                return DividerViewHolder.newInstance(parent);
        }
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        if (holder instanceof CellDividerUpdater) {
            CellDividerUpdater updater = (CellDividerUpdater) holder;
            if (position >= getItemCount() - 1) {
                updater.cellUpdater(true);
            } else {
                ResMessages.OriginalMessage current = getItem(position);
                ResMessages.OriginalMessage after = getItem(position + 1);
                updater.cellUpdater(current.writerId != after.writerId);
            }
        }

    }

    public void modifyStarredStateByMessageId(long messageId, boolean starred) {
        int itemCount = getItemCount();
        for (int i = 0; i < itemCount; i++) {
            ResMessages.OriginalMessage message = getItem(i);
            if (message != null
                    &&
                    (message instanceof ResMessages.CommentMessage
                            || message instanceof ResMessages.CommentStickerMessage)
                    && message.id == messageId) {
                message.isStarred = starred;
                break;
            }
        }
    }

    public int findIndexOfMessageId(long messageId) {
        int itemCount = getItemCount();
        for (int count = itemCount - 1; count >= 0; count--) {
            int itemViewType = getItemViewType(count);
            if (itemViewType == VIEW_TYPE_COMMENT) {
                ResMessages.CommentMessage item = getItem(count);
                if (item.id == messageId) {
                    return count;
                }
            }
        }
        return -1;
    }

    public void setOnCommentClickListener(OnCommentClickListener onCommentClickListener) {
        this.onCommentClickListener = onCommentClickListener;
    }

    public void setOnCommentLongClickListener(OnCommentLongClickListener onCommentLongClickListener) {
        this.onCommentLongClickListener = onCommentLongClickListener;
    }

    public void setOnImageFileClickListener(ImageFileViewHolder.OnImageFileClickListener onImageFileClickListener) {
        this.onImageFileClickListener = onImageFileClickListener;
    }

    public void setOnFileClickListener(NormalFileViewHolder.OnFileClickListener onFileClickListener) {
        this.onFileClickListener = onFileClickListener;
    }

}

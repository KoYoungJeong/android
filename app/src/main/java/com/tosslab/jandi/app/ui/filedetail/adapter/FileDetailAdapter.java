package com.tosslab.jandi.app.ui.filedetail.adapter;

import android.view.ViewGroup;

import com.tosslab.jandi.app.ui.base.adapter.MultiItemRecyclerAdapter;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.ui.filedetail.adapter.viewholder.NormalFileViewHolder;
import com.tosslab.jandi.app.ui.filedetail.adapter.viewholder.comment.CommentViewHolder;
import com.tosslab.jandi.app.ui.filedetail.adapter.viewholder.DividerViewHolder;
import com.tosslab.jandi.app.ui.filedetail.adapter.viewholder.FileViewHolder;
import com.tosslab.jandi.app.ui.filedetail.adapter.viewholder.ImageFileViewHolder;
import com.tosslab.jandi.app.ui.filedetail.adapter.viewholder.comment.StickerViewHolder;

/**
 * Created by tonyjs on 16. 1. 19..
 */
public class FileDetailAdapter extends MultiItemRecyclerAdapter {

    public static final int VIEW_TYPE_FILE = 0;
    public static final int VIEW_TYPE_IMAGE = 1;
    public static final int VIEW_TYPE_COMMENT_DIVIDER = 2;
    public static final int VIEW_TYPE_COMMENT = 3;
    public static final int VIEW_TYPE_STICKER = 4;

    private long roomId;

    public FileDetailAdapter(long roomId) {
        this.roomId = roomId;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_FILE:
                return NormalFileViewHolder.newInstance(parent);
            case VIEW_TYPE_IMAGE:
                return ImageFileViewHolder.newInstance(parent, roomId);
            case VIEW_TYPE_COMMENT:
                return CommentViewHolder.newInstance(parent);
            case VIEW_TYPE_STICKER:
                return StickerViewHolder.newInstance(parent);
            default:
            case VIEW_TYPE_COMMENT_DIVIDER:
                return DividerViewHolder.newInstance(parent);
        }
    }


}

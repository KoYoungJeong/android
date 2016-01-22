package com.tosslab.jandi.app.ui.filedetail.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.adapter.MultiItemRecyclerAdapter;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.ui.filedetail.adapter.viewholder.FileViewHolder;

/**
 * Created by tonyjs on 16. 1. 19..
 */
public class FileDetailAdapter extends MultiItemRecyclerAdapter {

    public static final int VIEW_TYPE_FILE = 0;
    public static final int VIEW_TYPE_IMAGE = 1;
    public static final int VIEW_TYPE_COMMENT = 2;
    public static final int VIEW_TYPE_StickerComment = 3;

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_FILE:
                return FileViewHolder.newInstance(parent);
            default:
                return null;
        }
    }




}

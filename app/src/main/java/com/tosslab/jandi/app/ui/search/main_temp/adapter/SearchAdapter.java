package com.tosslab.jandi.app.ui.search.main_temp.adapter;

import android.view.ViewGroup;

import com.tosslab.jandi.app.ui.base.adapter.MultiItemRecyclerAdapter;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.ui.search.main_temp.adapter.viewholder.MessageHeaderViewHolder;
import com.tosslab.jandi.app.ui.search.main_temp.adapter.viewholder.MessageItemViewHolder;
import com.tosslab.jandi.app.ui.search.main_temp.adapter.viewholder.RoomHeaderViewHolder;
import com.tosslab.jandi.app.ui.search.main_temp.adapter.viewholder.RoomItemViewHolder;
import com.tosslab.jandi.app.utils.UiUtils;

/**
 * Created by tee on 16. 7. 21..
 */
public class SearchAdapter extends MultiItemRecyclerAdapter {

    public static final int ITEM_TYPE_MESSAGE_HEADER = 0x01;
    public static final int ITEM_TYPE_MESSAGE_ITEM = 0x02;
    public static final int ITEM_TYPE_ROOM_HEADER = 0x03;
    public static final int ITEM_TYPE_ROOM_ITEM = 0x04;
    boolean isMessageItemFold = false;
    boolean isRoomItemFold = false;

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case ITEM_TYPE_MESSAGE_HEADER:
                return MessageHeaderViewHolder.newInstance(parent);
            case ITEM_TYPE_MESSAGE_ITEM:
                return MessageItemViewHolder.newInstance(parent);
            case ITEM_TYPE_ROOM_HEADER:
                return RoomHeaderViewHolder.newInstance(parent);
            case ITEM_TYPE_ROOM_ITEM:
                return RoomItemViewHolder.newInstance(parent);
        }
        return RoomHeaderViewHolder.newInstance(parent);
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (isMessageItemFold) {
            if (getItemViewType(position) == ITEM_TYPE_MESSAGE_HEADER) {
                holder.itemView.getLayoutParams().height = 0;
            }

            if (getItemViewType(position) == ITEM_TYPE_MESSAGE_ITEM) {
                holder.itemView.getLayoutParams().height = 0;
            }
        } else {
            if (getItemViewType(position) == ITEM_TYPE_MESSAGE_HEADER) {
                holder.itemView.getLayoutParams().height = (int) UiUtils.getPixelFromDp(96);
            }

            if (getItemViewType(position) == ITEM_TYPE_MESSAGE_ITEM) {
                holder.itemView.getLayoutParams().height = (int) UiUtils.getPixelFromDp(140);
            }
        }

        if (isRoomItemFold) {
            if (getItemViewType(position) == ITEM_TYPE_ROOM_HEADER) {
                holder.itemView.getLayoutParams().height = 0;
            }

            if (getItemViewType(position) == ITEM_TYPE_ROOM_ITEM) {
                holder.itemView.getLayoutParams().height = 0;
            }
        } else {
            if (getItemViewType(position) == ITEM_TYPE_ROOM_HEADER) {
                holder.itemView.getLayoutParams().height = (int) UiUtils.getPixelFromDp(48);
            }

            if (getItemViewType(position) == ITEM_TYPE_ROOM_ITEM) {
                holder.itemView.getLayoutParams().height = (int) UiUtils.getPixelFromDp(75);
            }
        }
    }

    @Override
    public synchronized <ITEM> ITEM getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public synchronized int getItemCount() {
        return super.getItemCount();
    }

    @Override
    public synchronized int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void onClickHeader(long headerId, boolean isFold) {
        if (headerId == 1) {
            isMessageItemFold = isFold;
        } else {
            isRoomItemFold = isFold;
        }

        notifyItemRangeChanged(0, getItemCount());
    }
}

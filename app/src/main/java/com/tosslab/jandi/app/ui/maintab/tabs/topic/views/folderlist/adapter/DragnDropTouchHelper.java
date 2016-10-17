package com.tosslab.jandi.app.ui.maintab.tabs.topic.views.folderlist.adapter;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;

/**
 * Created by tee on 16. 1. 22..
 */
public class DragnDropTouchHelper extends ItemTouchHelper.SimpleCallback {

    private TopicFolderSettingAdapter topicFolderSettingAdapter;
    private OnDragStartListener onDragStartListener;

    private long startFolderId;
    private int dropPosition;

    public DragnDropTouchHelper(TopicFolderSettingAdapter topicFolderChooseAdapter,
                                OnDragStartListener onDragStartListener) {
        super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.topicFolderSettingAdapter = topicFolderChooseAdapter;
        this.onDragStartListener = onDragStartListener;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        if (target.getAdapterPosition() != dropPosition) {
            dropPosition = target.getAdapterPosition();
        }
        topicFolderSettingAdapter.swap(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public int getDragDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        if (viewHolder.getItemViewType() != TopicFolderSettingAdapter.TYPE_FOLDER_LIST) {
            return 0;
        } else {
            return super.getDragDirs(recyclerView, viewHolder);
        }
    }

    // 스와이프를 안쓰려면 이렇게..
    @Override
    public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        return 0;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        // nothing
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            startFolderId = topicFolderSettingAdapter.getItem(viewHolder.getAdapterPosition()).getId();
            dropPosition = viewHolder.getAdapterPosition();
            viewHolder.itemView.setBackgroundResource(R.drawable.list_move_shadow);
            if (onDragStartListener != null) {
                onDragStartListener.onDragStart();
            }
        } else if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
            topicFolderSettingAdapter.sendChangeSeq(startFolderId, dropPosition + 1);
        }
        super.onSelectedChanged(viewHolder, actionState);
    }

    public interface OnDragStartListener {
        void onDragStart();
    }

}

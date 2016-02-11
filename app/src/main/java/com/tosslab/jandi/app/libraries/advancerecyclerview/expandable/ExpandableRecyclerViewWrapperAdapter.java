/*
 *    Copyright (C) 2015 Haruki Hasegawa
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.tosslab.jandi.app.libraries.advancerecyclerview.expandable;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.tosslab.jandi.app.libraries.advancerecyclerview.utils.BaseWrapperAdapter;
import com.tosslab.jandi.app.libraries.advancerecyclerview.utils.WrapperAdapterUtils;


class ExpandableRecyclerViewWrapperAdapter
        extends BaseWrapperAdapter<RecyclerView.ViewHolder> {
    private static final String TAG = "ARVExpandableWrapper";

    private static final int VIEW_TYPE_FLAG_IS_GROUP = ExpandableAdapterHelper.VIEW_TYPE_FLAG_IS_GROUP;

    private static final int STATE_FLAG_INITIAL_VALUE = -1;

    private ExpandableItemAdapter mExpandableItemAdapter;
    private ExpandablePositionTranslator mPositionTranslator;

    private RecyclerViewExpandableItemManager.OnGroupExpandListener mOnGroupExpandListener;
    private RecyclerViewExpandableItemManager.OnGroupCollapseListener mOnGroupCollapseListener;

    public ExpandableRecyclerViewWrapperAdapter(RecyclerView.Adapter<RecyclerView.ViewHolder> adapter, int[] expandedItemsSavedState) {
        super(adapter);

        mExpandableItemAdapter = getExpandableItemAdapter(adapter);
        if (mExpandableItemAdapter == null) {
            throw new IllegalArgumentException("adapter does not implement RecyclerViewExpandableListManager");
        }

        mPositionTranslator = new ExpandablePositionTranslator();
        mPositionTranslator.build(mExpandableItemAdapter);

        if (expandedItemsSavedState != null) {
            // NOTE: do not call hook routines and listener methods
            mPositionTranslator.restoreExpandedGroupItems(expandedItemsSavedState, null, null, null);
        }
    }

    private static ExpandableItemAdapter getExpandableItemAdapter(RecyclerView.Adapter adapter) {
        return WrapperAdapterUtils.findWrappedAdapter(adapter, ExpandableItemAdapter.class);
    }

    private static void safeUpdateExpandStateFlags(RecyclerView.ViewHolder holder, int flags) {
        if (!(holder instanceof ExpandableItemViewHolder)) {
            return;
        }

        final ExpandableItemViewHolder holder2 = (ExpandableItemViewHolder) holder;

        final int curFlags = holder2.getExpandStateFlags();
        final int mask = ~RecyclerViewExpandableItemManager.STATE_FLAG_IS_UPDATED;

        // append UPDATED flag
        if ((curFlags == STATE_FLAG_INITIAL_VALUE) || (((curFlags ^ flags) & mask) != 0)) {
            flags |= RecyclerViewExpandableItemManager.STATE_FLAG_IS_UPDATED;
        }

        holder2.setExpandStateFlags(flags);
    }

    @Override
    protected void onRelease() {
        super.onRelease();

        mExpandableItemAdapter = null;
        mOnGroupExpandListener = null;
        mOnGroupCollapseListener = null;
    }

    @Override
    public int getItemCount() {
        return mPositionTranslator.getItemCount();
    }

    @Override
    public long getItemId(int position) {
        if (mExpandableItemAdapter == null) {
            return RecyclerView.NO_ID;
        }

        final long expandablePosition = mPositionTranslator.getExpandablePosition(position);
        final int groupPosition = ExpandableAdapterHelper.getPackedPositionGroup(expandablePosition);
        final int childPosition = ExpandableAdapterHelper.getPackedPositionChild(expandablePosition);

        if (childPosition == RecyclerView.NO_POSITION) {
            final long groupId = mExpandableItemAdapter.getGroupId(groupPosition);
            return ExpandableAdapterHelper.getCombinedGroupId(groupId);
        } else {
            final long groupId = mExpandableItemAdapter.getGroupId(groupPosition);
            final long childId = mExpandableItemAdapter.getChildId(groupPosition, childPosition);
            return ExpandableAdapterHelper.getCombinedChildId(groupId, childId);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mExpandableItemAdapter == null) {
            return 0;
        }

        final long expandablePosition = mPositionTranslator.getExpandablePosition(position);
        final int groupPosition = ExpandableAdapterHelper.getPackedPositionGroup(expandablePosition);
        final int childPosition = ExpandableAdapterHelper.getPackedPositionChild(expandablePosition);

        final int type;

        if (childPosition == RecyclerView.NO_POSITION) {
            type = mExpandableItemAdapter.getGroupItemViewType(groupPosition);
        } else {
            type = mExpandableItemAdapter.getChildItemViewType(groupPosition, childPosition);
        }

        if ((type & VIEW_TYPE_FLAG_IS_GROUP) != 0) {
            throw new IllegalStateException("Illegal view type (type = " + Integer.toHexString(type) + ")");
        }

        return (childPosition == RecyclerView.NO_POSITION) ? (type | VIEW_TYPE_FLAG_IS_GROUP) : (type);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mExpandableItemAdapter == null) {
            return null;
        }

        final int maskedViewType = (viewType & (~VIEW_TYPE_FLAG_IS_GROUP));

        final RecyclerView.ViewHolder holder;

        if ((viewType & VIEW_TYPE_FLAG_IS_GROUP) != 0) {
            holder = mExpandableItemAdapter.onCreateGroupViewHolder(parent, maskedViewType);
        } else {
            holder = mExpandableItemAdapter.onCreateChildViewHolder(parent, maskedViewType);
        }

        if (holder instanceof ExpandableItemViewHolder) {
            ((ExpandableItemViewHolder) holder).setExpandStateFlags(STATE_FLAG_INITIAL_VALUE);
        }

        return holder;

    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (mExpandableItemAdapter == null) {
            return;
        }

        final long expandablePosition = mPositionTranslator.getExpandablePosition(position);
        final int groupPosition = ExpandableAdapterHelper.getPackedPositionGroup(expandablePosition);
        final int childPosition = ExpandableAdapterHelper.getPackedPositionChild(expandablePosition);
        final int viewType = (holder.getItemViewType() & (~VIEW_TYPE_FLAG_IS_GROUP));

        // update flags
        int flags = 0;

        if (childPosition == RecyclerView.NO_POSITION) {
            flags |= RecyclerViewExpandableItemManager.STATE_FLAG_IS_GROUP;
        } else {
            flags |= RecyclerViewExpandableItemManager.STATE_FLAG_IS_CHILD;
        }

        if (mPositionTranslator.isGroupExpanded(groupPosition)) {
            flags |= RecyclerViewExpandableItemManager.STATE_FLAG_IS_EXPANDED;
        }

        safeUpdateExpandStateFlags(holder, flags);

        if (childPosition == RecyclerView.NO_POSITION) {
            mExpandableItemAdapter.onBindGroupViewHolder(holder, groupPosition, viewType);
        } else {
            mExpandableItemAdapter.onBindChildViewHolder(holder, groupPosition, childPosition, viewType);
        }
    }

    private void rebuildPositionTranslator() {
        if (mPositionTranslator != null) {
            int[] savedState = mPositionTranslator.getSavedStateArray();
            mPositionTranslator.build(mExpandableItemAdapter);

            // NOTE: do not call hook routines and listener methods
            mPositionTranslator.restoreExpandedGroupItems(savedState, null, null, null);
        }
    }

    @Override
    protected void onHandleWrappedAdapterChanged() {
        rebuildPositionTranslator();
        super.onHandleWrappedAdapterChanged();
    }

    @Override
    protected void onHandleWrappedAdapterItemRangeChanged(int positionStart, int itemCount) {
        super.onHandleWrappedAdapterItemRangeChanged(positionStart, itemCount);
    }

    @Override
    protected void onHandleWrappedAdapterItemRangeInserted(int positionStart, int itemCount) {
        rebuildPositionTranslator();
        super.onHandleWrappedAdapterItemRangeInserted(positionStart, itemCount);
    }

    @Override
    protected void onHandleWrappedAdapterItemRangeRemoved(int positionStart, int itemCount) {
        if (itemCount == 1) {
            final long expandablePosition = mPositionTranslator.getExpandablePosition(positionStart);
            final int groupPosition = ExpandableAdapterHelper.getPackedPositionGroup(expandablePosition);
            final int childPosition = ExpandableAdapterHelper.getPackedPositionChild(expandablePosition);

            if (childPosition == RecyclerView.NO_POSITION) {
                mPositionTranslator.removeGroupItem(groupPosition);
            } else {
                mPositionTranslator.removeChildItem(groupPosition, childPosition);
            }
        } else {
            rebuildPositionTranslator();
        }

        super.onHandleWrappedAdapterItemRangeRemoved(positionStart, itemCount);
    }

    @Override
    protected void onHandleWrappedAdapterRangeMoved(int fromPosition, int toPosition, int itemCount) {
        rebuildPositionTranslator();
        super.onHandleWrappedAdapterRangeMoved(fromPosition, toPosition, itemCount);
    }


    // NOTE: This method is called from RecyclerViewExpandableItemManager
    /*package*/
    @SuppressWarnings("unchecked")
    boolean onTapItem(RecyclerView.ViewHolder holder, int position, int x, int y) {
        if (mExpandableItemAdapter == null) {
            return false;
        }

        final int flatPosition = position;
        final long expandablePosition = mPositionTranslator.getExpandablePosition(flatPosition);
        final int groupPosition = ExpandableAdapterHelper.getPackedPositionGroup(expandablePosition);
        final int childPosition = ExpandableAdapterHelper.getPackedPositionChild(expandablePosition);

        if (childPosition != RecyclerView.NO_POSITION) {
            return false;
        }

        final boolean expand = !(mPositionTranslator.isGroupExpanded(groupPosition));

        boolean result = mExpandableItemAdapter.onCheckCanExpandOrCollapseGroup(holder, groupPosition, x, y, expand);

        if (!result) {
            return false;
        }

        if (expand) {
            expandGroup(groupPosition, true);
        } else {
            collapseGroup(groupPosition, true);
        }

        return true;
    }

    /*package*/ boolean collapseGroup(int groupPosition, boolean fromUser) {
        if (!mPositionTranslator.isGroupExpanded(groupPosition)) {
            return false;
        }

        // call hook method
        if (!mExpandableItemAdapter.onHookGroupCollapse(groupPosition, fromUser)) {
            return false;
        }

        if (mPositionTranslator.collapseGroup(groupPosition)) {
            final long packedPosition = ExpandableAdapterHelper.getPackedPositionForGroup(groupPosition);
            final int flatPosition = mPositionTranslator.getFlatPosition(packedPosition);
            final int childCount = mPositionTranslator.getChildCount(groupPosition);

            notifyItemRangeRemoved(flatPosition + 1, childCount);
        }


        {
            final long packedPosition = ExpandableAdapterHelper.getPackedPositionForGroup(groupPosition);
            final int flatPosition = mPositionTranslator.getFlatPosition(packedPosition);

            notifyItemChanged(flatPosition);
        }

        // raise onGroupCollapse() event
        if (mOnGroupCollapseListener != null) {
            mOnGroupCollapseListener.onGroupCollapse(groupPosition, fromUser);
        }

        return true;
    }

    /*package*/
    public boolean expandGroup(int groupPosition, boolean fromUser) {
        if (mPositionTranslator.isGroupExpanded(groupPosition)) {
            return false;
        }

        // call hook method
        if (!mExpandableItemAdapter.onHookGroupExpand(groupPosition, fromUser)) {
            return false;
        }

        if (mPositionTranslator.expandGroup(groupPosition)) {
            final long packedPosition = ExpandableAdapterHelper.getPackedPositionForGroup(groupPosition);
            final int flatPosition = mPositionTranslator.getFlatPosition(packedPosition);
            final int childCount = mPositionTranslator.getChildCount(groupPosition);

            notifyItemRangeInserted(flatPosition + 1, childCount);
        }

        {
            final long packedPosition = ExpandableAdapterHelper.getPackedPositionForGroup(groupPosition);
            final int flatPosition = mPositionTranslator.getFlatPosition(packedPosition);

            notifyItemChanged(flatPosition);
        }

        // raise onGroupExpand() event
        if (mOnGroupExpandListener != null) {
            mOnGroupExpandListener.onGroupExpand(groupPosition, fromUser);
        }

        return true;
    }

    /*package*/ boolean isGroupExpanded(int groupPosition) {
        return mPositionTranslator.isGroupExpanded(groupPosition);
    }

    /*package*/ long getExpandablePosition(int flatPosition) {
        return mPositionTranslator.getExpandablePosition(flatPosition);
    }

    /*package*/ int getFlatPosition(long packedPosition) {
        return mPositionTranslator.getFlatPosition(packedPosition);
    }

    /*package*/ int[] getExpandedItemsSavedStateArray() {
        if (mPositionTranslator != null) {
            return mPositionTranslator.getSavedStateArray();
        } else {
            return null;
        }
    }

    /*package*/ void setOnGroupExpandListener(RecyclerViewExpandableItemManager.OnGroupExpandListener listener) {
        mOnGroupExpandListener = listener;
    }

    /*package*/ void setOnGroupCollapseListener(RecyclerViewExpandableItemManager.OnGroupCollapseListener listener) {
        mOnGroupCollapseListener = listener;
    }

    /*package*/ void restoreState(int[] adapterSavedState, boolean callHook, boolean callListeners) {
        mPositionTranslator.restoreExpandedGroupItems(
                adapterSavedState,
                (callHook ? mExpandableItemAdapter : null),
                (callListeners ? mOnGroupExpandListener : null),
                (callListeners ? mOnGroupCollapseListener : null));
    }

    /*package*/ void notifyGroupItemChanged(int groupPosition) {
        final long packedPosition = ExpandableAdapterHelper.getPackedPositionForGroup(groupPosition);
        final int flatPosition = mPositionTranslator.getFlatPosition(packedPosition);

        if (flatPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(flatPosition);
        }
    }

    /*package*/ void notifyGroupAndChildrenItemsChanged(int groupPosition) {
        final long packedPosition = ExpandableAdapterHelper.getPackedPositionForGroup(groupPosition);
        final int flatPosition = mPositionTranslator.getFlatPosition(packedPosition);
        final int visibleChildCount = mPositionTranslator.getVisibleChildCount(groupPosition);

        if (flatPosition != RecyclerView.NO_POSITION) {
            notifyItemRangeChanged(flatPosition, 1 + visibleChildCount);
        }
    }

    /*package*/ void notifyChildrenOfGroupItemChanged(int groupPosition) {
        final int visibleChildCount = mPositionTranslator.getVisibleChildCount(groupPosition);

        // notify if the group is expanded
        if (visibleChildCount > 0) {
            final long packedPosition = ExpandableAdapterHelper.getPackedPositionForChild(groupPosition, 0);
            final int flatPosition = mPositionTranslator.getFlatPosition(packedPosition);

            if (flatPosition != RecyclerView.NO_POSITION) {
                notifyItemRangeChanged(flatPosition, visibleChildCount);
            }
        }
    }

    /*package*/ void notifyChildItemChanged(int groupPosition, int childPosition) {
        notifyChildItemRangeChanged(groupPosition, childPosition, 1);
    }

    /*package*/ void notifyChildItemRangeChanged(int groupPosition, int childPositionStart, int itemCount) {
        final int visibleChildCount = mPositionTranslator.getVisibleChildCount(groupPosition);

        // notify if the group is expanded
        if ((visibleChildCount > 0) && (childPositionStart < visibleChildCount)) {
            final long packedPosition = ExpandableAdapterHelper.getPackedPositionForChild(groupPosition, 0);
            final int flatPosition = mPositionTranslator.getFlatPosition(packedPosition);

            if (flatPosition != RecyclerView.NO_POSITION) {
                final int startPosition = flatPosition + childPositionStart;
                final int count = Math.min(itemCount, (visibleChildCount - childPositionStart));

                notifyItemRangeChanged(startPosition, count);
            }
        }
    }

    /*package*/ void notifyChildItemInserted(int groupPosition, int childPosition) {
        mPositionTranslator.insertChildItem(groupPosition, childPosition);

        final long packedPosition = ExpandableAdapterHelper.getPackedPositionForChild(groupPosition, childPosition);
        final int flatPosition = mPositionTranslator.getFlatPosition(packedPosition);

        if (flatPosition != RecyclerView.NO_POSITION) {
            notifyItemInserted(flatPosition);
        }
    }

    /*package*/ void notifyChildItemRangeInserted(int groupPosition, int childPositionStart, int itemCount) {
        mPositionTranslator.insertChildItems(groupPosition, childPositionStart, itemCount);

        final long packedPosition = ExpandableAdapterHelper.getPackedPositionForChild(groupPosition, childPositionStart);
        final int flatPosition = mPositionTranslator.getFlatPosition(packedPosition);

        if (flatPosition != RecyclerView.NO_POSITION) {
            notifyItemRangeInserted(flatPosition, itemCount);
        }
    }

    /*package*/ void notifyChildItemRemoved(int groupPosition, int childPosition) {
        final long packedPosition = ExpandableAdapterHelper.getPackedPositionForChild(groupPosition, childPosition);
        final int flatPosition = mPositionTranslator.getFlatPosition(packedPosition);

        mPositionTranslator.removeChildItem(groupPosition, childPosition);

        if (flatPosition != RecyclerView.NO_POSITION) {
            notifyItemRemoved(flatPosition);
        }
    }

    /*package*/ void notifyChildItemRangeRemoved(int groupPosition, int childPositionStart, int itemCount) {
        final long packedPosition = ExpandableAdapterHelper.getPackedPositionForChild(groupPosition, childPositionStart);
        final int flatPosition = mPositionTranslator.getFlatPosition(packedPosition);

        mPositionTranslator.removeChildItems(groupPosition, childPositionStart, itemCount);

        if (flatPosition != RecyclerView.NO_POSITION) {
            notifyItemRangeRemoved(flatPosition, itemCount);
        }
    }

    /*package*/ void notifyGroupItemInserted(int groupPosition) {
        int insertedCount = mPositionTranslator.insertGroupItem(groupPosition);
        if (insertedCount > 0) {
            final long packedPosition = ExpandableAdapterHelper.getPackedPositionForGroup(groupPosition);
            final int flatPosition = mPositionTranslator.getFlatPosition(packedPosition);

            notifyItemInserted(flatPosition);
        }
    }

    /*package*/ void notifyGroupItemRangeInserted(int groupPositionStart, int count) {
        int insertedCount = mPositionTranslator.insertGroupItems(groupPositionStart, count);
        if (insertedCount > 0) {
            final long packedPosition = ExpandableAdapterHelper.getPackedPositionForGroup(groupPositionStart);
            final int flatPosition = mPositionTranslator.getFlatPosition(packedPosition);

            notifyItemRangeInserted(flatPosition, insertedCount);
        }
    }

    /*package*/ void notifyGroupItemRemoved(int groupPosition) {
        final long packedPosition = ExpandableAdapterHelper.getPackedPositionForGroup(groupPosition);
        final int flatPosition = mPositionTranslator.getFlatPosition(packedPosition);

        int removedCount = mPositionTranslator.removeGroupItem(groupPosition);
        if (removedCount > 0) {
            notifyItemRangeRemoved(flatPosition, removedCount);
        }
    }

    /*package*/ void notifyGroupItemRangeRemoved(int groupPositionStart, int count) {
        final long packedPosition = ExpandableAdapterHelper.getPackedPositionForGroup(groupPositionStart);
        final int flatPosition = mPositionTranslator.getFlatPosition(packedPosition);

        int removedCount = mPositionTranslator.removeGroupItems(groupPositionStart, count);
        if (removedCount > 0) {
            notifyItemRangeRemoved(flatPosition, removedCount);
        }
    }
}

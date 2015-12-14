package com.tosslab.jandi.app.ui.message.v2.adapter;

import android.support.v7.widget.RecyclerView;

import com.tosslab.jandi.app.local.orm.domain.SendMessage;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.message.to.DummyMessageLink;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.RecyclerBodyViewHolder;

import java.util.List;

public abstract class MessageAdapter extends RecyclerView.Adapter<RecyclerBodyViewHolder> {
    public abstract ResMessages.Link getItem(int position);

    public abstract void addAll(int position, List<ResMessages.Link> messages);

    public abstract void setOldLoadingComplete();

    public abstract void setOldNoMoreLoading();

    public abstract int indexByMessageId(int linkId);

    public abstract int indexOfLinkId(int linkId);

    public abstract void clear();

    public abstract List<Integer> indexByFeedbackId(int messageId);

    public abstract void updateMessageId(long localId, int messageId);

    public abstract void addDummyMessage(DummyMessageLink dummyMessageLink);

    public abstract void updateDummyMessageState(long localId, SendMessage.Status state);

    public abstract int getDummeMessagePositionByLocalId(long localId);

    public abstract void remove(int position);

    public abstract void setMarker(int lastMarker);

    public abstract void setMoreFromNew(boolean isMoreNew);

    public abstract void setNewLoadingComplete();

    public abstract void setNewNoMoreLoading();

    public abstract void setOnItemClickListener(MessageListAdapter.OnItemClickListener onItemClickListener);

    public abstract void setOnItemLongClickListener(MessageListAdapter.OnItemLongClickListener onItemLongClickListener);

    public abstract void setTeamId(int teamId);

    public abstract void setRoomId(int roomId);

    public abstract int getDummyMessageCount();

    public abstract int getRoomId();

    public abstract void setEntityId(int entityId);

    public abstract void modifyStarredStateByPosition(int position, boolean isStarred);

    public abstract void setLastReadLinkId(int lastReadLinkId);

    public abstract int getLastReadLinkId();

    enum MoreState {
        Idle, Loading, Nope
    }

    enum AnimState {
        Idle, Loading, End
    }

    public interface OnItemClickListener {
        void onItemClick(RecyclerView.Adapter adapter, int position);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(RecyclerView.Adapter adapter, int position);
    }
}

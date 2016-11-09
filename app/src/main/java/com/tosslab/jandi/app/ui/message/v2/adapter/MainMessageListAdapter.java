package com.tosslab.jandi.app.ui.message.v2.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tosslab.jandi.app.events.messages.RefreshOldMessageEvent;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.poll.Poll;
import com.tosslab.jandi.app.ui.message.to.DummyMessageLink;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.BodyViewFactory;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.BodyViewHolder;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.RecyclerBodyViewHolder;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.TypeUtil;
import com.tosslab.jandi.app.ui.message.v2.domain.MessagePointer;
import com.tosslab.jandi.app.ui.message.v2.domain.Room;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.greenrobot.event.EventBus;

public class MainMessageListAdapter extends RecyclerView.Adapter<RecyclerBodyViewHolder>
        implements MessageListHeaderAdapter.MessageItemDate, MessageListAdapterView, MessageListAdapterModel {

    Context context;
    AnimState markerAnimState = AnimState.Idle;
    MoreState oldMoreState;
    MainMessageListAdapter.OnItemClickListener onItemClickListener;
    MainMessageListAdapter.OnItemLongClickListener onItemLongClickListener;
    List<ResMessages.Link> links;
    // 소켓으로 데이터를 받지 않은 경우에 저장하기 위한 정보
    private Room room;
    private MessagePointer messagePointer;

    private Map<ResMessages.Link, Integer> itemTypes;
    private Lock lock;

    public MainMessageListAdapter(Context context, Room room) {
        this.context = context;
        this.room = room;
        oldMoreState = MoreState.Idle;
        links = new ArrayList<>();
        setHasStableIds(true);
        itemTypes = new WeakHashMap<>();

        lock = new ReentrantLock();
    }

    @Override
    public RecyclerBodyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        BodyViewHolder viewHolder = BodyViewFactory.createViewHolder(viewType);
        View convertView = LayoutInflater.from(context).inflate(viewHolder.getLayoutId(), parent, false);
        viewHolder.initView(convertView);

        return new RecyclerBodyViewHolder(convertView, viewHolder);
    }

    @Override
    public void onBindViewHolder(RecyclerBodyViewHolder viewHolder, int position) {
        ResMessages.Link item = getItem(position);
        if (item == null) {
            LogUtil.e("onBindViewHolder / Item is Null!!!!!!!!!!!");
            return;
        }

        BodyViewHolder bodyViewHolder = viewHolder.getViewHolder();

        try {
            bodyViewHolder.bindData(item, room.getTeamId(), room.getRoomId(), room.getEntityId());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (position > 0 && position < getItemCount() - 1 - getDummyMessageCount()) {
            bodyViewHolder.setLastReadViewVisible(item.id, messagePointer.getLastReadLinkId());
        } else {
            bodyViewHolder.setLastReadViewVisible(0, -1);
        }

        if (position <= 2 && oldMoreState == MoreState.Idle) {
            oldMoreState = MainMessageListAdapter.MoreState.Loading;
            EventBus.getDefault().post(new RefreshOldMessageEvent());
        }

        bodyViewHolder.setOnItemClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(MainMessageListAdapter.this, position);
            }
        });

        bodyViewHolder.setOnItemLongClickListener(v -> {
            if (onItemLongClickListener != null) {
                return onItemLongClickListener.onItemLongClick(MainMessageListAdapter.this, position);
            }
            return false;
        });

    }

    @Override
    public int getItemViewType(int position) {
        ResMessages.Link currentLink = getItem(position);
        ResMessages.Link previousLink = null;
        ResMessages.Link nextLink = null;
        if (position > 0) {
            previousLink = getItem(position - 1);
        }

        if (position < getItemCount() - 1) {
            nextLink = getItem(position + 1);
        }

        if (!itemTypes.containsKey(currentLink)) {
            int contentType = BodyViewFactory.getContentType(previousLink, currentLink, nextLink, room.getRoomId());
            itemTypes.put(currentLink, contentType);
            return contentType;
        } else {
            return itemTypes.get(currentLink);
        }
    }

    @Override
    public void addAll(int position, List<ResMessages.Link> links) {

        lock.lock();
        try {
            if (links == null || links.isEmpty()) {
                return;
            }

            int minPosition = Math.min(position, getItemCount());

            if (minPosition != 0) {
                for (int idx = links.size() - 1; idx >= 0; idx--) {
                    ResMessages.Link link = links.get(idx);
                    if (TextUtils.equals(link.status, "archived")) {
                        links.remove(idx);
                        int searchedPosition = indexByMessageId(link.messageId);
                        if (searchedPosition < 0) {
                            continue;
                        }

                        if (TextUtils.equals(link.message.contentType, "file")) {
                            ResMessages.Link originLink = MainMessageListAdapter.this.getItem(searchedPosition);
                            originLink.message = link.message;
                            originLink.status = "archived";
                            itemTypes.remove(originLink);
                            getItemViewType(searchedPosition);
                        } else {
                            remove(searchedPosition);
                            minPosition--;
                        }
                    }
                }
            }

            int dummyMessageCount = getDummyMessageCount();
            int beforePosition;
            if (minPosition > dummyMessageCount) {
                this.links.addAll(minPosition - dummyMessageCount, links);
                beforePosition = minPosition - dummyMessageCount - 1;
                for (int idx = 0; idx < links.size(); idx++) {
                    getItemViewType(minPosition - dummyMessageCount + idx);
                }
            } else {
                this.links.addAll(minPosition, links);
                beforePosition = minPosition - 1;
                for (int idx = 0; idx < links.size(); idx++) {
                    getItemViewType(minPosition + idx);
                }
            }
            if (beforePosition >= 0) {
                itemTypes.remove(getItem(beforePosition));
                getItemViewType(beforePosition);
            }

            if (beforePosition + links.size() + 1 < getItemCount()) {
                itemTypes.remove(getItem(beforePosition + links.size() + 1));
                getItemViewType(beforePosition + links.size() + 1);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void remove(int position) {
        lock.lock();
        try {

            ResMessages.Link removed = links.remove(position);
            itemTypes.remove(removed);

            if (position > 0) {
                itemTypes.remove(getItem(position - 1));
                getItemViewType(position - 1);
            }
            if (position < getItemCount() - 1) {
                itemTypes.remove(getItem(position));
                getItemViewType(position);
            }

        } catch (Exception e) {
            LogUtil.e(Log.getStackTraceString(e));
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void updateCachedType(int position) {
        itemTypes.remove(getItem(position));
        getItemViewType(position);
    }

    @Override
    public ResMessages.Link getItem(int position) {
        lock.lock();
        try {
            if (position < 0 || position >= getItemCount()) {
                return null;
            }
            return links.get(position);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void setOldLoadingComplete() {
        oldMoreState = MoreState.Idle;
    }

    @Override
    public void setOldNoMoreLoading() {
        oldMoreState = MoreState.Nope;
    }

    @Override
    public int indexByMessageId(long messageId) {
        int count = getItemCount();
        for (int idx = 0; idx < count; idx++) {
            if (getItem(idx).messageId == messageId)
                return idx;
        }
        return -1;
    }

    @Override
    public int indexOfDummyLinkId(long linkId) {
        int count = getItemCount();
        for (int idx = count - 1; idx >= 0; idx--) {
            ResMessages.Link item = getItem(idx);
            if (item instanceof DummyMessageLink) {
                if (item.id == linkId) { return idx; }
            } else {
                return -1;
            }
        }
        return -1;
    }

    @Override
    public int getLastIndexByMessageId(long messageId) {
        int lastIndex = -1;
        int count = getItemCount();
        for (int idx = 0; idx < count; idx++) {
            if (getItem(idx).messageId == messageId) {
                lastIndex = idx;
            }
        }
        return lastIndex;
    }

    @Override
    public int indexOfLinkId(long linkId) {
        int size = getItemCount();
        for (int idx = size - 1; idx >= 0; --idx) {
            if (getItem(idx).id == linkId) {
                return idx;
            }
        }
        return -1;
    }

    @Override
    public List<Integer> indexByFeedbackId(long messageId) {

        List<Integer> indexList = new ArrayList<Integer>();

        int count = getItemCount();
        for (int idx = 0; idx < count; idx++) {
            int itemViewType = getItemViewType(idx);
            if (TypeUtil.hasTypeElement(itemViewType, TypeUtil.TYPE_VIEW_MESSAGE_COMMENT_FOR_FILE)
                    || TypeUtil.hasTypeElement(itemViewType, TypeUtil.TYPE_VIEW_STICKER_COMMENT_FOR_FILE)) {
                ResMessages.Link item = getItem(idx);
                if (item.message.feedbackId == messageId) {
                    indexList.add(idx);
                }
            }
        }


        return indexList;
    }

    @Override
    public List<Integer> getIndexListByPollId(long pollId) {
        List<Integer> indexList = new ArrayList<Integer>();

        int count = getItemCount();
        for (int idx = 0; idx < count; idx++) {
            Poll poll = getItem(idx).poll;
            if (poll != null && poll.getId() == pollId) {
                indexList.add(idx);
            }
        }

        return indexList;
    }

    @Override
    public int getDummyMessagePositionByLocalId(long localId) {
        if (localId <= 0) {
            return -1;
        }

        int size = getItemCount();

        for (int idx = size - 1; idx >= 0; --idx) {
            ResMessages.Link link = getItem(idx);

            if (link instanceof DummyMessageLink) {
                DummyMessageLink dummyMessageLink = (DummyMessageLink) link;
                if (dummyMessageLink.getLocalId() == localId) {
                    return idx;
                }
            } else {
                return -1;
            }
        }
        return -1;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        lock.lock();
        try {
            return links.size();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int getCount() {
        return getItemCount();
    }

    @Override
    public int getDummyMessageCount() {

        int total = 0;
        for (int idx = getItemCount() - 1; idx >= 0; idx--) {
            if (getItem(idx) instanceof DummyMessageLink) {
                ++total;
            } else {
                break;
            }
        }

        return total;
    }

    @Override
    public void removeAllDummy() {
        for (int idx = getItemCount() - 1; idx >= 0; idx--) {
            ResMessages.Link item = getItem(idx);
            if (item instanceof DummyMessageLink) {
                remove(idx);
            } else {
                break;
            }
        }
    }

    @Override
    public void add(ResMessages.Link dummyMessage) {
        lock.lock();
        try {
            links.add(dummyMessage);
            int itemCount = getItemCount();
            if (itemCount > 1) {
                itemTypes.remove(getItem(itemCount - 2));
                getItemViewType(itemCount - 2);
            }
            getItemViewType(itemCount - 1);
        } finally {
            lock.unlock();
        }

    }

    @Override
    public void add(int position, ResMessages.Link dummyMessage) {
        lock.lock();
        try {
            links.add(position, dummyMessage);
            int itemCount = getItemCount();
            if (position > 1) {
                itemTypes.remove(getItem(position - 1));
                getItemViewType(itemCount - 1);
            }
            getItemViewType(position);

            if (position < itemCount - 1) {
                itemTypes.remove(getItem(position + 1));
                getItemViewType(position + 1);
            }
        } finally {
            lock.unlock();
        }

    }

    @Override
    public void modifyStarredStateByPosition(int position, boolean isStarred) {
        getItem(position).message.isStarred = isStarred;
    }

    @Override
    public Date getItemDate(int position) {
        if (position >= getItemCount()) {
            return null;
        }

        ResMessages.Link item = getItem(position);
        return item != null ? item.time : null;
    }

    public void setMessagePointer(MessagePointer messagPointer) {
        this.messagePointer = messagPointer;
    }

    @Override
    public void refresh() {
        notifyDataSetChanged();
    }

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

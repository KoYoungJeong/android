package com.tosslab.jandi.app.ui.message.v2.adapter;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.messages.RefreshNewMessageEvent;
import com.tosslab.jandi.app.events.messages.RefreshOldMessageEvent;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.domain.SendMessage;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.local.orm.repositories.SendMessageRepository;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.ui.message.to.DummyMessageLink;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.BodyViewFactory;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.BodyViewHolder;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.Divider;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.RecyclerBodyViewHolder;
import com.tosslab.jandi.app.ui.message.v2.domain.MessagePointer;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.views.listeners.SimpleEndAnimatorListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.greenrobot.event.EventBus;
import rx.Observable;

public class MainMessageListAdapter extends RecyclerView.Adapter<RecyclerBodyViewHolder>
        implements MessageListHeaderAdapter.MessageItemDate {

    Context context;
    long lastMarker = -1;
    AnimState markerAnimState = AnimState.Idle;
    boolean moreFromNew;
    MoreState oldMoreState;
    MoreState newMoreState;
    MainMessageListAdapter.OnItemClickListener onItemClickListener;
    MainMessageListAdapter.OnItemLongClickListener onItemLongClickListener;
    long teamId;
    long roomId = -1;
    long entityId;
    List<ResMessages.Link> links;
    private ExecutorService threadPool = Executors.newSingleThreadExecutor();

    private MessagePointer messagePointer;

    public MainMessageListAdapter(Context context) {
        this.context = context;
        oldMoreState = MoreState.Idle;
        links = new CopyOnWriteArrayList<>();
        setHasStableIds(true);
    }

    public void saveCacheAndNotifyDataSetChanged(NotifyDataSetChangedCallback callback) {
        Runnable saveCacheRunnable = () -> {
            if (roomId == -1 || messagePointer.getFirstCursorLinkId() == -1) {
                links.clear();
                return;
            }
            addBeforeLinks(roomId, messagePointer.getFirstCursorLinkId(), links);
            removeDummyLink(links);
            addAfterLinks(roomId, links);
            addDummyLink(roomId, links);

            Activity activity = (Activity) context;
            activity.runOnUiThread(() -> {
                MainMessageListAdapter.this.notifyDataSetChanged();
                if (callback != null) {
                    callback.callBack();
                }
            });
        };

        threadPool.execute(saveCacheRunnable);
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
        BodyViewHolder bodyViewHolder = viewHolder.getViewHolder();
        bodyViewHolder.bindData(item, teamId, roomId, entityId);

        if (bodyViewHolder instanceof Divider) {
            ((Divider) bodyViewHolder).setUpDividerVisible();
        }

        if (item.id == lastMarker) {
            if (markerAnimState == MainMessageListAdapter.AnimState.Idle) {
                final View view = viewHolder.itemView;
                Integer colorFrom = context.getResources().getColor(R.color.jandi_transparent_white_1f);
                Integer colorTo = context.getResources().getColor(R.color.jandi_accent_color_1f);
                final ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
                colorAnimation.setDuration(context.getResources().getInteger(R.integer.highlight_animation_time));
                colorAnimation.setRepeatMode(ValueAnimator.REVERSE);
                colorAnimation.setRepeatCount(1);
                colorAnimation.addUpdateListener(animator -> view.setBackgroundColor((Integer) animator.getAnimatedValue()));

                colorAnimation.addListener(new SimpleEndAnimatorListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        markerAnimState = MainMessageListAdapter.AnimState.End;
                    }
                });
                colorAnimation.start();
                markerAnimState = MainMessageListAdapter.AnimState.Loading;
            }
        }

        if (position > 0 && position < getItemCount() - 1 - getDummyMessageCount()) {
            bodyViewHolder.setLastReadViewVisible(item.id, messagePointer.getLastReadLinkId());
        } else {
            bodyViewHolder.setLastReadViewVisible(0, -1);
        }

        if (position == 50 || position == getItemCount() / 10 && oldMoreState == MainMessageListAdapter.MoreState.Idle) {
            oldMoreState = MainMessageListAdapter.MoreState.Loading;
            if (oldMoreState != MainMessageListAdapter.MoreState.Idle) {
                EventBus.getDefault().post(new RefreshOldMessageEvent());
            }
        } else if (moreFromNew && position == getItemCount() - 1
                && newMoreState == MainMessageListAdapter.MoreState.Idle) {
            newMoreState = MainMessageListAdapter.MoreState.Loading;
            if (oldMoreState != MainMessageListAdapter.MoreState.Idle) {
                EventBus.getDefault().post(new RefreshNewMessageEvent());
            }
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

        return BodyViewFactory.getContentType(previousLink, currentLink, nextLink).ordinal();
    }

    private long getToCursorLinkId(List<ResMessages.Link> links) {
        long toCursorLinkId;
        if (!links.isEmpty()) {
            toCursorLinkId = links.get(0).id;
        } else {
            toCursorLinkId = Integer.MAX_VALUE;
        }
        return toCursorLinkId;
    }

    private void addAfterLinks(long roomId, List<ResMessages.Link> links) {
        long afterLinkStartId = getAfterLinkStartId(links);
        long afterLinkEndId = Integer.MAX_VALUE;
        int afterMessageCount = MessageRepository.getRepository().getMessagesCount(roomId, afterLinkStartId, afterLinkEndId);
        if (afterMessageCount > 0) {
            List<ResMessages.Link> afterLinks = MessageRepository.getRepository().getMessages(roomId, afterLinkStartId, afterLinkEndId);
            links.addAll(afterLinks);
        }
    }

    private long getAfterLinkStartId(List<ResMessages.Link> links) {
        long afterLinkStartId;
        if (!links.isEmpty()) {
            afterLinkStartId = links.get(links.size() - 1).id + 1;
        } else {
            afterLinkStartId = -1;
        }
        return afterLinkStartId;
    }

    private void addDummyLink(long roomId, List<ResMessages.Link> links) {
        Observable.from(SendMessageRepository.getRepository().getSendMessage(roomId))
                .map(sendMessage -> getDummyMessageLink(EntityManager.getInstance().getMe().getId(), sendMessage))
                .collect(() -> links, List::add)
                .onErrorResumeNext(throwable -> {
                    return Observable.empty();
                })
                .subscribe();
    }

    private void removeDummyLink(List<ResMessages.Link> links) {
        for (int idx = links.size() - 1; idx >= 0; idx--) {
            if (links.get(idx) instanceof DummyMessageLink) {
                links.remove(idx);
            } else {
                break;
            }
        }
    }

    private void addBeforeLinks(long roomId, long firstCursorLinkId, List<ResMessages.Link> links) {
        long toCursorLinkId = getToCursorLinkId(links);
        MessageRepository messageRepository = MessageRepository.getRepository();
        int beforeMessageCount = messageRepository.getMessagesCount(roomId, firstCursorLinkId, toCursorLinkId);

        if (firstCursorLinkId < toCursorLinkId && beforeMessageCount > 0) {
            List<ResMessages.Link> messages = messageRepository.getMessages(roomId, firstCursorLinkId, toCursorLinkId);
            links.addAll(0, messages);
        }
    }

    private DummyMessageLink getDummyMessageLink(long id, SendMessage link) {
        List<MentionObject> mentionObjects = new ArrayList<>();

        Collection<MentionObject> savedMention = link.getMentionObjects();
        if (savedMention != null) {
            for (MentionObject mentionObject : savedMention) {
                mentionObjects.add(mentionObject);
            }
        }

        DummyMessageLink dummyMessageLink;
        if (link.getStickerGroupId() > 0 && !TextUtils.isEmpty(link.getStickerId())) {

            dummyMessageLink = new DummyMessageLink(link.getId(), link.getStatus(),
                    link.getStickerGroupId(), link.getStickerId());
            dummyMessageLink.message.writerId = id;
            dummyMessageLink.message.createTime = new Date();
        } else {
            dummyMessageLink = new DummyMessageLink(link.getId(), link.getMessage(),
                    link.getStatus(), mentionObjects);
            dummyMessageLink.message.writerId = id;
            dummyMessageLink.message.createTime = new Date();
        }
        return dummyMessageLink;
    }

    public void addAll(int position, List<ResMessages.Link> links) {

        if (links == null || links.isEmpty()) {
            return;
        }

        Observable.from(links)
                .filter(link -> TextUtils.equals(link.status, "archived"))
                .subscribe(link -> {
                    int searchedPosition = indexByMessageId(link.messageId);
                    if (searchedPosition < 0) {
                        return;
                    }

                    if (TextUtils.equals(link.message.contentType, "file")) {
                        ResMessages.Link originLink = MainMessageListAdapter.this.links.get(searchedPosition);
                        originLink.message = link.message;
                        originLink.status = "archived";
                    } else {
                        MainMessageListAdapter.this.links.remove(searchedPosition);
                    }
                });
    }

    public void remove(int position) {
    }

    public void clear() {

    }

    public ResMessages.Link getItem(int position) {
        return links.get(position);
    }

    public void setOldLoadingComplete() {
        oldMoreState = MoreState.Idle;
    }

    public void setOldNoMoreLoading() {
        oldMoreState = MoreState.Nope;
    }

    public int indexByMessageId(long messageId) {
        int count = getItemCount();
        for (int idx = 0; idx < count; idx++) {
            if (getItem(idx).messageId == messageId)
                return idx;
        }
        return -1;
    }

    public int indexOfLinkId(long linkId) {
        int size = getItemCount();
        for (int idx = size - 1; idx >= 0; --idx) {
            if (getItem(idx).id == linkId) {
                return idx;
            }
        }
        return -1;
    }

    public List<Integer> indexByFeedbackId(long messageId) {

        List<Integer> indexList = new ArrayList<Integer>();

        int count = getItemCount();
        for (int idx = 0; idx < count; idx++) {
            int itemViewType = getItemViewType(idx);
            if (itemViewType == BodyViewHolder.Type.FileComment.ordinal()
                    || itemViewType == BodyViewHolder.Type.PureComment.ordinal()
                    || itemViewType == BodyViewHolder.Type.FileCommentWioutDivider.ordinal()
                    || itemViewType == BodyViewHolder.Type.PureCommentWioutDivider.ordinal()
                    || itemViewType == BodyViewHolder.Type.FileStickerCommentWioutDivider.ordinal()
                    || itemViewType == BodyViewHolder.Type.PureStickerCommentWioutDivider.ordinal()) {
                ResMessages.Link item = getItem(idx);
                if (item.message.feedbackId == messageId) {
                    indexList.add(idx);
                }
            }
        }

        return indexList;
    }

    public int getDummeMessagePositionByLocalId(long localId) {
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

    public void setMarker(long lastMarker) {
        this.lastMarker = lastMarker;
    }

    public void setMoreFromNew(boolean moreFromNew) {
        LogUtil.w("tony", "setMoreFromNew");
        this.moreFromNew = moreFromNew;
    }

    public void setNewLoadingComplete() {
        LogUtil.w("tony", "setNewLoadingComplete");
        newMoreState = MoreState.Idle;
    }

    public void setNewNoMoreLoading() {
        LogUtil.w("tony", "setNewNoMoreLoading");
        newMoreState = MoreState.Nope;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    public void setTeamId(long teamId) {
        this.teamId = teamId;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return links.size();
    }

    public int getDummyMessageCount() {

        int total = 0;
        for (int idx = getItemCount() - 1; idx >= 0; idx--) {
            if (links.get(idx) instanceof DummyMessageLink) {
                ++total;
            } else {
                break;
            }
        }

        return total;
    }

    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    public void setEntityId(long entityId) {
        this.entityId = entityId;
    }

    public void modifyStarredStateByPosition(int position, boolean isStarred) {
        links.get(position).message.isStarred = isStarred;
        notifyItemChanged(position);
    }

    @Override
    public Date getItemDate(int position) {
        return getItem(position).time;
    }

    public void setMessagePointer(MessagePointer messagPointer) {
        this.messagePointer = messagPointer;
    }

    enum MoreState {
        Idle, Loading, Nope
    }

    enum AnimState {
        Idle, Loading, End
    }

    public interface NotifyDataSetChangedCallback {
        void callBack();
    }

    public interface OnItemClickListener {
        void onItemClick(RecyclerView.Adapter adapter, int position);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(RecyclerView.Adapter adapter, int position);
    }

}

package com.tosslab.jandi.app.ui.message.v2.adapter;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
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
import com.tosslab.jandi.app.views.listeners.SimpleEndAnimatorListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class MessageCursorListAdapter extends MessageAdapter {
    private final PublishSubject<Object> objectPublishSubject;
    private Context context;

    private int lastMarker = -1;
    private AnimState markerAnimState = AnimState.Idle;
    private boolean moreFromNew;
    private MoreState oldMoreState;
    private MoreState newMoreState;

    private MessageListAdapter.OnItemClickListener onItemClickListener;
    private MessageListAdapter.OnItemLongClickListener onItemLongClickListener;

    private int teamId;
    private int roomId = -1;
    private int entityId;
    private int lastReadLinkId = -1;

    private int firstCursorLinkId = -1;

    private List<ResMessages.Link> links;

    public MessageCursorListAdapter(Context context) {
        this.context = context;
        oldMoreState = MoreState.Idle;
        links = new CopyOnWriteArrayList<>();
        setHasStableIds(true);
        registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {

                if (roomId == -1 || firstCursorLinkId == -1) {
                    links.clear();
                    return;
                }


                objectPublishSubject.onNext(1);
            }
        });

        objectPublishSubject = PublishSubject.create();
        objectPublishSubject
                .doOnNext(o -> {
                    addBeforeLinks(roomId, firstCursorLinkId, links);
                    removeDummyLink(links);
                    addAfterLinks(roomId, links);
                    addDummyLink(roomId, links);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o1 -> {
                    notifyItemRangeChanged(0, links.size());
                });
    }

    private int getToCursorLinkId(List<ResMessages.Link> links) {
        int toCursorLinkId;
        if (!links.isEmpty()) {
            toCursorLinkId = links.get(0).id;
        } else {
            toCursorLinkId = Integer.MAX_VALUE;
        }
        return toCursorLinkId;
    }

    private void addAfterLinks(int roomId, List<ResMessages.Link> links) {
        int afterLinkStartId = getAfterLinkStartId(links);
        int afterLinkEndId = Integer.MAX_VALUE;
        int afterMessageCount = MessageRepository.getRepository().getMessagesCount(roomId, afterLinkStartId, afterLinkEndId);
        if (afterMessageCount > 0) {
            List<ResMessages.Link> afterLinks = MessageRepository.getRepository().getMessages(roomId, afterLinkStartId, afterLinkEndId);
            links.addAll(afterLinks);
        }
    }

    private int getAfterLinkStartId(List<ResMessages.Link> links) {
        int afterLinkStartId;
        if (!links.isEmpty()) {
            afterLinkStartId = links.get(links.size() - 1).id + 1;
        } else {
            afterLinkStartId = -1;
        }
        return afterLinkStartId;
    }

    private void addDummyLink(int roomId, List<ResMessages.Link> links) {
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

    private void addBeforeLinks(int roomId, int firstCursorLinkId, List<ResMessages.Link> links) {
        int toCursorLinkId = getToCursorLinkId(links);
        MessageRepository messageRepository = MessageRepository.getRepository();
        int beforeMessageCount = messageRepository.getMessagesCount(roomId, firstCursorLinkId, toCursorLinkId);

        if (firstCursorLinkId < toCursorLinkId && beforeMessageCount > 0) {
            List<ResMessages.Link> messages = messageRepository.getMessages(roomId, firstCursorLinkId, toCursorLinkId);
            links.addAll(0, messages);
        }
    }

    @Override
    public int getLastReadLinkId() {
        return lastReadLinkId;
    }

    @Override
    public void setLastReadLinkId(int lastReadLinkId) {
        this.lastReadLinkId = lastReadLinkId;
    }

    @Override
    public void setTeamId(int teamId) {
        this.teamId = teamId;
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
            if (markerAnimState == AnimState.Idle) {
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
                        markerAnimState = AnimState.End;
                    }
                });
                colorAnimation.start();
                markerAnimState = AnimState.Loading;
            }
        }

        if (position > 0 && position < getItemCount() - 1 - getDummyMessageCount()) {
            bodyViewHolder.setLastReadViewVisible(item.id, lastReadLinkId);
        } else {
            bodyViewHolder.setLastReadViewVisible(0, -1);
        }

        if (position <= getItemCount() / 10 && oldMoreState == MoreState.Idle) {
            oldMoreState = MoreState.Loading;
            EventBus.getDefault().post(new RefreshOldMessageEvent());
        } else if (moreFromNew && position == getItemCount() - 1 && newMoreState == MoreState.Idle) {
            newMoreState = MoreState.Loading;
            EventBus.getDefault().post(new RefreshNewMessageEvent());
        }

        bodyViewHolder.setOnItemClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(MessageCursorListAdapter.this, position);
            }
        });

        bodyViewHolder.setOnItemLongClickListener(v -> {
            if (onItemLongClickListener != null) {
                return onItemLongClickListener.onItemLongClick(MessageCursorListAdapter.this, position);
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

    @Override
    public ResMessages.Link getItem(int position) {
        return links.get(position);
    }

    private DummyMessageLink getDummyMessageLink(int id, SendMessage link) {
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

    @Override
    public void addAll(int position, List<ResMessages.Link> links) {

        if (position == 0 && links != null && !links.isEmpty()) {
            firstCursorLinkId = links.get(0).id;
        } else {
            Observable.from(links)
                    .filter(link -> TextUtils.equals(link.status, "archived"))
                    .subscribe(link -> {
                        int searchedPosition = indexByMessageId(link.messageId);
                        if (searchedPosition < 0) {
                            return;
                        }

                        if (TextUtils.equals(link.message.contentType, "file")) {
                            ResMessages.Link originLink = MessageCursorListAdapter.this.links.get(searchedPosition);
                            originLink.message = link.message;
                            originLink.status = "archived";
                        } else {
                            MessageCursorListAdapter.this.links.remove(searchedPosition);
                        }
                    });
        }

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return links.size();
    }

    @Override
    public int getDummyMessageCount() {

        int total = 0;
        for (int idx = links.size() - 1; idx >= 0; idx--) {
            if (links.get(idx) instanceof DummyMessageLink) {
                ++total;
            } else {
                break;
            }
        }

        return total;
    }

    @Override
    public int indexByMessageId(int messageId) {
        int count = getItemCount();
        for (int idx = 0; idx < count; idx++) {
            if (getItem(idx).messageId == messageId)
                return idx;
        }
        return -1;
    }

    @Override
    public void setOldNoMoreLoading() {
        oldMoreState = MoreState.Nope;
    }

    public void setOldLoadingComplete() {
        oldMoreState = MoreState.Idle;
    }

    @Override
    public void addDummyMessage(DummyMessageLink dummyMessageLink) {
    }

    @Override
    public void updateMessageId(long localId, int id) {
    }

    @Override
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

    @Override
    public void updateDummyMessageState(long localId, SendMessage.Status state) {
    }

    @Override
    public void remove(int position) {
    }

    @Override
    public List<Integer> indexByFeedbackId(int messageId) {

        List<Integer> indexList = new ArrayList<Integer>();

        int count = getItemCount();
        for (int idx = 0; idx < count; idx++) {
            int itemViewType = getItemViewType(idx);
            if (itemViewType == BodyViewHolder.Type.FileComment.ordinal() || itemViewType == BodyViewHolder.Type.PureComment.ordinal()) {
                ResMessages.Link item = getItem(idx);
                if (item.message.feedbackId == messageId) {
                    indexList.add(idx);
                }
            }
        }

        return indexList;
    }

    @Override
    public void setMarker(int lastMarker) {
        this.lastMarker = lastMarker;
    }

    @Override
    public void setMoreFromNew(boolean moreFromNew) {
        this.moreFromNew = moreFromNew;
    }

    @Override
    public void setNewLoadingComplete() {
        newMoreState = MoreState.Idle;
    }

    @Override
    public void setNewNoMoreLoading() {
        newMoreState = MoreState.Nope;
    }

    @Override
    public void setOnItemClickListener(MessageListAdapter.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public void setOnItemLongClickListener(MessageListAdapter.OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    @Override
    public int getRoomId() {
        return roomId;
    }

    @Override
    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    @Override
    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    @Override
    public int indexOfLinkId(int linkId) {
        int size = getItemCount();
        for (int idx = size - 1; idx >= 0; --idx) {
            if (getItem(idx).id == linkId) {
                return idx;
            }
        }
        return -1;
    }

    @Override
    public void clear() {

    }

    @Override
    public void modifyStarredStateByPosition(int position, boolean isStarred) {
        links.get(position).message.isStarred = isStarred;
        notifyItemChanged(position);
    }


    private enum MoreState {
        Idle, Loading, Nope
    }

    private enum AnimState {
        Idle, Loading, End
    }


}

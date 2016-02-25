package com.tosslab.jandi.app.ui.message.v2.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;

import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.domain.SendMessage;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.local.orm.repositories.SendMessageRepository;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.ui.message.to.DummyMessageLink;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import rx.Observable;

public class MainMessageListAdapter extends MessageAdapter {

    private long firstCursorLinkId = -1;

    public MainMessageListAdapter(Context context) {
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

                addBeforeLinks(roomId, firstCursorLinkId, links);
                removeDummyLink(links);
                addAfterLinks(roomId, links);
                addDummyLink(roomId, links);
            }
        });
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

    @Override
    public void addAll(int position, List<ResMessages.Link> links) {

        if (position == 0 && links != null && !links.isEmpty()) {
            long firstItemLinkId = links.get(0).id;
            if (firstCursorLinkId > 0) {
                firstCursorLinkId = Math.min(firstItemLinkId, firstCursorLinkId);
            } else {
                firstCursorLinkId = firstItemLinkId;
            }
        } else {
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
    }

    public void setFirstCursorLinkId(long firstCursorLinkId) {
        this.firstCursorLinkId = firstCursorLinkId;
    }

    @Override
    public void remove(int position) {
    }

    @Override
    public void clear() {

    }

}

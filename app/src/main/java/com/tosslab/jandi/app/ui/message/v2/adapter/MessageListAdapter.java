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

public class MessageListAdapter extends MessageAdapter {

    public MessageListAdapter(Context context) {
        this.context = context;
        this.links = new ArrayList<>();
        oldMoreState = MoreState.Idle;
        setHasStableIds(true);
    }

    @Override
    public void addAll(int position, List<ResMessages.Link> messages) {
        // delete dummy message by same messageId
        for (int idx = messages.size() - 1; idx >= 0; --idx) {
            int dummyMessagePosition = getDummyMessagePositionByMessageId(messages.get(idx).messageId);
            if (dummyMessagePosition >= 0) {
                links.remove(dummyMessagePosition);
            } else {
                break;
            }
        }

        for (int idx = links.size() - 1; idx >= 0; idx--) {
            ResMessages.Link link = links.get(idx);
            if (link instanceof DummyMessageLink) {
                DummyMessageLink dummyLink = (DummyMessageLink) link;
                if (TextUtils.equals(dummyLink.getStatus(), SendMessage.Status.COMPLETE.name())) {
                    links.remove(idx);
                }
            } else {
                break;
            }
        }

        int size = messages.size();
        ResMessages.Link link;

        for (int idx = size - 1; idx >= 0; --idx) {
            link = messages.get(idx);

            if (TextUtils.equals(link.status, "created") || TextUtils.equals(link.status, "shared") || TextUtils.equals(link.status, "event")) {
            } else if (TextUtils.equals(link.status, "edited")) {
                int searchedPosition = indexByMessageId(link.messageId);
                if (searchedPosition >= 0) {
                    links.set(searchedPosition, link);
                }
                messages.remove(link);
            } else if (TextUtils.equals(link.status, "archived")) {
                int searchedPosition = indexByMessageId(link.messageId);
                // if file type
                if (TextUtils.equals(link.message.contentType, "file")) {
                    if (searchedPosition >= 0) {
                        ResMessages.Link originLink = links.get(searchedPosition);
                        originLink.message = link.message;
                        originLink.status = "archived";
                        messages.remove(link);
                    }
                    // if cannot find same object, will be add to list.
                } else {
                    if (searchedPosition >= 0) {
                        links.remove(searchedPosition);
                    }
                    messages.remove(link);
                }
            } else {
                messages.remove(link);
            }
        }

        links.addAll(Math.min(position, links.size() - getDummyMessageCount()), messages);
    }

    @Override
    public void clear() {
        links.clear();
    }

    private int getDummyMessagePositionByMessageId(long messageId) {

        int size = getItemCount();

        for (int idx = size - 1; idx >= 0; --idx) {
            ResMessages.Link link = getItem(idx);

            if (link instanceof DummyMessageLink) {
                DummyMessageLink dummyMessageLink = (DummyMessageLink) link;
                if (dummyMessageLink.getMessageId() == messageId) {
                    return idx;
                }
            } else {
                return -1;
            }
        }
        return -1;

    }

    @Override
    public void remove(int position) {
        links.remove(position);
    }

    public boolean isEndOfLoad() {
        return newMoreState == MoreState.Nope;
    }
}

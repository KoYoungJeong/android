package com.tosslab.jandi.app.ui.poll.detail.adapter;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.Pair;
import android.view.ViewGroup;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.files.FileCommentClickEvent;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.poll.Poll;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.ui.base.adapter.MultiItemRecyclerAdapter;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.ui.filedetail.adapter.FileDetailAdapter;
import com.tosslab.jandi.app.ui.filedetail.adapter.viewholder.comment.CommentViewHolder;
import com.tosslab.jandi.app.ui.filedetail.adapter.viewholder.comment.StickerViewHolder;
import com.tosslab.jandi.app.ui.poll.detail.adapter.model.PollDetailDataModel;
import com.tosslab.jandi.app.ui.poll.detail.adapter.view.PollDetailDataView;
import com.tosslab.jandi.app.ui.poll.detail.adapter.viewholder.DividerViewHolder;
import com.tosslab.jandi.app.ui.poll.detail.adapter.viewholder.PollDetailRow;
import com.tosslab.jandi.app.ui.poll.detail.adapter.viewholder.PollInfoViewHolder;
import com.tosslab.jandi.app.ui.poll.detail.adapter.viewholder.PollItemViewHolder;
import com.tosslab.jandi.app.ui.poll.detail.adapter.viewholder.PollSharedInViewHolder;
import com.tosslab.jandi.app.ui.poll.detail.adapter.viewholder.PollVoteViewHolder;
import com.tosslab.jandi.app.ui.poll.detail.adapter.viewholder.ProfileViewHolder;
import com.tosslab.jandi.app.utils.UiUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.greenrobot.event.EventBus;
import rx.Observable;

/**
 * Created by tonyjs on 16. 6. 14..
 */
public class PollDetailAdapter extends MultiItemRecyclerAdapter
        implements PollDetailDataModel, PollDetailDataView {

    public static final int VIEW_TYPE_PROFILE = 0;
    public static final int VIEW_TYPE_POLL_INFO = 1;
    public static final int VIEW_TYPE_POLL_ITEM = 2;
    public static final int VIEW_TYPE_POLL_ITEM_VOTE = 3;
    public static final int VIEW_TYPE_POLL_SHARED_IN = 4;
    public static final int VIEW_TYPE_COMMENT = 5;
    public static final int VIEW_TYPE_STICKER = 6;
    public static final int VIEW_TYPE_DIVIDER = 7;
    public static final int VIEW_TYPE_POLL_DELETED = 8;

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_PROFILE:
                return ProfileViewHolder.newInstance(parent);
            case VIEW_TYPE_POLL_INFO:
                return PollInfoViewHolder.newInstance(parent);
            case VIEW_TYPE_POLL_ITEM:
                return PollItemViewHolder.newInstance(parent);
            case VIEW_TYPE_POLL_ITEM_VOTE:
                return PollVoteViewHolder.newInstance(parent);
            case VIEW_TYPE_POLL_SHARED_IN:
                return PollSharedInViewHolder.newInstance(parent);
            case VIEW_TYPE_DIVIDER:
                return DividerViewHolder.newInstance(parent);
            case VIEW_TYPE_COMMENT:
                return CommentViewHolder.newInstance(parent);
            case VIEW_TYPE_STICKER:
                return StickerViewHolder.newInstance(parent);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        int itemViewType = getItemViewType(position);
        if (itemViewType == VIEW_TYPE_COMMENT || itemViewType == VIEW_TYPE_STICKER) {
            final ResMessages.OriginalMessage item = getItem(position);
            holder.itemView.setOnClickListener(v -> {
                EventBus.getDefault().post(new FileCommentClickEvent(item));
            });

            holder.itemView.setOnLongClickListener(v -> {
                EventBus.getDefault().post(new FileCommentClickEvent(item, true /* isLongClick */));
                return true;
            });
        }
    }

    @Override
    public void setPollDetails(Poll poll) {
        List<Row<?>> rows = getPollDetailRows(poll);

        setRows(rows);
    }

    private List<Row<?>> getPollDetailRows(Poll poll) {
        List<Row<?>> rows = new ArrayList<>();
        if (poll == null) {
            return rows;
        }

        rows.add(PollDetailRow.create(poll, VIEW_TYPE_PROFILE));
        rows.add(PollDetailRow.create(poll, VIEW_TYPE_POLL_INFO));

        List<Poll.Item> items = new ArrayList<>(poll.getItems());

        for (int i = 0; i < items.size(); i++) {
            rows.add(PollDetailRow.create(Pair.create(poll, items.get(i)), VIEW_TYPE_POLL_ITEM));
            if (i < items.size() - 1) {
                DividerViewHolder.Info dividerInfo =
                        DividerViewHolder.Info.create((int) UiUtils.getPixelFromDp(5), Color.TRANSPARENT);
                rows.add(PollDetailRow.create(dividerInfo, VIEW_TYPE_DIVIDER));
            }
        }

        if ("enabled".equals(poll.getVoteStatus())) {
            DividerViewHolder.Info dividerInfo =
                    DividerViewHolder.Info.create((int) UiUtils.getPixelFromDp(15), Color.TRANSPARENT);
            rows.add(PollDetailRow.create(dividerInfo, VIEW_TYPE_DIVIDER));
            rows.add(PollDetailRow.create(poll, VIEW_TYPE_POLL_ITEM_VOTE));
        }

        DividerViewHolder.Info dividerInfo =
                DividerViewHolder.Info.create((int) UiUtils.getPixelFromDp(20), Color.TRANSPARENT);
        rows.add(PollDetailRow.create(dividerInfo, VIEW_TYPE_DIVIDER));

        TopicRoom room = TeamInfoLoader.getInstance().getTopic(poll.getTopicId());
        rows.add(PollDetailRow.create(room, VIEW_TYPE_POLL_SHARED_IN));
        return rows;
    }

    @Override
    public void replacePollDetails(Poll poll) {
        List<Row<?>> rows = getPollDetailRows(poll);
        addRows(0, rows);
    }

    @Override
    public void removePollDetailRow() {
        List<Row<?>> rows = getRows();
        if (rows == null || rows.size() <= 0) {
            return;
        }

        for (int i = rows.size() - 1; i >= 0; i--) {
            if (rows.get(i) instanceof PollDetailRow) {
                rows.remove(i);
            }
        }
    }

    @Override
    public void addPollComments(List<ResMessages.OriginalMessage> comments) {
        if (comments == null || comments.isEmpty()) {
            return;
        }
        List<Row<?>> rows = new ArrayList<>();

        rows.add(getPollCommentDividerRow());

        Observable.from(comments)
                .subscribe(commentMessage -> {
                    rows.add(getPollCommentRow(commentMessage));
                });

        addRows(rows);
    }

    @Override
    public void addPollComment(ResMessages.OriginalMessage comment) {
        if (!hasCommentRows()) {
            addRow(getPollCommentDividerRow());
        }

        addRow(getPollCommentRow(comment));
    }

    @Override
    public void addPollComment(int position, ResMessages.OriginalMessage comment) {
        addRow(position, getPollCommentRow(comment));
    }

    @Override
    public void removeAllRows() {
        clear();
    }

    @Override
    public void modifyCommentStarredState(long messageId, boolean starred) {
        for (Row<?> row : getRows()) {
            if (!(row.getItem() instanceof ResMessages.OriginalMessage)) {
                continue;
            }

            ResMessages.OriginalMessage message = (ResMessages.OriginalMessage) row.getItem();
            if (message.id == messageId) {
                message.isStarred = starred;
                break;
            }
        }
    }

    @Override
    public Pair<Integer, ResMessages.OriginalMessage> removeCommentByMessageIdAndGet(long messageId) {
        List<Row<?>> rows = getRows();
        for (int i = rows.size() - 1; i >= 0; i--) {
            Object item = rows.get(i).getItem();
            if (!(item instanceof ResMessages.OriginalMessage)) {
                continue;
            }

            ResMessages.OriginalMessage comment = (ResMessages.OriginalMessage) item;
            if (comment.id == messageId) {
                remove(i);
                return Pair.create(i, comment);
            }
        }

        return Pair.create(-1, new ResMessages.OriginalMessage());
    }

    @Override
    public void removePollComment(long messageId) {
        List<Row<?>> rows = getRows();
        for (int i = rows.size() - 1; i >= 0; i--) {
            Object item = rows.get(i).getItem();
            if (!(item instanceof ResMessages.OriginalMessage)) {
                continue;
            }

            ResMessages.OriginalMessage message = (ResMessages.OriginalMessage) item;
            if (message.id == messageId) {
                remove(i);
                break;
            }
        }
    }

    public Row getPollCommentDividerRow() {
        int dividerColor = JandiApplication.getContext()
                .getResources().getColor(R.color.jandi_file_search_item_divider);
        DividerViewHolder.Info dividerInfo =
                DividerViewHolder.Info.create((int) UiUtils.getPixelFromDp(1), dividerColor);
        return Row.create(dividerInfo, VIEW_TYPE_DIVIDER);
    }

    public Row getPollCommentRow(ResMessages.OriginalMessage commentMessage) {
        boolean isSticker = !(commentMessage instanceof ResMessages.CommentMessage);
        int viewType = isSticker ? VIEW_TYPE_STICKER : VIEW_TYPE_COMMENT;
        return Row.create(commentMessage, viewType);
    }

    @Override
    public boolean hasCommentRows() {
        for (Row<?> row : getRows()) {
            if (row.getItem() instanceof ResMessages.OriginalMessage) {
                return true;
            }
        }
        return false;
    }
}

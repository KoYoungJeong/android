package com.tosslab.jandi.app.ui.poll.detail.adapter;

import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.view.ViewGroup;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.poll.Poll;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.ui.base.adapter.MultiItemRecyclerAdapter;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.ui.comment.CellDividerUpdater;
import com.tosslab.jandi.app.ui.comment.OnCommentClickListener;
import com.tosslab.jandi.app.ui.comment.OnCommentLongClickListener;
import com.tosslab.jandi.app.ui.comment.StickerCommentViewHolder;
import com.tosslab.jandi.app.ui.comment.TextCommentViewHolder;
import com.tosslab.jandi.app.ui.poll.detail.adapter.model.PollDetailDataModel;
import com.tosslab.jandi.app.ui.poll.detail.adapter.view.PollDetailDataView;
import com.tosslab.jandi.app.ui.poll.detail.adapter.viewholder.PollDeletedInfoViewHolder;
import com.tosslab.jandi.app.ui.poll.detail.adapter.viewholder.PollDetailRow;
import com.tosslab.jandi.app.ui.poll.detail.adapter.viewholder.PollHeaderViewHolder;
import com.tosslab.jandi.app.ui.poll.detail.adapter.viewholder.PollInfoViewHolder;
import com.tosslab.jandi.app.ui.poll.detail.adapter.viewholder.PollItemViewHolder;
import com.tosslab.jandi.app.ui.poll.detail.adapter.viewholder.PollSharedInViewHolder;
import com.tosslab.jandi.app.ui.poll.detail.adapter.viewholder.PollVoteViewHolder;
import com.tosslab.jandi.app.utils.UiUtils;
import com.tosslab.jandi.app.views.decoration.DividerViewHolder;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

/**
 * Created by tonyjs on 16. 6. 14..
 */
public class PollDetailAdapter extends MultiItemRecyclerAdapter
        implements PollDetailDataModel, PollDetailDataView {

    private static final int VIEW_TYPE_POLL_HEADER = 0;
    private static final int VIEW_TYPE_POLL_INFO = 1;
    private static final int VIEW_TYPE_POLL_ITEM = 2;
    private static final int VIEW_TYPE_POLL_ITEM_VOTE = 3;
    private static final int VIEW_TYPE_POLL_SHARED_IN = 4;
    private static final int VIEW_TYPE_COMMENT = 5;
    private static final int VIEW_TYPE_STICKER = 6;
    private static final int VIEW_TYPE_DIVIDER = 7;
    private static final int VIEW_TYPE_POLL_DELETED = 8;
    private static final int VIEW_TYPE_COMMENT_NO_PROFILE = 9;
    private static final int VIEW_TYPE_STICKER_NO_PROFILE = 10;

    private OnCommentClickListener onCommentClickListener;
    private OnCommentLongClickListener onCommentLongClickListener;

    private PollInfoViewHolder.OnPollParticipantsClickListener onPollParticipantsClickListener;
    private PollItemViewHolder.OnPollItemParticipantsClickListener onPollItemParticipantsClickListener;
    private PollVoteViewHolder.OnPollVoteClickListener onPollVoteClickListener;
    private PollHeaderViewHolder.OnPollStarClickListener onPollStarClickListener;

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_POLL_HEADER:
                return PollHeaderViewHolder.newInstance(parent, onPollStarClickListener);

            case VIEW_TYPE_POLL_INFO:
                return PollInfoViewHolder.newInstance(parent, onPollParticipantsClickListener);

            case VIEW_TYPE_POLL_DELETED:
                return PollDeletedInfoViewHolder.newInstance(parent);

            case VIEW_TYPE_POLL_ITEM:
                return PollItemViewHolder.newInstance(parent, onPollItemParticipantsClickListener);

            case VIEW_TYPE_POLL_ITEM_VOTE:
                return PollVoteViewHolder.newInstance(parent, onPollVoteClickListener);

            case VIEW_TYPE_POLL_SHARED_IN:
                return PollSharedInViewHolder.newInstance(parent);

            case VIEW_TYPE_DIVIDER:
                return DividerViewHolder.newInstance(parent);
            case VIEW_TYPE_COMMENT:
                return TextCommentViewHolder.newInstance(parent, onCommentClickListener, onCommentLongClickListener);
            case VIEW_TYPE_STICKER:
                return StickerCommentViewHolder.newInstance(parent, onCommentClickListener, onCommentLongClickListener);
            case VIEW_TYPE_COMMENT_NO_PROFILE:
                return TextCommentViewHolder.newInstanceNoProfile(parent, onCommentClickListener, onCommentLongClickListener);
            case VIEW_TYPE_STICKER_NO_PROFILE:
                return StickerCommentViewHolder.newInstanceNoProfile(parent, onCommentClickListener, onCommentLongClickListener);
        }
        return DividerViewHolder.newInstance(parent);
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        if (holder instanceof CellDividerUpdater) {
            CellDividerUpdater updater = (CellDividerUpdater) holder;
            if (position >= getItemCount() - 1) {
                updater.cellUpdater(true);
            } else {
                ResMessages.OriginalMessage current = getItem(position);
                ResMessages.OriginalMessage after = getItem(position + 1);
                updater.cellUpdater(current.writerId != after.writerId);
            }
        }
    }

    @Override
    public void setPollDetails(Poll poll) {
        List<Row<?>> rows = getPollDetailRows(poll);

        setRows(rows);
    }

    private List<Row<?>> getPollDetailRows(Poll poll) {
        if (poll == null) {
            return new ArrayList<>();
        }

        if ("deleted".equals(poll.getStatus())) {
            return getDeletedPollDetailRows(poll);
        }

        List<Row<?>> rows = new ArrayList<>();

        rows.add(PollDetailRow.create(poll, VIEW_TYPE_POLL_HEADER));

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

    private List<Row<?>> getDeletedPollDetailRows(Poll poll) {
        List<Row<?>> rows = new ArrayList<>();

        rows.add(PollDetailRow.create(poll, VIEW_TYPE_POLL_HEADER));
        rows.add(PollDetailRow.create(poll, VIEW_TYPE_POLL_DELETED));

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

        if (!hasCommentRows()
                && getItemViewType(getItemCount() - 1) != VIEW_TYPE_DIVIDER) {
            addRow(getPollCommentDividerRow());
        }

        Observable.range(0, comments.size())
                .map(it -> {
                    if (it > 0) {
                        return getPollCommentRow(comments.get(it), comments.get(it - 1));
                    } else {
                        return getPollCommentRow(comments.get(it), null);
                    }
                })
                .collect(() -> rows, List::add)
                .subscribe();

        addRows(rows);
    }

    @Override
    public void addPollComment(ResMessages.OriginalMessage comment) {
        if (!hasCommentRows()
                && getItemViewType(getItemCount() - 1) != VIEW_TYPE_DIVIDER) {
            addRow(getPollCommentDividerRow());
        }

        int itemCount = getItemCount();
        if (itemCount > 0) {
            addRow(getPollCommentRow(comment, getItemIfCommentType(getItem(itemCount - 1))));
        } else {
            addRow(getPollCommentRow(comment, null));
        }
    }

    @Override
    public void addPollComment(int position, ResMessages.OriginalMessage comment) {
        int itemCount = getItemCount();
        if (itemCount > 0) {
            addRow(position, getPollCommentRow(comment, getItemIfCommentType(getItem(itemCount - 1))));
        } else {
            addRow(position, getPollCommentRow(comment, null));
        }
    }

    @Nullable
    private ResMessages.OriginalMessage getItemIfCommentType(Object item) {
        ResMessages.OriginalMessage message = null;
        if (item instanceof ResMessages.CommentMessage
                || item instanceof ResMessages.CommentStickerMessage) {
            message = (ResMessages.OriginalMessage) item;
        }
        return message;
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
                updateCommentMessageType(i);
                return Pair.create(i, comment);
            }
        }

        return Pair.create(-1, new ResMessages.OriginalMessage());
    }

    private void updateCommentMessageType(int position) {
        ResMessages.OriginalMessage before = null;
        if (position > 0) {
            Object obj = getItem(position - 1);
            if (obj instanceof ResMessages.CommentMessage
                    || obj instanceof ResMessages.CommentStickerMessage) {
                before = (ResMessages.OriginalMessage) obj;
            }
        }

        ResMessages.OriginalMessage after = null;
        if (position < getItemCount()) {
            Object obj = getItem(position);
            if (obj instanceof ResMessages.CommentMessage
                    || obj instanceof ResMessages.CommentStickerMessage) {
                after = (ResMessages.OriginalMessage) obj;
            }
        }
        if (after != null) {
            Row row = getPollCommentRow(after, before);
            setRow(position, row);
        }
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
                updateCommentMessageType(i);
                break;
            }
        }
    }

    @Override
    public Poll getPoll() {
        for (Row row : getRows()) {
            if (row instanceof PollDetailRow) {
                return (Poll) row.getItem();
            }
        }

        Poll poll = new Poll();
        poll.setId(-1);
        return poll;
    }

    @Override
    public ResMessages.Link getPollCommentById(long id) {
        for (Row row : getRows()) {
            if (row.getItem() instanceof ResMessages.Link) {
                return (ResMessages.Link) row.getItem();
            }
        }

        ResMessages.Link link = new ResMessages.Link();
        link.id = -1;
        return link;
    }

    public Row getPollCommentDividerRow() {
        int dividerColor = JandiApplication.getContext()
                .getResources().getColor(R.color.jandi_file_search_item_divider);
        DividerViewHolder.Info dividerInfo =
                DividerViewHolder.Info.create((int) UiUtils.getPixelFromDp(1), dividerColor);
        return Row.create(dividerInfo, VIEW_TYPE_DIVIDER);
    }

    public Row getPollCommentRow(ResMessages.OriginalMessage current, ResMessages.OriginalMessage before) {
        boolean profile = true;
        if (before != null) {
            profile = before.writerId != current.writerId;
        }

        boolean comment = current instanceof ResMessages.CommentMessage;
        int viewType;
        if (comment) {
            if (profile) {
                viewType = VIEW_TYPE_COMMENT;
            } else {
                viewType = VIEW_TYPE_COMMENT_NO_PROFILE;
            }
        } else {
            if (profile) {
                viewType = VIEW_TYPE_STICKER;
            } else {
                viewType = VIEW_TYPE_STICKER_NO_PROFILE;
            }
        }
        return Row.create(current, viewType);
    }

    @Override
    public boolean hasCommentRows() {
        for (int idx = getItemCount() - 1; idx >= 0; idx--) {
            Row<?> row = getRow(idx);
            if (row != null && row.getItem() instanceof ResMessages.OriginalMessage) {
                return true;
            }
        }
        return false;
    }

    public void setOnCommentClickListener(OnCommentClickListener onCommentClickListener) {
        this.onCommentClickListener = onCommentClickListener;
    }

    public void setOnCommentLongClickListener(OnCommentLongClickListener onCommentLongClickListener) {
        this.onCommentLongClickListener = onCommentLongClickListener;
    }

    public void setOnPollParticipantsClickListener(
            PollInfoViewHolder.OnPollParticipantsClickListener onPollParticipantsClickListener) {
        this.onPollParticipantsClickListener = onPollParticipantsClickListener;
    }

    public void setOnPollItemParticipantsClickListener(
            PollItemViewHolder.OnPollItemParticipantsClickListener onPollItemParticipantsClickListener) {
        this.onPollItemParticipantsClickListener = onPollItemParticipantsClickListener;
    }

    public void setOnPollVoteClickListener(
            PollVoteViewHolder.OnPollVoteClickListener onPollVoteClickListener) {
        this.onPollVoteClickListener = onPollVoteClickListener;
    }

    public void setOnPollStarClickListener(
            PollHeaderViewHolder.OnPollStarClickListener onPollStarClickListener) {
        this.onPollStarClickListener = onPollStarClickListener;
    }
}

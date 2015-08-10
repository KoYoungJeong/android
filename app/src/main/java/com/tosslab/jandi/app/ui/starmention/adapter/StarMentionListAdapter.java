package com.tosslab.jandi.app.ui.starmention.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.messages.RefreshOldStarMentionedEvent;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.ui.starmention.StarMentionListActivity;
import com.tosslab.jandi.app.ui.starmention.viewholder.CommentStarMentionViewHolder;
import com.tosslab.jandi.app.ui.starmention.viewholder.CommonStarMentionViewHolder;
import com.tosslab.jandi.app.ui.starmention.viewholder.FileStarMentionViewHolder;
import com.tosslab.jandi.app.ui.starmention.viewholder.MessageStarMentionViewHolder;
import com.tosslab.jandi.app.ui.starmention.viewholder.RecyclerViewFactory;
import com.tosslab.jandi.app.ui.starmention.vo.StarMentionVO;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.GenerateMentionMessageUtil;
import com.tosslab.jandi.app.utils.GlideCircleTransform;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by tee on 15. 7. 29..
 */
public class StarMentionListAdapter extends RecyclerView.Adapter<CommonStarMentionViewHolder> {

    MoreState moreState = MoreState.Idle;
    private List<StarMentionVO> starMentionList = new ArrayList<>();
    private RecyclerViewFactory recyclerViewFactory;
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;
    private String listType = StarMentionListActivity.TYPE_MENTION_LIST;

    @Override
    public CommonStarMentionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (recyclerViewFactory == null) {
            recyclerViewFactory = new RecyclerViewFactory();
        }
        return recyclerViewFactory.getViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(CommonStarMentionViewHolder holder, int position) {
        StarMentionVO starMentionVO = starMentionList.get(position);

        Glide.with(holder.getStarMentionProfileView().getContext())
                .load(starMentionVO.getWriterPictureUrl())
                .placeholder(R.drawable.jandi_profile)
                .error(R.drawable.jandi_profile)
                .crossFade()
                .transform(new GlideCircleTransform(holder.getStarMentionProfileView().getContext()))
                .into(holder.getStarMentionProfileView());

        holder.getStarMentionNameView().setText(starMentionVO.getWriterName());

        String updateTime = DateTransformator.getTimeString(starMentionVO.getUpdatedAt());

        holder.getStarMentionDateView().setText(updateTime);

        if (getItemViewType(position) == StarMentionVO.Type.Text.getValue()) {

            MessageStarMentionViewHolder viewHolder = (MessageStarMentionViewHolder) holder;
            viewHolder.getStarMentionTopicNameView().setText(starMentionVO.getRoomName());

            SpannableStringBuilder messageStringBuilder = new SpannableStringBuilder(starMentionVO.getContent());

            GenerateMentionMessageUtil generateMentionMessageUtil = new GenerateMentionMessageUtil(
                    viewHolder.getStarMentionContentView(), messageStringBuilder, starMentionVO.getMentions(),
                    EntityManager.getInstance(viewHolder.getStarMentionContentView().getContext()).getMe().getId())
                    .setMeBackgroundColor(0xFF01a4e7)
                    .setMeTextColor(0xFFffffff)
                    .setPxSize(R.dimen.jandi_mention_star_list_item_font_size);

            messageStringBuilder = generateMentionMessageUtil.generate();
            // for single spannable
            messageStringBuilder.append(" ");
            viewHolder.getStarMentionContentView().setText(messageStringBuilder);


        } else if (getItemViewType(position) == StarMentionVO.Type.Comment.getValue()) {

            CommentStarMentionViewHolder viewHolder = (CommentStarMentionViewHolder) holder;
            viewHolder.getStarMentionFileNameView().setText(starMentionVO.getFileName());
            SpannableStringBuilder commentStringBuilder = new SpannableStringBuilder
                    (starMentionVO.getContent());

            GenerateMentionMessageUtil generateMentionMessageUtil = new GenerateMentionMessageUtil(
                    viewHolder.getStarMentionCommentView(), commentStringBuilder, starMentionVO.getMentions(),
                    EntityManager.getInstance(viewHolder.getStarMentionCommentView().getContext()).getMe().getId())
                    .setMeBackgroundColor(0xFF01a4e7)
                    .setMeTextColor(0xFFffffff)
                    .setPxSize(R.dimen.jandi_mention_star_list_item_font_size);
            commentStringBuilder = generateMentionMessageUtil.generate();
            // for single spannable
            commentStringBuilder.append(" ");
            viewHolder.getStarMentionCommentView().setText(commentStringBuilder);

        } else if (getItemViewType(position) == StarMentionVO.Type.File.getValue()) {

            FileStarMentionViewHolder viewHolder = (FileStarMentionViewHolder) holder;
            viewHolder.getStarMentionFileNameView().setText(starMentionVO.getFileName());
            viewHolder.getStarFileTypeView().setImageResource(starMentionVO.getImageResource());

        }

        holder.getConvertView().setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(StarMentionListAdapter.this, position);
            }
        });

        holder.getConvertView().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (onItemLongClickListener != null) {
                    return onItemLongClickListener.
                            onItemLongClick(StarMentionListAdapter.this, position);
                }
                return false;
            }
        });

        if (position == getItemCount() - 1 && moreState == MoreState.Idle) {
            moreState = MoreState.Loading;

            EventBus.getDefault().post(new RefreshOldStarMentionedEvent(getListType()));
        }

    }

    @Override
    public int getItemViewType(int position) {
        return starMentionList.get(position).getContentType();
    }

    @Override
    public int getItemCount() {
        return starMentionList.size();
    }

    public StarMentionVO getItemsByPosition(int position) {
        return starMentionList.get(position);
    }

    public void addStarMentionList(List<StarMentionVO> newStarMentionList) {

        if (this.starMentionList == null) {
            this.starMentionList = new ArrayList<StarMentionVO>();
        }

        this.starMentionList.addAll(newStarMentionList);

        notifyDataSetChanged();
    }

    public void removeStarMentionListAt(int position) {
        starMentionList.remove(position);
        notifyDataSetChanged();
    }

    public void removeStarMentionListAll() {
        starMentionList.clear();
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    public void setNoMoreLoad() {
        moreState = MoreState.NoMore;
    }

    public void setReadyMore() {
        moreState = MoreState.Idle;

    }

    public String getListType() {
        return listType;
    }

    public void setListType(String listType) {
        this.listType = listType;
    }

    public StarMentionVO getItem(int position) {
        return starMentionList.get(position);
    }

    public StarMentionVO remove(int position) {
        return starMentionList.remove(position);
    }

    private enum MoreState {
        Idle, Loading, NoMore
    }

    public interface OnItemClickListener {
        void onItemClick(StarMentionListAdapter adapter, int position);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(StarMentionListAdapter adapter, int position);
    }
}

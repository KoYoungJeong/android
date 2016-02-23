package com.tosslab.jandi.app.ui.maintab.topic.adapter.updated;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.maintab.topic.domain.Topic;
import com.tosslab.jandi.app.views.FixedLinearLayout;
import com.tosslab.jandi.app.views.listeners.OnRecyclerItemClickListener;
import com.tosslab.jandi.app.views.listeners.OnRecyclerItemLongClickListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UpdatedTopicAdapter extends RecyclerView.Adapter<UpdatedTopicAdapter.UpdatedTopicItemViewHolder> {

    private final Context context;
    private List<Topic> topicItemDataList;
    private OnRecyclerItemClickListener onRecyclerItemClickListener;
    private OnRecyclerItemLongClickListener onRecyclerItemLongClickListener;

    public UpdatedTopicAdapter(Context context) {
        this.context = context;
        topicItemDataList = new ArrayList<>();
    }

    @Override
    public UpdatedTopicItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.item_topic_list, parent, false);
        return new UpdatedTopicItemViewHolder(view);
    }

    public void addAll(List<Topic> topicItemDatas) {
        topicItemDataList.addAll(topicItemDatas);
    }

    public void clear() {
        topicItemDataList.clear();
    }

    public Topic getItem(int position) {
        return topicItemDataList.get(position);
    }

    @Override
    public int getItemCount() {
        return topicItemDataList.size();
    }

    @Override
    public void onBindViewHolder(UpdatedTopicItemViewHolder holder, int position) {
        Topic item = getItem(position);
        holder.container.setBackgroundResource(R.drawable.bg_list_item);
        holder.ivFolderItemUnderline.setVisibility(View.GONE);
        holder.ivDefaultUnderline.setVisibility(View.VISIBLE);
        holder.ivShadowUnderline.setVisibility(View.GONE);

        holder.itemView.setClickable(true);
        holder.tvTopicName.setText(item.getName());

        if (item.getUnreadCount() > 0) {
            holder.vgTopicBadge.setVisibility(View.VISIBLE);
            holder.tvTopicBadge.setText(String.valueOf(item.getUnreadCount()));
        } else {
            holder.vgTopicBadge.setVisibility(View.GONE);
        }
        String memberCount = context.getString(R.string.jandi_count_with_brace, item.getMemberCount());
        holder.tvTopicUserCnt.setText(memberCount);
        if (!TextUtils.isEmpty(item.getDescription())) {
            holder.tvTopicDescription.setVisibility(View.VISIBLE);
            holder.tvTopicDescription.setText(item.getDescription());
        } else {
            holder.tvTopicDescription.setVisibility(View.GONE);
        }

        if (item.isPublic()) {
            if (item.isStarred()) {
                holder.ivTopicIcon.setImageResource(R.drawable.topiclist_icon_topic_fav);
            } else {
                holder.ivTopicIcon.setImageResource(R.drawable.topiclist_icon_topic);
            }
        } else {
            if (item.isStarred()) {
                holder.ivTopicIcon.setImageResource(R.drawable.topiclist_icon_topic_private_fav);
            } else {
                holder.ivTopicIcon.setImageResource(R.drawable.topiclist_icon_topic_private);
            }
        }

        if (!item.isPushOn()) {
            holder.vPushOff.setVisibility(View.VISIBLE);
        } else {
            holder.vPushOff.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (onRecyclerItemClickListener != null) {
                onRecyclerItemClickListener.onItemClick(holder.itemView,
                        UpdatedTopicAdapter.this, position);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (onRecyclerItemLongClickListener != null) {
                onRecyclerItemLongClickListener.onItemClick(holder.itemView,
                        UpdatedTopicAdapter.this, position);
            }
            return false;
        });

    }

    public void setOnRecyclerItemClickListener(OnRecyclerItemClickListener onRecyclerItemClickListener) {
        this.onRecyclerItemClickListener = onRecyclerItemClickListener;
    }

    public void setOnRecyclerItemLongClickListener(OnRecyclerItemLongClickListener onRecyclerItemLongClickListener) {
        this.onRecyclerItemLongClickListener = onRecyclerItemLongClickListener;
    }

    public List<Topic> getItems() {
        return Collections.unmodifiableList(topicItemDataList);
    }

    static class UpdatedTopicItemViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout container;
        View vTopicSelector;
        ImageView ivTopicIcon;
        FixedLinearLayout vgTopicName;
        TextView tvTopicName;
        TextView tvTopicUserCnt;
        View vPushOff;
        TextView tvTopicDescription;
        TextView tvTopicBadge;
        ImageView ivDefaultUnderline;
        ImageView ivFolderItemUnderline;
        ImageView ivShadowUnderline;
        RelativeLayout vgTopicBadge;
        View vAnimator;

        public UpdatedTopicItemViewHolder(View itemView) {
            super(itemView);

            container = (RelativeLayout) itemView.findViewById(R.id.rl_topic_item_container);
            vTopicSelector = itemView.findViewById(R.id.v_entity_listitem_selector);
            ivTopicIcon = (ImageView) itemView.findViewById(R.id.iv_entity_listitem_icon);
            vgTopicName = (FixedLinearLayout) itemView.findViewById(R.id.vg_entity_listitem_name);
            tvTopicName = (TextView) itemView.findViewById(R.id.tv_entity_listitem_name);
            tvTopicUserCnt = (TextView) itemView.findViewById(R.id.tv_entity_listitem_additional);
            vPushOff = itemView.findViewById(R.id.v_push_off);
            tvTopicDescription = (TextView) itemView.findViewById(R.id.tv_entity_listitem_description);
            vgTopicBadge = (RelativeLayout) itemView.findViewById(R.id.vg_entity_listitem_badge);
            tvTopicBadge = (TextView) itemView.findViewById(R.id.tv_entity_listitem_badge);
            ivDefaultUnderline = (ImageView) itemView.findViewById(R.id.iv_default_underline);
            ivFolderItemUnderline = (ImageView) itemView.findViewById(R.id.iv_folder_item_underline);
            ivShadowUnderline = (ImageView) itemView.findViewById(R.id.iv_shadow_underline);
            vAnimator = itemView.findViewById(R.id.v_topic_item_animator);
        }
    }
}

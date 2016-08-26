package com.tosslab.jandi.app.ui.maintab.tabs.topic.adapter.updated;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.JoinableTopicCallEvent;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.adapter.folder.viewholder.TopicJoinButtonViewHolder;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.domain.Topic;
import com.tosslab.jandi.app.views.FixedLinearLayout;
import com.tosslab.jandi.app.views.listeners.OnRecyclerItemClickListener;
import com.tosslab.jandi.app.views.listeners.OnRecyclerItemLongClickListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

public class UpdatedTopicAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_JOIN_TOPIC = 1;
    private final Context context;
    private List<Topic> topicItemDataList;
    private OnRecyclerItemClickListener onRecyclerItemClickListener;
    private OnRecyclerItemLongClickListener onRecyclerItemLongClickListener;
    private long selectedEntity = -1;

    private AnimStatus animStatus = AnimStatus.READY;
    private ValueAnimator colorAnimator;


    public UpdatedTopicAdapter(Context context) {
        this.context = context;
        topicItemDataList = new ArrayList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType != TYPE_JOIN_TOPIC) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_topic_list, parent, false);
            return new UpdatedTopicItemViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_join_topic, parent, false);
            return new TopicJoinButtonViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position < getItemCount() - 1) {
            return 0;
        } else {
            return TYPE_JOIN_TOPIC;
        }
    }

    public Topic getItem(int position) {
        if (position < getItemCount()) {
            return topicItemDataList.get(position);
        } else {
            return null;
        }
    }

    @Override
    public int getItemCount() {
        return topicItemDataList.size();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_JOIN_TOPIC) {
            holder.itemView.setOnClickListener(v -> {
                EventBus.getDefault().post(new JoinableTopicCallEvent());
            });
            return;
        }

        UpdatedTopicItemViewHolder updatedHolder = (UpdatedTopicItemViewHolder) holder;
        Topic item = getItem(position);
        updatedHolder.container.setBackgroundResource(R.drawable.bg_list_item);
        updatedHolder.ivFolderItemUnderline.setVisibility(View.GONE);
        updatedHolder.ivDefaultUnderline.setVisibility(View.VISIBLE);
        updatedHolder.ivShadowUnderline.setVisibility(View.GONE);

        updatedHolder.itemView.setClickable(true);
        updatedHolder.tvTopicName.setText(item.getName());

        if (item.getUnreadCount() > 0) {
            updatedHolder.vgTopicBadge.setVisibility(View.VISIBLE);
            updatedHolder.tvTopicBadge.setText(String.valueOf(item.getUnreadCount()));
        } else {
            updatedHolder.vgTopicBadge.setVisibility(View.GONE);
        }
        String memberCount = context.getString(R.string.jandi_count_with_brace, item.getMemberCount());
        updatedHolder.tvTopicUserCnt.setText(memberCount);
        if (!TextUtils.isEmpty(item.getDescription())) {
            updatedHolder.tvTopicDescription.setVisibility(View.VISIBLE);
            updatedHolder.tvTopicDescription.setText(item.getDescription());
        } else {
            updatedHolder.tvTopicDescription.setVisibility(View.GONE);
        }

        if (item.isPublic()) {
            if (item.isStarred()) {
                updatedHolder.ivTopicIcon.setImageResource(R.drawable.topiclist_icon_topic_fav);
            } else {
                updatedHolder.ivTopicIcon.setImageResource(R.drawable.topiclist_icon_topic);
            }
        } else {
            if (item.isStarred()) {
                updatedHolder.ivTopicIcon.setImageResource(R.drawable.topiclist_icon_topic_private_fav);
            } else {
                updatedHolder.ivTopicIcon.setImageResource(R.drawable.topiclist_icon_topic_private);
            }
        }

        if (!item.isPushOn()) {
            updatedHolder.vPushOff.setVisibility(View.VISIBLE);
        } else {
            updatedHolder.vPushOff.setVisibility(View.GONE);
        }

        updatedHolder.itemView.setOnClickListener(v -> {
            if (onRecyclerItemClickListener != null) {
                onRecyclerItemClickListener.onItemClick(updatedHolder.itemView,
                        UpdatedTopicAdapter.this, position);
            }
        });

        updatedHolder.itemView.setOnLongClickListener(v -> {
            if (onRecyclerItemLongClickListener != null) {
                onRecyclerItemLongClickListener.onItemClick(updatedHolder.itemView,
                        UpdatedTopicAdapter.this, position);
            }
            return false;
        });

        boolean isSelectedEntity = item.getEntityId() == selectedEntity;
        if (isSelectedEntity && animStatus == AnimStatus.READY) {
            animateForSelectedEntity(updatedHolder.vAnimator);
        }
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        if (colorAnimator != null) {
            colorAnimator.cancel();
        }
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

    public void setItems(List<Topic> topics) {
        this.topicItemDataList = topics;
    }

    private void animateForSelectedEntity(final View targetView) {
        Context context = targetView.getContext();

        animStatus = AnimStatus.IN_ANIM;
        int colorFrom = Color.TRANSPARENT;
        int colorTo = context.getResources().getColor(R.color.jandi_accent_color_1f);

        colorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimator.setDuration(context.getResources().getInteger(R.integer.highlight_animation_time));
        colorAnimator.setRepeatMode(ValueAnimator.REVERSE);
        colorAnimator.setRepeatCount(1);
        colorAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animStatus = AnimStatus.FINISH;
                targetView.setBackgroundColor(Color.TRANSPARENT);
                colorAnimator.removeAllListeners();
            }
        });
        colorAnimator.addUpdateListener(animation ->
                targetView.setBackgroundColor((Integer) animation.getAnimatedValue()));
        colorAnimator.start();
    }


    public void startAnimation() {
        if (animStatus == AnimStatus.IDLE) {
            animStatus = AnimStatus.READY;
        }
    }

    public boolean isIdleOfAnim() {
        return animStatus == AnimStatus.IDLE;
    }

    public long getSelectedEntity() {
        return selectedEntity;
    }

    public void setSelectedEntity(long selectedEntity) {
        this.selectedEntity = selectedEntity;
        animStatus = AnimStatus.IDLE;
    }

    public int indexOfEntity(long entityId) {
        int itemCount = getItemCount();
        for (int idx = 0; idx < itemCount; idx++) {
            Topic item = getItem(idx);
            if (item != null
                    && item.getEntityId() == entityId) {
                return idx;
            }
        }
        return -1;
    }

    public void stopAnimation() {
        if (colorAnimator != null) {
            colorAnimator.cancel();
        }
    }

    private enum AnimStatus {
        READY, IN_ANIM, FINISH, IDLE
    }

    static class UpdatedTopicItemViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.rl_topic_item_container)
        RelativeLayout container;
        @Bind(R.id.iv_entity_listitem_icon)
        ImageView ivTopicIcon;
        @Bind(R.id.vg_entity_listitem_name)
        FixedLinearLayout vgTopicName;
        @Bind(R.id.tv_user_name)
        TextView tvTopicName;
        @Bind(R.id.tv_entity_listitem_additional)
        TextView tvTopicUserCnt;
        @Bind(R.id.v_push_off)
        View vPushOff;
        @Bind(R.id.tv_entity_listitem_description)
        TextView tvTopicDescription;
        @Bind(R.id.tv_entity_listitem_badge)
        TextView tvTopicBadge;
        @Bind(R.id.iv_default_underline)
        ImageView ivDefaultUnderline;
        @Bind(R.id.iv_folder_item_underline)
        ImageView ivFolderItemUnderline;
        @Bind(R.id.iv_shadow_underline)
        ImageView ivShadowUnderline;
        @Bind(R.id.vg_entity_listitem_badge)
        RelativeLayout vgTopicBadge;
        @Bind(R.id.v_topic_item_animator)
        View vAnimator;

        public UpdatedTopicItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}

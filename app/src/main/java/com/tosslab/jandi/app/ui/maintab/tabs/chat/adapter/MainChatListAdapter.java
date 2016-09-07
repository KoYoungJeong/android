package com.tosslab.jandi.app.ui.maintab.tabs.chat.adapter;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.maintab.tabs.chat.to.ChatItem;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;
import com.tosslab.jandi.app.views.listeners.OnRecyclerItemClickListener;
import com.tosslab.jandi.app.views.listeners.OnRecyclerItemLongClickListener;
import com.tosslab.jandi.app.views.listeners.SimpleEndAnimatorListener;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 1. 6..
 */
public class MainChatListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;

    private List<ChatItem> entities;
    private long selectedEntity = -1;
    private AnimStatus animStatus = AnimStatus.READY;
    private OnRecyclerItemClickListener onRecyclerItemClickListener;
    private OnRecyclerItemLongClickListener onRecyclerItemLongClickListener;

    public MainChatListAdapter(Context context) {
        this.context = context;
        entities = new ArrayList<>();
    }

    public ChatItem getItem(int position) {
        return entities.get(position);
    }

    public void setOnRecyclerItemClickListener(
            OnRecyclerItemClickListener onRecyclerItemClickListener) {
        this.onRecyclerItemClickListener = onRecyclerItemClickListener;
    }

    public void setOnRecyclerItemLongClickListener(
            OnRecyclerItemLongClickListener onRecyclerItemLongClickListener) {
        this.onRecyclerItemLongClickListener = onRecyclerItemLongClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_list, parent, false);
        return new ChatViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ChatViewHolder viewHolder = (ChatViewHolder) holder;

        ChatItem item = getItem(position);

        if (!item.isInactive()) {
            viewHolder.tvName.setText(item.getName());
        } else {
            viewHolder.tvName.setText(item.getEmail());
        }


        if (item.isStarred()) {
            viewHolder.ivFavorite.setVisibility(View.VISIBLE);
        } else {
            viewHolder.ivFavorite.setVisibility(View.INVISIBLE);
        }

        viewHolder.tvBadgeCount.setText(String.valueOf(item.getUnread()));

        if (item.getUnread() <= 0) {
            viewHolder.tvBadgeCount.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.tvBadgeCount.setVisibility(View.VISIBLE);
        }

        viewHolder.tvAdditional.setText(item.getLastMessage());


        if (item.getStatus()) {

            viewHolder.vDisableLineThrough.setVisibility(View.GONE);
            viewHolder.vDisableWarning.setVisibility(View.GONE);
            viewHolder.vDisableCover.setVisibility(View.GONE);

        } else {

            viewHolder.vDisableLineThrough.setVisibility(View.VISIBLE);
            viewHolder.vDisableWarning.setVisibility(View.VISIBLE);
            viewHolder.vDisableCover.setVisibility(View.VISIBLE);

        }

        if (item.getRoomId() == selectedEntity && animStatus == AnimStatus.READY) {
            animStatus = AnimStatus.IN_ANIM;
            int colorFrom = context.getResources().getColor(R.color.transparent);
            int colorTo = context.getResources().getColor(R.color.jandi_accent_color_1f);
            final ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
            colorAnimation.setDuration(context.getResources().getInteger(R.integer.highlight_animation_time));
            colorAnimation.setRepeatMode(ValueAnimator.REVERSE);
            colorAnimation.setRepeatCount(1);
            colorAnimation.addUpdateListener(animator -> viewHolder.selector.setBackgroundColor((Integer)
                    animator.getAnimatedValue()));

            colorAnimation.addListener(new SimpleEndAnimatorListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    animStatus = AnimStatus.FINISH;
                }
            });
            colorAnimation.start();
        }

        ImageView ivIcon = viewHolder.ivIcon;
        ivIcon.setOnClickListener(getProfileClickListener(item.getEntityId()));

        ViewGroup.LayoutParams layoutParams = ivIcon.getLayoutParams();

        boolean isJandiBot = TeamInfoLoader.getInstance().isJandiBot(item.getEntityId());
        if (!isJandiBot) {
            layoutParams.height = layoutParams.width;
        } else {
            layoutParams.height = layoutParams.width * 5 / 4;
        }
        ivIcon.setLayoutParams(layoutParams);

        if (!isJandiBot) {
            if (!item.isInactive()) {
                ImageUtil.loadProfileImage(ivIcon, item.getPhoto(), R.drawable.profile_img);
            } else {
                ivIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
                ImageLoader.loadFromResources(ivIcon, R.drawable.profile_img_dummyaccount_43);
            }

        } else {
            ivIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            ImageLoader.loadFromResources(ivIcon, R.drawable.bot_80x100);
        }

        if (onRecyclerItemClickListener != null) {
            viewHolder.itemView.setOnClickListener(v ->
                    onRecyclerItemClickListener.onItemClick(
                            viewHolder.itemView, MainChatListAdapter.this, position));
        }

        if (onRecyclerItemLongClickListener != null) {
            viewHolder.itemView.setOnLongClickListener(v -> {
                onRecyclerItemLongClickListener.onItemClick(viewHolder.itemView, this, position);
                return true;
            });
        }

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return entities.size();
    }

    private View.OnClickListener getProfileClickListener(long entityId) {
        return v -> {
            EventBus.getDefault().post(new ShowProfileEvent(entityId, ShowProfileEvent.From.Image));
        };
    }

    public void setChatItem(List<ChatItem> chatItems) {
        entities.clear();
        entities.addAll(chatItems);
    }

    public List<ChatItem> getChatItems() {
        return entities;
    }

    public void setSelectedEntity(long selectedEntity) {
        this.selectedEntity = selectedEntity;
        animStatus = AnimStatus.IDLE;
    }

    public void startAnimation() {
        if (animStatus == AnimStatus.IDLE) {
            animStatus = AnimStatus.READY;
        }
    }

    public int findPosition(long entityId) {
        int count = getItemCount();
        for (int idx = 0; idx < count; idx++) {
            if (getItem(idx).getEntityId() == entityId) {
                return idx;
            }
        }

        return -1;
    }

    private enum AnimStatus {
        IDLE, READY, IN_ANIM, FINISH
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivIcon;
        public ImageView ivFavorite;
        public TextView tvName;
        public TextView tvAdditional;
        public TextView tvBadgeCount;
        public View vDisableLineThrough;
        public View vDisableWarning;
        public View vDisableCover;
        public View selector;

        public ChatViewHolder(View itemView) {
            super(itemView);
            selector = itemView.findViewById(R.id.v_entity_listitem_selector);
            tvName = (TextView) itemView.findViewById(R.id.tv_user_name);
            ivIcon = (ImageView) itemView.findViewById(R.id.iv_entity_listitem_icon);
            ivFavorite = (ImageView) itemView.findViewById(R.id.iv_favorite);
            tvAdditional = (TextView) itemView.findViewById(R.id.tv_user_department);
            tvBadgeCount = (TextView) itemView.findViewById(R.id.tv_entity_listitem_badge);
            vDisableLineThrough = itemView.findViewById(R.id.iv_name_line_through);
            vDisableWarning = itemView.findViewById(R.id.iv_entity_listitem_warning);
            vDisableCover = itemView.findViewById(R.id.v_name_warning);
        }


    }

}
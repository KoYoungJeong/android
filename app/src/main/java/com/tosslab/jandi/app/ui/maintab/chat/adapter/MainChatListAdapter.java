package com.tosslab.jandi.app.ui.maintab.chat.adapter;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.ui.maintab.chat.to.ChatItem;
import com.tosslab.jandi.app.utils.UriFactory;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;
import com.tosslab.jandi.app.views.listeners.SimpleEndAnimatorListener;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 1. 6..
 */
public class MainChatListAdapter extends BaseAdapter {

    private Context context;

    private List<ChatItem> entities;
    private int selectedEntity = -1;
    private AnimStatus animStatus = AnimStatus.READY;

    public MainChatListAdapter(Context context) {
        this.context = context;
        entities = new ArrayList<ChatItem>();
    }

    @Override
    public int getCount() {
        return entities.size();
    }

    @Override
    public ChatItem getItem(int position) {
        return entities.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_chat_list, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.selector = convertView.findViewById(R.id.v_entity_listitem_selector);
            viewHolder.tvName = (TextView) convertView.findViewById(R.id.tv_entity_listitem_name);
            viewHolder.ivIcon = (SimpleDraweeView) convertView.findViewById(R.id.iv_entity_listitem_icon);
            viewHolder.ivFavorite = (ImageView) convertView.findViewById(R.id.iv_entity_listitem_fav);
            viewHolder.tvAdditional = (TextView) convertView.findViewById(R.id.tv_entity_listitem_user_count);
            viewHolder.tvBadgeCount = (TextView) convertView.findViewById(R.id.tv_entity_listitem_badge);
            viewHolder.vDisableLineThrough = convertView.findViewById(R.id.iv_entity_listitem_line_through);
            viewHolder.vDisableWarning = convertView.findViewById(R.id.iv_entity_listitem_warning);
            viewHolder.vDisableCover = convertView.findViewById(R.id.v_entity_listitem_warning);

            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        ChatItem item = getItem(position);

        viewHolder.tvName.setText(item.getName());
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
            Integer colorFrom = context.getResources().getColor(R.color.transparent);
            Integer colorTo = context.getResources().getColor(R.color.jandi_accent_color_1f);
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

        SimpleDraweeView ivIcon = viewHolder.ivIcon;
        ivIcon.setOnClickListener(getProfileClickListener(item.getEntityId()));

        boolean isUser = !EntityManager.getInstance().isBot(item.getEntityId());
        ViewGroup.LayoutParams layoutParams = ivIcon.getLayoutParams();
        if (isUser) {
            layoutParams.height = layoutParams.width;
        } else {
            layoutParams.height = layoutParams.width * 5 / 4;
        }
        ivIcon.setLayoutParams(layoutParams);

        if (isUser) {
            ImageUtil.loadProfileImage(ivIcon, Uri.parse(item.getPhoto()), R.drawable.profile_img);
        } else {
            ImageLoader.newBuilder()
                    .placeHolder(R.drawable.bot_80x100, ScalingUtils.ScaleType.CENTER_INSIDE)
                    .actualScaleType(ScalingUtils.ScaleType.CENTER_INSIDE)
                    .backgroundColor(Color.BLACK)
                    .load(UriFactory.getResourceUri(R.drawable.bot_80x100))
                    .into(ivIcon);

        }


        return convertView;
    }

    private View.OnClickListener getProfileClickListener(int entityId) {
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

    public void setSelectedEntity(int selectedEntity) {
        this.selectedEntity = selectedEntity;
        animStatus = AnimStatus.IDLE;
    }

    public void startAnimation() {
        if (animStatus == AnimStatus.IDLE) {
            animStatus = AnimStatus.READY;
        }
    }

    private enum AnimStatus {
        IDLE, READY, IN_ANIM, FINISH
    }

    static class ViewHolder {
        public Context context;
        public SimpleDraweeView ivIcon;
        public ImageView ivFavorite;
        public TextView tvName;
        public TextView tvAdditional;
        public TextView tvBadgeCount;
        public View vDisableLineThrough;
        public View vDisableWarning;
        public View vDisableCover;
        public View selector;
    }

}
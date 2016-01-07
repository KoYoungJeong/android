package com.tosslab.jandi.app.ui.entities.chats.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v4.util.Pair;
import android.text.TextUtils;
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
import com.tosslab.jandi.app.ui.entities.chats.to.ChatChooseItem;
import com.tosslab.jandi.app.ui.entities.chats.to.DisableDummyItem;
import com.tosslab.jandi.app.utils.UriFactory;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 1. 14..
 */
public class ChatChooseAdapter extends BaseAdapter {

    private Context context;
    private List<ChatChooseItem> chatChooseItems;

    public ChatChooseAdapter(Context context) {
        this.context = context;
        chatChooseItems = new ArrayList<ChatChooseItem>();
    }

    @Override
    public int getCount() {

        Pair<Integer, DisableDummyItem> item = getDisabledInfo();

        if (item.first > 0 && item.second != null && !item.second.isExpand()) {
            return chatChooseItems.size() - item.first;
        } else {
            return chatChooseItems.size();
        }

    }

    private Pair<Integer, DisableDummyItem> getDisabledInfo() {

        DisableDummyItem disableDummyItem = null;
        int disableCount = 0;

        for (int idx = chatChooseItems.size() - 1; idx >= 0; idx--) {
            ChatChooseItem chatChooseItem = chatChooseItems.get(idx);
            if (chatChooseItem.isEnabled()) {
                break;
            } else if (!(chatChooseItem instanceof DisableDummyItem)) {
                ++disableCount;
            }

            if (chatChooseItem instanceof DisableDummyItem) {
                disableDummyItem = (DisableDummyItem) chatChooseItem;
            }
        }

        return new Pair<Integer, DisableDummyItem>(disableCount, disableDummyItem);
    }

    @Override
    public ChatChooseItem getItem(int position) {
        return chatChooseItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        int itemViewType = getItemViewType(position);

        if (itemViewType == 0) {
            convertView = setChatChooseView(position, convertView, parent);
        } else {
            convertView = setDisableFoldingView(position, convertView, parent);
        }

        return convertView;
    }

    private View setDisableFoldingView(int position, View convertView, ViewGroup parent) {

        DisableFoldingViewHolder disableFoldingViewHolder;

        if (convertView == null) {
            disableFoldingViewHolder = new DisableFoldingViewHolder();

            convertView = LayoutInflater.from(context).inflate(R.layout.item_disabled_folding, parent, false);

            disableFoldingViewHolder.ivIcon = (ImageView) convertView.findViewById(R.id.iv_disabled_folding_icon);
            disableFoldingViewHolder.ivArrow = (ImageView) convertView.findViewById(R.id.iv_disabled_folding_arrow);
            disableFoldingViewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tv_disabled_folding_title);
            disableFoldingViewHolder.tvCount = (TextView) convertView.findViewById(R.id.tv_disabled_folding_count);

            convertView.setTag(R.id.chatchoose_disable_folding, disableFoldingViewHolder);

        } else {
            disableFoldingViewHolder = (DisableFoldingViewHolder) convertView.getTag(R.id.chatchoose_disable_folding);
        }

        DisableDummyItem item = ((DisableDummyItem) getItem(position));

        disableFoldingViewHolder.tvCount.setText(context.getString(R.string.jandi_count_with_brace, item.getDisabledCount()));

        return convertView;
    }

    private View setChatChooseView(int position, View convertView, ViewGroup parent) {
        ChatCHooseViewHolder chatCHooseViewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_entity_body_two_line, parent, false);
            chatCHooseViewHolder = new ChatCHooseViewHolder();
            chatCHooseViewHolder.tvName =
                    (TextView) convertView.findViewById(R.id.tv_entity_listitem_name);
            chatCHooseViewHolder.ivIcon =
                    (SimpleDraweeView) convertView.findViewById(R.id.iv_entity_listitem_icon);
            chatCHooseViewHolder.ivFavorite =
                    (ImageView) convertView.findViewById(R.id.iv_entity_listitem_fav);
            chatCHooseViewHolder.tvAdditional =
                    (TextView) convertView.findViewById(R.id.tv_entity_listitem_user_count);
            chatCHooseViewHolder.vDisableLineThrough =
                    convertView.findViewById(R.id.iv_entity_listitem_line_through);
            chatCHooseViewHolder.vDisableCover =
                    convertView.findViewById(R.id.v_entity_listitem_warning);
            chatCHooseViewHolder.ivKick = convertView.findViewById(R.id.iv_entity_listitem_user_kick);
            chatCHooseViewHolder.tvOwnerBadge =
                    (TextView) convertView.findViewById(R.id.tv_owner_badge);

            convertView.setTag(R.id.chatchoose_item, chatCHooseViewHolder);

        } else {
            chatCHooseViewHolder = (ChatCHooseViewHolder) convertView.getTag(R.id.chatchoose_item);
        }

        ChatChooseItem item = getItem(position);

        chatCHooseViewHolder.tvName.setText(item.getName());
        chatCHooseViewHolder.ivKick.setVisibility(View.GONE);

        Resources resources = context.getResources();
        chatCHooseViewHolder.tvOwnerBadge.setText(resources.getString(R.string.jandi_team_owner));
        chatCHooseViewHolder.tvOwnerBadge.setVisibility(item.isOwner() ? View.VISIBLE : View.GONE);

        if (!TextUtils.isEmpty(item.getStatusMessage())) {
            chatCHooseViewHolder.tvAdditional.setVisibility(View.VISIBLE);
        } else {
            chatCHooseViewHolder.tvAdditional.setVisibility(View.GONE);
        }
        chatCHooseViewHolder.tvAdditional.setText(item.getStatusMessage());

        if (item.isStarred()) {
            chatCHooseViewHolder.ivFavorite.setVisibility(View.VISIBLE);
        } else {
            chatCHooseViewHolder.ivFavorite.setVisibility(View.GONE);
        }

        if (item.isEnabled()) {
            chatCHooseViewHolder.vDisableLineThrough.setVisibility(View.GONE);
            chatCHooseViewHolder.vDisableCover.setVisibility(View.GONE);
        } else {
            chatCHooseViewHolder.vDisableLineThrough.setVisibility(View.VISIBLE);
            chatCHooseViewHolder.vDisableCover.setVisibility(View.VISIBLE);
        }

        SimpleDraweeView imageViewIcon = chatCHooseViewHolder.ivIcon;
        imageViewIcon.setOnClickListener(getProfileClickListener(item.getEntityId()));

        boolean user = !item.isBot();

        ViewGroup.LayoutParams layoutParams = imageViewIcon.getLayoutParams();
        if (user) {
            layoutParams.height = layoutParams.width;
        } else {
            layoutParams.height = layoutParams.width * 5 / 4;
        }

        imageViewIcon.setLayoutParams(layoutParams);

        if (user) {
            ImageUtil.loadProfileImage(imageViewIcon, item.getPhotoUrl(), R.drawable.profile_img);
        } else {
            ImageLoader.newBuilder()
                    .placeHolder(R.drawable.bot_43x54, ScalingUtils.ScaleType.CENTER_INSIDE)
                    .actualScaleType(ScalingUtils.ScaleType.CENTER_INSIDE)
                    .load(UriFactory.getResourceUri(R.drawable.bot_43x54))
                    .into(imageViewIcon);
        }
        return convertView;
    }

    private View.OnClickListener getProfileClickListener(int entityId) {
        return v -> {
            EventBus.getDefault().post(new ShowProfileEvent(entityId, ShowProfileEvent.From.Image));
        };
    }

    public void add(ChatChooseItem chatChooseItem) {
        chatChooseItems.add(chatChooseItem);
    }

    public void addAll(List<ChatChooseItem> chatListWithoutMe) {
        chatChooseItems.addAll(chatListWithoutMe);
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position) instanceof DisableDummyItem ? 1 : 0;
    }

    public void clear() {
        chatChooseItems.clear();
    }

    public void remove(ChatChooseItem chatChooseItem) {
        chatChooseItems.remove(chatChooseItem);
    }

    static class ChatCHooseViewHolder {
        public Context context;
        public SimpleDraweeView ivIcon;
        public ImageView ivFavorite;
        public TextView tvName;
        public TextView tvAdditional;
        public View vDisableLineThrough;
        public View vDisableCover;
        public View ivKick;
        public TextView tvOwnerBadge;
    }

    static class DisableFoldingViewHolder {
        public TextView tvTitle;
        public ImageView ivIcon;
        public ImageView ivArrow;
        public TextView tvCount;
    }
}

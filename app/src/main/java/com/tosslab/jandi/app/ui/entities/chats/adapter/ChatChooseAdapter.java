package com.tosslab.jandi.app.ui.entities.chats.adapter;

import android.content.Context;
import android.content.res.Resources;
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
import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;
import com.tosslab.jandi.app.ui.entities.chats.domain.DisableDummyItem;
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
        chatChooseItems = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return chatChooseItems.size();
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
            convertView = setDisableFoldingView(convertView, parent);
        }

        return convertView;
    }

    private View setDisableFoldingView(View convertView, ViewGroup parent) {

        DisableFoldingViewHolder disableFoldingViewHolder;

        if (convertView == null) {
            disableFoldingViewHolder = new DisableFoldingViewHolder();

            convertView = LayoutInflater.from(context).inflate(R.layout.item_disabled_folding, parent, false);

            disableFoldingViewHolder.ivIcon = (ImageView) convertView.findViewById(R.id.iv_disabled_folding_icon);
            disableFoldingViewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tv_disabled_folding_title);

            convertView.setTag(R.id.chatchoose_disable_folding, disableFoldingViewHolder);
        }


        return convertView;
    }

    private View setChatChooseView(int position, View convertView, ViewGroup parent) {
        ChatChooseViewHolder chatChooseViewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_entity_body_two_line, parent, false);
            chatChooseViewHolder = new ChatChooseViewHolder();
            chatChooseViewHolder.tvName =
                    (TextView) convertView.findViewById(R.id.tv_entity_listitem_name);
            chatChooseViewHolder.ivIcon =
                    (SimpleDraweeView) convertView.findViewById(R.id.iv_entity_listitem_icon);
            chatChooseViewHolder.ivFavorite =
                    (ImageView) convertView.findViewById(R.id.iv_entity_listitem_fav);
            chatChooseViewHolder.tvAdditional =
                    (TextView) convertView.findViewById(R.id.tv_entity_listitem_user_count);
            chatChooseViewHolder.vDisableLineThrough =
                    convertView.findViewById(R.id.iv_entity_listitem_line_through);
            chatChooseViewHolder.vDisableCover =
                    convertView.findViewById(R.id.v_entity_listitem_warning);
            chatChooseViewHolder.ivKick = convertView.findViewById(R.id.iv_entity_listitem_user_kick);
            chatChooseViewHolder.tvOwnerBadge =
                    (TextView) convertView.findViewById(R.id.tv_owner_badge);

            convertView.setTag(R.id.chatchoose_item, chatChooseViewHolder);

        } else {
            chatChooseViewHolder = (ChatChooseViewHolder) convertView.getTag(R.id.chatchoose_item);
        }

        ChatChooseItem item = getItem(position);

        if (!item.isInactive()) {
            chatChooseViewHolder.tvName.setText(item.getName());
        } else {
            chatChooseViewHolder.tvName.setText(item.getEmail());
        }
        chatChooseViewHolder.ivKick.setVisibility(View.GONE);

        Resources resources = context.getResources();
        chatChooseViewHolder.tvOwnerBadge.setText(resources.getString(R.string.jandi_team_owner));
        chatChooseViewHolder.tvOwnerBadge.setVisibility(item.isOwner() ? View.VISIBLE : View.GONE);

        if (!TextUtils.isEmpty(item.getStatusMessage())) {
            chatChooseViewHolder.tvAdditional.setVisibility(View.VISIBLE);
        } else {
            chatChooseViewHolder.tvAdditional.setVisibility(View.GONE);
        }
        chatChooseViewHolder.tvAdditional.setText(item.getStatusMessage());

        if (item.isStarred()) {
            chatChooseViewHolder.ivFavorite.setVisibility(View.VISIBLE);
        } else {
            chatChooseViewHolder.ivFavorite.setVisibility(View.GONE);
        }

        if (item.isEnabled()) {
            chatChooseViewHolder.vDisableLineThrough.setVisibility(View.GONE);
            chatChooseViewHolder.vDisableCover.setVisibility(View.GONE);
        } else {
            chatChooseViewHolder.vDisableLineThrough.setVisibility(View.VISIBLE);
            chatChooseViewHolder.vDisableCover.setVisibility(View.VISIBLE);
        }

        SimpleDraweeView imageViewIcon = chatChooseViewHolder.ivIcon;
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
            if (!item.isInactive()) {
                ImageUtil.loadProfileImage(imageViewIcon, item.getPhotoUrl(), R.drawable.profile_img);
            } else {
                ImageUtil.loadProfileImage(imageViewIcon,
                        UriFactory.getResourceUri(R.drawable.profile_img_dummyaccount_43),
                        R.drawable.profile_img_dummyaccount_43);
            }
        } else {
            ImageLoader.newBuilder()
                    .placeHolder(R.drawable.bot_43x54, ScalingUtils.ScaleType.CENTER_INSIDE)
                    .actualScaleType(ScalingUtils.ScaleType.CENTER_INSIDE)
                    .load(UriFactory.getResourceUri(R.drawable.bot_43x54))
                    .into(imageViewIcon);
        }
        return convertView;
    }

    private View.OnClickListener getProfileClickListener(long entityId) {
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

    static class ChatChooseViewHolder {
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
    }
}

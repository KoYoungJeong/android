package com.tosslab.jandi.app.ui.entities.chats.adapter;

import android.content.Context;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.profile.ProfileDetailEvent;
import com.tosslab.jandi.app.ui.entities.chats.to.ChatChooseItem;
import com.tosslab.jandi.app.ui.entities.chats.to.DisableDummyItem;
import com.tosslab.jandi.app.utils.IonCircleTransform;

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

            disableFoldingViewHolder.imageViewIcon = (ImageView) convertView.findViewById(R.id.img_disabled_folding_icon);
            disableFoldingViewHolder.imageViewArrow = (ImageView) convertView.findViewById(R.id.img_disabled_folding_arrow);
            disableFoldingViewHolder.textViewTitle = (TextView) convertView.findViewById(R.id.txt_disabled_folding_title);
            disableFoldingViewHolder.textViewCount = (TextView) convertView.findViewById(R.id.txt_disabled_folding_count);

            convertView.setTag(R.id.chatchoose_disable_folding, disableFoldingViewHolder);

        } else {
            disableFoldingViewHolder = (DisableFoldingViewHolder) convertView.getTag(R.id.chatchoose_disable_folding);
        }

        DisableDummyItem item = ((DisableDummyItem) getItem(position));

        disableFoldingViewHolder.textViewCount.setText(context.getString(R.string.jandi_count_with_brace, item.getDisabledCount()));

        return convertView;
    }

    private View setChatChooseView(int position, View convertView, ViewGroup parent) {
        ChatCHooseViewHolder chatCHooseViewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_entity_body, parent, false);
            chatCHooseViewHolder = new ChatCHooseViewHolder();
            chatCHooseViewHolder.textViewName = (TextView) convertView.findViewById(R.id.txt_entity_listitem_name);
            chatCHooseViewHolder.imageViewIcon = (ImageView) convertView.findViewById(R.id.img_entity_listitem_icon);
            chatCHooseViewHolder.imageViewFavorite = (ImageView) convertView.findViewById(R.id.img_entity_listitem_fav);
            chatCHooseViewHolder.textViewAdditional = (TextView) convertView.findViewById(R.id.txt_entity_listitem_user_count);
            chatCHooseViewHolder.disableLineThrouthView = convertView.findViewById(R.id.img_entity_listitem_line_through);
            chatCHooseViewHolder.disableCoverView = convertView.findViewById(R.id.view_entity_listitem_warning);


            convertView.setTag(R.id.chatchoose_item, chatCHooseViewHolder);

        } else {
            chatCHooseViewHolder = (ChatCHooseViewHolder) convertView.getTag(R.id.chatchoose_item);
        }

        ChatChooseItem item = getItem(position);

        chatCHooseViewHolder.textViewName.setText(item.getName());

        if (!TextUtils.isEmpty(item.getEmail())) {
            chatCHooseViewHolder.textViewAdditional.setVisibility(View.VISIBLE);
        } else {
            chatCHooseViewHolder.textViewAdditional.setVisibility(View.GONE);
        }
        chatCHooseViewHolder.textViewAdditional.setText(item.getEmail());

        if (item.isStarred()) {
            chatCHooseViewHolder.imageViewFavorite.setVisibility(View.VISIBLE);
        } else {
            chatCHooseViewHolder.imageViewFavorite.setVisibility(View.GONE);
        }

        if (item.isEnabled()) {
            chatCHooseViewHolder.disableLineThrouthView.setVisibility(View.GONE);
            chatCHooseViewHolder.disableCoverView.setVisibility(View.GONE);
        } else {
            chatCHooseViewHolder.disableLineThrouthView.setVisibility(View.VISIBLE);
            chatCHooseViewHolder.disableCoverView.setVisibility(View.VISIBLE);
        }

        chatCHooseViewHolder.imageViewIcon.setOnClickListener(getProfileClickListener(item.getEntityId()));
        chatCHooseViewHolder.imageViewIcon.setImageResource(R.drawable.profile_img);

        Ion.with(chatCHooseViewHolder.imageViewIcon)
                .placeholder(R.drawable.profile_img)
                .error(R.drawable.profile_img)
                .transform(new IonCircleTransform())
                .load(item.getPhotoUrl());
        return convertView;
    }

    private View.OnClickListener getProfileClickListener(int entityId) {
        return v -> {
            EventBus.getDefault().post(new ProfileDetailEvent(entityId));
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
        public ImageView imageViewIcon;
        public ImageView imageViewFavorite;
        public TextView textViewName;
        public TextView textViewAdditional;
        public View disableLineThrouthView;
        public View disableCoverView;
    }

    static class DisableFoldingViewHolder {
        public TextView textViewTitle;
        public ImageView imageViewIcon;
        public ImageView imageViewArrow;
        public TextView textViewCount;
    }
}

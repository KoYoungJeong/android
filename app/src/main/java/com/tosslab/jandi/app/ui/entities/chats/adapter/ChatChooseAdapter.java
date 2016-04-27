package com.tosslab.jandi.app.ui.entities.chats.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;
import com.tosslab.jandi.app.ui.entities.chats.domain.DisableDummyItem;
import com.tosslab.jandi.app.ui.entities.chats.domain.EmptyChatChooseItem;
import com.tosslab.jandi.app.ui.members.adapter.searchable.viewholder.EmptySearchedMemberViewHolder;
import com.tosslab.jandi.app.utils.UriFactory;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;
import com.tosslab.jandi.app.views.listeners.OnRecyclerItemClickListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

public class ChatChooseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements ChatChooseAdapterDataView, ChatChooseAdapterDataModel {

    private static final int TYPE_QUERY_EMPTY = 2;
    private static final int TYPE_DISABLED = 1;
    private static final int TYPE_NORMAL = 0;
    private Context context;
    private List<ChatChooseItem> chatChooseItems;
    private OnRecyclerItemClickListener onRecyclerItemClickListener;

    public ChatChooseAdapter(Context context) {
        this.context = context;
        chatChooseItems = new ArrayList<>();
    }

    @Override
    public boolean isEmpty() {
        return chatChooseItems.isEmpty();
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
    public int getItemCount() {
        return getCount();
    }


    @Override
    public void add(ChatChooseItem chatChooseItem) {
        chatChooseItems.add(chatChooseItem);
    }

    @Override
    public void addAll(List<ChatChooseItem> chatListWithoutMe) {
        chatChooseItems.addAll(chatListWithoutMe);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == TYPE_NORMAL) {
            View view = LayoutInflater.from(context)
                    .inflate(R.layout.item_entity_body_two_line, parent, false);
            return new ChatChooseViewHolder(view);
        } else if (viewType == TYPE_QUERY_EMPTY) {
            return EmptySearchedMemberViewHolder.newInstance(parent);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_disabled_folding, parent, false);
            return new DisableFoldingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int itemViewType = getItemViewType(position);
        if (itemViewType == TYPE_NORMAL) {
            ((ChatChooseViewHolder) holder).bind(getItem(position));
        } else if (itemViewType == TYPE_QUERY_EMPTY) {
            ((EmptySearchedMemberViewHolder) holder).onBindView(((EmptyChatChooseItem) getItem(position)).getQuery());
        }

        holder.itemView.setOnClickListener(v -> {
            if (onRecyclerItemClickListener != null) {
                onRecyclerItemClickListener.onItemClick(holder.itemView, ChatChooseAdapter.this, position);
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        ChatChooseItem item = getItem(position);
        if (item instanceof DisableDummyItem) {
            return TYPE_DISABLED;
        } else if (item instanceof EmptyChatChooseItem) {
            return TYPE_QUERY_EMPTY;
        } else {
            return TYPE_NORMAL;
        }
    }

    @Override
    public void clear() {
        chatChooseItems.clear();
    }

    @Override
    public void remove(ChatChooseItem chatChooseItem) {
        chatChooseItems.remove(chatChooseItem);
    }

    @Override
    public void refresh() {
        notifyDataSetChanged();
    }

    public void setOnRecyclerItemClickListener(OnRecyclerItemClickListener onRecyclerItemClickListener) {
        this.onRecyclerItemClickListener = onRecyclerItemClickListener;
    }

    static class ChatChooseViewHolder extends RecyclerView.ViewHolder {
        public Context context;
        @Bind(R.id.iv_entity_listitem_icon)
        public SimpleDraweeView ivIcon;
        @Bind(R.id.iv_entity_listitem_fav)
        public ImageView ivFavorite;
        @Bind(R.id.tv_entity_listitem_name)
        public TextView tvName;
        @Bind(R.id.tv_entity_listitem_user_count)
        public TextView tvAdditional;
        @Bind(R.id.iv_entity_listitem_line_through)
        public View vDisableLineThrough;
        @Bind(R.id.v_entity_listitem_warning)
        public View vDisableCover;
        @Bind(R.id.iv_entity_listitem_user_kick)
        public View ivKick;
        @Bind(R.id.tv_owner_badge)
        public TextView tvOwnerBadge;

        public ChatChooseViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            context = itemView.getContext();

        }

        void bind(ChatChooseItem item) {

            if (!item.isInactive()) {
                tvName.setText(item.getName());
            } else {
                tvName.setText(item.getEmail());
            }
            ivKick.setVisibility(View.GONE);

            Resources resources = context.getResources();
            tvOwnerBadge.setText(resources.getString(R.string.jandi_team_owner));
            tvOwnerBadge.setVisibility(item.isOwner() ? View.VISIBLE : View.GONE);

            if (!TextUtils.isEmpty(item.getStatusMessage())) {
                tvAdditional.setVisibility(View.VISIBLE);
            } else {
                tvAdditional.setVisibility(View.GONE);
            }
            tvAdditional.setText(item.getStatusMessage());

            if (item.isStarred()) {
                ivFavorite.setVisibility(View.VISIBLE);
            } else {
                ivFavorite.setVisibility(View.GONE);
            }

            if (item.isEnabled()) {
                vDisableLineThrough.setVisibility(View.GONE);
                vDisableCover.setVisibility(View.GONE);
            } else {
                vDisableLineThrough.setVisibility(View.VISIBLE);
                vDisableCover.setVisibility(View.VISIBLE);
            }

            SimpleDraweeView imageViewIcon = ivIcon;
            imageViewIcon.setOnClickListener(v ->
                    EventBus.getDefault().post(
                            new ShowProfileEvent(item.getEntityId(),
                                    ShowProfileEvent.From.Image)));

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
        }
    }

    static class DisableFoldingViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_disabled_folding_title)
        public TextView tvTitle;
        @Bind(R.id.iv_disabled_folding_icon)
        public ImageView ivIcon;

        public DisableFoldingViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}

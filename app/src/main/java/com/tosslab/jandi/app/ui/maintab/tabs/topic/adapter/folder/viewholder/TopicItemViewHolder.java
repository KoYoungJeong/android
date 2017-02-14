package com.tosslab.jandi.app.ui.maintab.tabs.topic.adapter.folder.viewholder;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.domain.TopicItemData;
import com.tosslab.jandi.app.views.FixedLinearLayout;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by tee on 2017. 2. 10..
 */

public class TopicItemViewHolder extends MainTopicViewHolder<TopicItemData> {

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
    @Bind(R.id.tv_entity_listitem_read_only)
    View vReadOnly;
    @Bind(R.id.vg_topic_description)
    View vgTopicDescription;

    private View itemView;

    private OnItemLongClickListener onItemLongClickListener;

    public TopicItemViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        this.itemView = itemView;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    @Override
    public void bind(TopicItemData item) {
        if (item == null) {
            return;
        }

        if (item.isInnerFolder()) {
            container.setBackgroundResource(R.drawable.bg_list_innerfolder_item);
            if (item.getChildIndex() < item.getParentChildCnt() - 1) {
                ivFolderItemUnderline.setVisibility(View.VISIBLE);
                ivDefaultUnderline.setVisibility(View.GONE);
            } else {
                ivFolderItemUnderline.setVisibility(View.GONE);
                ivDefaultUnderline.setVisibility(View.VISIBLE);
            }
            if (item.getChildIndex() == 0) {
                ivShadowUnderline.setVisibility(View.VISIBLE);
            } else {
                ivShadowUnderline.setVisibility(View.GONE);
            }
        } else {
            container.setBackgroundResource(R.drawable.bg_list_item);
            ivFolderItemUnderline.setVisibility(View.GONE);
            ivDefaultUnderline.setVisibility(View.VISIBLE);
            ivShadowUnderline.setVisibility(View.GONE);
        }


        if (itemView != null) {
            itemView.setClickable(true);
        }

        tvTopicName.setText(item.getName());

        if (item.getUnreadCount() > 0) {
            tvTopicBadge.setVisibility(View.VISIBLE);
            if (item.getUnreadCount() > 999) {
                tvTopicBadge.setText(String.valueOf(999));
            } else {
                tvTopicBadge.setText(String.valueOf(item.getUnreadCount()));
            }
        } else {
            tvTopicBadge.setVisibility(View.GONE);
        }

        if (item.getUnreadCount() <= 0 && !item.isReadOnly()) {
            vgTopicBadge.setVisibility(View.GONE);
        } else {
            vgTopicBadge.setVisibility(View.VISIBLE);
        }

        tvTopicUserCnt.setText("(" + item.getMemberCount() + ")");
        if (!TextUtils.isEmpty(item.getDescription())) {
            vgTopicDescription.setVisibility(View.VISIBLE);
            tvTopicDescription.setVisibility(View.VISIBLE);
            tvTopicDescription.setText(item.getDescription());
        } else {
            vgTopicDescription.setVisibility(View.GONE);
            tvTopicDescription.setVisibility(View.GONE);
        }

        if (item.isReadOnly()) {
            if (vgTopicDescription.getVisibility() == View.GONE) {
                vgTopicDescription.setVisibility(View.VISIBLE);
            }
            vReadOnly.setVisibility(View.VISIBLE);
        } else {
            vReadOnly.setVisibility(View.GONE);
        }

        if (item.isPublic()) {
            if (item.isStarred()) {
                ivTopicIcon.setImageResource(R.drawable.topiclist_icon_topic_fav);
            } else {
                ivTopicIcon.setImageResource(R.drawable.topiclist_icon_topic);
            }
        } else {
            if (item.isStarred()) {
                ivTopicIcon.setImageResource(R.drawable.topiclist_icon_topic_private_fav);
            } else {
                ivTopicIcon.setImageResource(R.drawable.topiclist_icon_topic_private);
            }
        }

        if (!item.isPushOn()) {
            vPushOff.setVisibility(View.VISIBLE);
        } else {
            vPushOff.setVisibility(View.GONE);
        }

        itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onClick();
            }
        });

        itemView.setOnLongClickListener(v -> {
            if (onItemLongClickListener != null) {
                onItemLongClickListener.onLongClick();
            }
            return false;
        });
    }

    public View getvAnimator() {
        return vAnimator;
    }

    public interface OnItemLongClickListener {
        void onLongClick();
    }

}

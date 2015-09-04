package com.tosslab.jandi.app.ui.maintab.topics.adapter.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.libs.advancerecyclerview.utils.AbstractExpandableItemViewHolder;
import com.tosslab.jandi.app.views.FixedLinearLayout;

/**
 * Created by tee on 15. 8. 27..
 */
public class TopicItemViewHolder extends AbstractExpandableItemViewHolder {

    public RelativeLayout container;
    public View vTopicSelector;
    public ImageView ivTopicIcon;
    public FixedLinearLayout vgTopicName;
    public TextView tvTopicName;
    public TextView tvTopicUserCnt;
    public View vPushOff;
    public TextView tvTopicDescription;
    public RelativeLayout rlTopicBadge;
    public ImageView ivTopicListItemInfo;
    public TextView tvTopicBadge;
    public ImageView ivDefaultUnderline;
    public ImageView ivFolderItemUnderline;
    public ImageView ivShadowUnderline;
    public RelativeLayout vgTopicBadge;

    public TopicItemViewHolder(View itemView) {
        super(itemView);
        container = (RelativeLayout) itemView.findViewById(R.id.rl_topic_item_container);
        vTopicSelector = (View) itemView.findViewById(R.id.view_entity_listitem_selector);
        ivTopicIcon = (ImageView) itemView.findViewById(R.id.iv_entity_listitem_icon);
        vgTopicName = (FixedLinearLayout) itemView.findViewById(R.id.vg_entity_listitem_name);
        tvTopicName = (TextView) itemView.findViewById(R.id.tv_entity_listitem_name);
        tvTopicUserCnt = (TextView) itemView.findViewById(R.id.tv_entity_listitem_user_count);
        vPushOff = (View) itemView.findViewById(R.id.v_push_off);
        tvTopicDescription = (TextView) itemView.findViewById(R.id.tv_entity_listitem_description);
        rlTopicBadge = (RelativeLayout) itemView.findViewById(R.id.rl_entity_listitem_badge);
        ivTopicListItemInfo = (ImageView) itemView.findViewById(R.id.iv_entity_listitem_info);
        tvTopicBadge = (TextView) itemView.findViewById(R.id.tv_entity_listitem_badge);
        ivDefaultUnderline = (ImageView) itemView.findViewById(R.id.iv_default_underline);
        ivFolderItemUnderline = (ImageView) itemView.findViewById(R.id.iv_folder_item_underline);
        ivShadowUnderline = (ImageView) itemView.findViewById(R.id.iv_shadow_underline);
        vgTopicBadge = (RelativeLayout) itemView.findViewById(R.id.rl_entity_listitem_badge);
    }
}

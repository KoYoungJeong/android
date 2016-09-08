package com.tosslab.jandi.app.ui.maintab.tabs.topic.adapter.folder.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.libraries.advancerecyclerview.utils.AbstractExpandableItemViewHolder;
import com.tosslab.jandi.app.views.FixedLinearLayout;

/**
 * Created by tee on 15. 8. 27..
 */
public class TopicItemViewHolder extends AbstractExpandableItemViewHolder {

    public RelativeLayout container;
    public ImageView ivTopicIcon;
    public FixedLinearLayout vgTopicName;
    public TextView tvTopicName;
    public TextView tvTopicUserCnt;
    public View vPushOff;
    public TextView tvTopicDescription;
    public TextView tvTopicBadge;
    public ImageView ivDefaultUnderline;
    public ImageView ivFolderItemUnderline;
    public ImageView ivShadowUnderline;
    public RelativeLayout vgTopicBadge;
    public View vAnimator;

    public TopicItemViewHolder(View itemView) {
        super(itemView);
        container = (RelativeLayout) itemView.findViewById(R.id.rl_topic_item_container);
        ivTopicIcon = (ImageView) itemView.findViewById(R.id.iv_entity_listitem_icon);
        vgTopicName = (FixedLinearLayout) itemView.findViewById(R.id.vg_entity_listitem_name);
        tvTopicName = (TextView) itemView.findViewById(R.id.tv_user_name);
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

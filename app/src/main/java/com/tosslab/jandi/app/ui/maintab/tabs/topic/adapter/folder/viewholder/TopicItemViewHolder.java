package com.tosslab.jandi.app.ui.maintab.tabs.topic.adapter.folder.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.libraries.advancerecyclerview.utils.AbstractExpandableItemViewHolder;
import com.tosslab.jandi.app.views.FixedLinearLayout;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by tee on 15. 8. 27..
 */
public class TopicItemViewHolder extends AbstractExpandableItemViewHolder {

    @Bind(R.id.rl_topic_item_container)
    public RelativeLayout container;
    @Bind(R.id.iv_entity_listitem_icon)
    public ImageView ivTopicIcon;
    @Bind(R.id.vg_entity_listitem_name)
    public FixedLinearLayout vgTopicName;
    @Bind(R.id.tv_user_name)
    public TextView tvTopicName;
    @Bind(R.id.tv_entity_listitem_additional)
    public TextView tvTopicUserCnt;
    @Bind(R.id.v_push_off)
    public View vPushOff;
    @Bind(R.id.tv_entity_listitem_description)
    public TextView tvTopicDescription;
    @Bind(R.id.tv_entity_listitem_badge)
    public TextView tvTopicBadge;
    @Bind(R.id.iv_default_underline)
    public ImageView ivDefaultUnderline;
    @Bind(R.id.iv_folder_item_underline)
    public ImageView ivFolderItemUnderline;
    @Bind(R.id.iv_shadow_underline)
    public ImageView ivShadowUnderline;
    @Bind(R.id.vg_entity_listitem_badge)
    public RelativeLayout vgTopicBadge;
    @Bind(R.id.v_topic_item_animator)
    public View vAnimator;
    @Bind(R.id.tv_entity_listitem_read_only)
    public View vReadOnly;
    @Bind(R.id.vg_topic_description)
    public View vgTopicDescription;

    public TopicItemViewHolder(View itemView, boolean bindView) {
        super(itemView);
        if (bindView) {
            ButterKnife.bind(this, itemView);
        }
    }

}

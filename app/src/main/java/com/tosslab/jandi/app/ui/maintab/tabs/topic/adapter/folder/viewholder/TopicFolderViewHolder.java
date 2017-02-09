package com.tosslab.jandi.app.ui.maintab.tabs.topic.adapter.folder.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.libraries.advancerecyclerview.utils.AbstractExpandableItemViewHolder;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by tee on 15. 8. 27..
 */
public class TopicFolderViewHolder extends AbstractExpandableItemViewHolder {

    @Bind(R.id.rl_folder_container)
    public RelativeLayout container;

    @Bind(R.id.tv_topic_folder_title)
    public TextView tvTitle;
    @Bind(R.id.tv_topic_folder_count)
    public TextView tvChildCount;

    @Bind(R.id.tv_folder_listitem_badge)
    public TextView tvChildBadgeCnt;

    @Bind(R.id.iv_folder_default_underline)
    public ImageView ivDefaultUnderline;
    @Bind(R.id.iv_folder_setting)
    public View vgFolderSetting;

    public TopicFolderViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

}

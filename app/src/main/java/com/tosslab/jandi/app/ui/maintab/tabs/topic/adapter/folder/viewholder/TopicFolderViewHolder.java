package com.tosslab.jandi.app.ui.maintab.tabs.topic.adapter.folder.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.libraries.advancerecyclerview.utils.AbstractExpandableItemViewHolder;

/**
 * Created by tee on 15. 8. 27..
 */
public class TopicFolderViewHolder extends AbstractExpandableItemViewHolder {

    public RelativeLayout container;

    public ImageView ivFolderSetting;

    public TextView tvTitle;

    public TextView tvTopicCnt;

    public TextView tvChildBadgeCnt;

    public RelativeLayout vgChildBadgeCnt;
    public ImageView ivDefaultUnderline;
    public LinearLayout vgFolderSetting;

    public TopicFolderViewHolder(View itemView) {
        super(itemView);

        container = (RelativeLayout) itemView.findViewById(R.id.rl_folder_container);
        ivFolderSetting = (ImageView) itemView.findViewById(R.id.iv_folder_setting);
        vgFolderSetting = (LinearLayout) itemView.findViewById(R.id.ll_folder_setting);
        tvTitle = (TextView) itemView.findViewById(R.id.tv_topic_folder_title);
        tvTopicCnt = (TextView) itemView.findViewById(R.id.tv_topic_cnt);
        tvChildBadgeCnt = (TextView) itemView.findViewById(R.id.tv_folder_listitem_badge);
        vgChildBadgeCnt = (RelativeLayout) itemView.findViewById(R.id.rl_folder_listitem_badge);
        ivDefaultUnderline = (ImageView) itemView.findViewById(R.id.iv_folder_default_underline);

    }

}

package com.tosslab.jandi.app.ui.maintab.tabs.topic.adapter.folder.viewholder;

import android.content.res.Resources;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.domain.TopicFolderData;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by tee on 2017. 2. 10..
 */

public class TopicFolderViewHolder extends MainTopicViewHolder<TopicFolderData> {

    @Bind(R.id.rl_folder_container)
    RelativeLayout container;

    @Bind(R.id.iv_folder_setting)
    ImageView ivFolderSetting;

    @Bind(R.id.ll_folder_setting)
    LinearLayout vgFolderSetting;

    @Bind(R.id.tv_topic_folder_title)
    TextView tvTitle;

    @Bind(R.id.tv_topic_cnt)
    TextView tvTopicCnt;

    @Bind(R.id.tv_folder_listitem_badge)
    TextView tvChildBadgeCnt;

    @Bind(R.id.rl_folder_listitem_badge)
    RelativeLayout vgChildBadgeCnt;

    @Bind(R.id.iv_folder_default_underline)
    ImageView ivDefaultUnderline;

    private OnFolderSettingClickListener onFolderSettingClickListener;

    public TopicFolderViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void setOnFolderSettingClickListener(OnFolderSettingClickListener onFolderSettingClickListener) {
        this.onFolderSettingClickListener = onFolderSettingClickListener;
    }

    @Override
    public void bind(TopicFolderData item) {
        if (item == null) {
            return;
        }

        container.setVisibility(View.VISIBLE);
        tvTitle.setText(item.getTitle());
        tvTopicCnt.setText(String.valueOf(item.getItemCount()));
        itemView.setClickable(true);
        container.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onClick();
            }
        });

        Resources resources = container.getContext().getResources();

        if ((item.getItemCount() > 0) && (item.isOpened())) {
            tvTopicCnt.setBackgroundResource(R.drawable.topiclist_icon_folder_open);
            tvTopicCnt.setTextColor(resources.getColor(R.color.jandi_topic_folder_expand));
            tvTitle.setTextColor(resources.getColor(R.color.jandi_topic_folder_expand));
            ivDefaultUnderline.setVisibility(View.GONE);
            vgChildBadgeCnt.setVisibility(View.GONE);
        } else {
            tvTopicCnt.setBackgroundResource(R.drawable.topiclist_icon_folder);
            tvTopicCnt.setTextColor(resources.getColor(R.color.jandi_topic_folder_collapse));
            tvTitle.setTextColor(resources.getColor(R.color.jandi_topic_folder_collapse));
            ivDefaultUnderline.setVisibility(View.VISIBLE);
            if (item.getChildBadgeCnt() > 0) {
                vgChildBadgeCnt.setVisibility(View.VISIBLE);
                if (item.getChildBadgeCnt() > 999) {
                    tvChildBadgeCnt.setText(String.valueOf(999));
                } else {
                    tvChildBadgeCnt.setText(String.valueOf(item.getChildBadgeCnt()));
                }
            } else {
                vgChildBadgeCnt.setVisibility(View.GONE);
            }
        }

        vgFolderSetting.setClickable(true);
        vgFolderSetting.setOnClickListener(v -> {
            onFolderSettingClickListener.onClick();
        });
    }


    public interface OnFolderSettingClickListener {
        void onClick();
    }
}

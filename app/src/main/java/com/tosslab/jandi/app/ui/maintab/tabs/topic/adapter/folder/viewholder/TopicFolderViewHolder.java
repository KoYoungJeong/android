package com.tosslab.jandi.app.ui.maintab.tabs.topic.adapter.folder.viewholder;

import android.content.res.Resources;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.domain.TopicFolderData;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TopicFolderViewHolder extends MainTopicViewHolder<TopicFolderData> {

    @Bind(R.id.rl_folder_container)
    RelativeLayout container;

    @Bind(R.id.iv_folder_setting)
    ImageView ivFolderSetting;

    @Bind(R.id.tv_topic_folder_title)
    TextView tvTitle;

    @Bind(R.id.tv_topic_folder_count)
    TextView tvTopicCnt;

    @Bind(R.id.tv_folder_listitem_badge)
    TextView tvChildBadgeCnt;

    @Bind(R.id.iv_folder_default_underline)
    ImageView ivDefaultUnderline;

    private OnFolderSettingClickListener onFolderSettingClickListener;
    private View itemView;

    public TopicFolderViewHolder(View itemView) {
        super(itemView);
        this.itemView = itemView;
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
        tvTopicCnt.setText(String.format("(%d)", item.getItemCount()));
        itemView.setClickable(true);
        container.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onClick();
            }
        });

        Resources resources = container.getContext().getResources();

        if ((item.getItemCount() > 0) && (item.isOpened())) {
            ivDefaultUnderline.setVisibility(View.GONE);
            tvChildBadgeCnt.setVisibility(View.GONE);
            itemView.setBackgroundColor(resources.getColor(R.color.rgb_f5f5f5));
        } else {
            ivDefaultUnderline.setVisibility(View.VISIBLE);
            itemView.setBackgroundColor(resources.getColor(R.color.white));
            if (item.getChildBadgeCnt() > 0) {
                tvChildBadgeCnt.setVisibility(View.VISIBLE);
                if (item.getChildBadgeCnt() > 999) {
                    tvChildBadgeCnt.setText(String.valueOf(999));
                } else {
                    tvChildBadgeCnt.setText(String.valueOf(item.getChildBadgeCnt()));
                }
            } else {
                tvChildBadgeCnt.setVisibility(View.GONE);
            }
        }

        ivFolderSetting.setClickable(true);
        ivFolderSetting.setOnClickListener(v -> {
            onFolderSettingClickListener.onClick();
        });
    }


    public interface OnFolderSettingClickListener {
        void onClick();
    }
}

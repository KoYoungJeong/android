package com.tosslab.jandi.app.ui.starmention.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;

/**
 * Created by tee on 15. 7. 29..
 */
public class CommentViewHolder extends RecyclerView.ViewHolder {

    private ImageView starMentionProfileView;
    private TextView starMentionNameView;
    private TextView starMentionCommentView;
    private TextView starMentionFileNameView;
    private TextView starMentionDateView;

    public CommentViewHolder(View itemView) {
        super(itemView);

        starMentionProfileView = (ImageView) itemView.findViewById(R.id.iv_star_mention_profile);
        starMentionNameView = (TextView) itemView.findViewById(R.id.tv_star_mention_name);
        starMentionCommentView = (TextView) itemView.findViewById(R.id.tv_star_mention_comment);
        starMentionFileNameView = (TextView) itemView.findViewById(R.id.tv_star_mention_file_name);
        starMentionDateView = (TextView) itemView.findViewById(R.id.tv_star_mention_date);
    }

    public ImageView getStarMentionProfileView() {
        return starMentionProfileView;
    }

    public TextView getStarMentionNameView() {
        return starMentionNameView;
    }

    public TextView getStarMentionCommentView() {
        return starMentionCommentView;
    }

    public TextView getStarMentionFileNameView() {
        return starMentionFileNameView;
    }

    public TextView getStarMentionDateView() {
        return starMentionDateView;
    }
}

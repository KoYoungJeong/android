package com.tosslab.jandi.app.ui.starmention.viewholder;

import android.view.View;
import android.widget.TextView;

import com.tosslab.jandi.app.R;

/**
 * Created by tee on 15. 7. 29..
 */
public class CommentStarMentionViewHolder extends CommonStarMentionViewHolder {

    private TextView starMentionCommentView;
    private TextView starMentionFileNameView;

    public CommentStarMentionViewHolder(View itemView) {
        super(itemView);

        starMentionCommentView = (TextView) itemView.findViewById(R.id.tv_star_mention_comment);
        starMentionFileNameView = (TextView) itemView.findViewById(R.id.tv_star_mention_file_name);
    }


    public TextView getStarMentionCommentView() {
        return starMentionCommentView;
    }

    public TextView getStarMentionFileNameView() {
        return starMentionFileNameView;
    }

    @Override
    public String toString() {
        return "CommentStarMentionViewHolder{" +
                "starMentionCommentView=" + starMentionCommentView +
                ", starMentionFileNameView=" + starMentionFileNameView +
                '}';
    }
}

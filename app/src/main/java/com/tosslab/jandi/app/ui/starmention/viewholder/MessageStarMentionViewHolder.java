package com.tosslab.jandi.app.ui.starmention.viewholder;

import android.view.View;
import android.widget.TextView;

import com.tosslab.jandi.app.R;

/**
 * Created by tee on 15. 7. 29..
 */
public class MessageStarMentionViewHolder extends CommonStarMentionViewHolder {

    private TextView starMentionContentView;
    private TextView starMentionTopicNameView;

    public MessageStarMentionViewHolder(View itemView) {
        super(itemView);

        starMentionContentView = (TextView) itemView.findViewById(R.id.tv_star_mention_content);
        starMentionTopicNameView = (TextView) itemView.findViewById(R.id.tv_star_mention_topic_name);

    }

    public TextView getStarMentionContentView() {
        return starMentionContentView;
    }

    public TextView getStarMentionTopicNameView() {
        return starMentionTopicNameView;
    }

    @Override
    public String toString() {
        return "MessageStarMentionViewHolder{" +
                ", starMentionContentView=" + starMentionContentView +
                ", starMentionTopicNameView=" + starMentionTopicNameView +
                '}';
    }
}

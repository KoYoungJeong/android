package com.tosslab.jandi.app.ui.starmention.adapter.viewholder;

import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.ui.starmention.vo.StarMentionVO;
import com.tosslab.jandi.app.utils.GenerateMentionMessageUtil;

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

    @Override
    public void bindView(StarMentionVO starMentionVO) {
        super.bindView(starMentionVO);

        this.getStarMentionTopicNameView().setText(starMentionVO.getRoomName());

        SpannableStringBuilder messageStringBuilder = new SpannableStringBuilder(starMentionVO.getContent());

        GenerateMentionMessageUtil generateMentionMessageUtil = new GenerateMentionMessageUtil(
                this.getStarMentionContentView(), messageStringBuilder, starMentionVO.getMentions(),
                EntityManager.getInstance(this.getStarMentionContentView().getContext()).getMe().getId())
                .setMeBackgroundColor(0xFF01a4e7)
                .setMeTextColor(0xFFffffff)
                .setPxSize(R.dimen.jandi_mention_star_list_item_font_size);

        messageStringBuilder = generateMentionMessageUtil.generate(false);
        // for single spannable
        messageStringBuilder.append(" ");
        this.getStarMentionContentView().setText(messageStringBuilder);

    }
}

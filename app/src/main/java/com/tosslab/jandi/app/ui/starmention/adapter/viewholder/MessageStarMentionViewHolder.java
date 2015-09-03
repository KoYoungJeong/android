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

    private TextView tvMentionContent;
    private TextView tvMentionTopicName;

    public MessageStarMentionViewHolder(View itemView) {
        super(itemView);
        tvMentionContent = (TextView) itemView.findViewById(R.id.tv_star_mention_content);
        tvMentionTopicName = (TextView) itemView.findViewById(R.id.tv_star_mention_topic_name);

    }

    @Override
    public String toString() {
        return "MessageStarMentionViewHolder{" +
                ", tvMentionContent=" + tvMentionContent +
                ", tvMentionTopicName=" + tvMentionTopicName +
                '}';
    }

    @Override
    public void bindView(StarMentionVO starMentionVO) {
        super.bindView(starMentionVO);

        tvMentionTopicName.setText(starMentionVO.getRoomName());

        SpannableStringBuilder messageStringBuilder = new SpannableStringBuilder(starMentionVO.getBody());

        GenerateMentionMessageUtil generateMentionMessageUtil = new GenerateMentionMessageUtil(
                tvMentionContent, messageStringBuilder, starMentionVO.getMentions(),
                EntityManager.getInstance().getMe().getId())
                .setMeBackgroundColor(0xFF01a4e7)
                .setMeTextColor(0xFFffffff)
                .setBackgroundColor(0x00ffffff)
                .setTextColor(0xff000000)
                .setPxSize(R.dimen.jandi_mention_star_list_item_font_size);

        messageStringBuilder = generateMentionMessageUtil.generate(false);
        // for single spannable
        messageStringBuilder.append(" ");
        tvMentionContent.setText(messageStringBuilder);

    }
}

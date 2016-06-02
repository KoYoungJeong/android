package com.tosslab.jandi.app.ui.starmention.adapter.viewholder;

import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.spannable.SpannableLookUp;
import com.tosslab.jandi.app.spannable.analysis.mention.MentionAnalysisInfo;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.starmention.vo.StarMentionVO;

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

        long myId = TeamInfoLoader.getInstance().getMyId();
        MentionAnalysisInfo mentionAnalysisInfo =
                MentionAnalysisInfo.newBuilder(myId, starMentionVO.getMentions())
                        .textSizeFromResource(R.dimen.jandi_mention_star_list_item_font_size)
                        .forMeBackgroundColor(Color.parseColor("#FF01A4E7"))
                        .forMeTextColor(Color.WHITE)
                        .build();

        SpannableLookUp.text(messageStringBuilder)
                .hyperLink(false)
                .markdown(false)
                .mention(mentionAnalysisInfo, false)
                .lookUp(tvMentionContent.getContext());

        // for single spannable
        messageStringBuilder.append(" ");
        tvMentionContent.setText(messageStringBuilder);

    }
}

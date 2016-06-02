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
public class CommentStarMentionViewHolder extends CommonStarMentionViewHolder {

    private TextView tvComment;
    private TextView tvFileName;

    public CommentStarMentionViewHolder(View itemView) {
        super(itemView);

        tvComment = (TextView) itemView.findViewById(R.id.tv_star_mention_comment);
        tvFileName = (TextView) itemView.findViewById(R.id.tv_star_mention_file_name);
    }

    @Override
    public String toString() {
        return "CommentStarMentionViewHolder{" +
                "tvComment=" + tvComment +
                ", tvFileName=" + tvFileName +
                '}';
    }

    @Override
    public void bindView(StarMentionVO starMentionVO) {
        super.bindView(starMentionVO);
        tvFileName.setText(starMentionVO.getFileName());
        SpannableStringBuilder commentStringBuilder = new SpannableStringBuilder(starMentionVO.getBody());

        long myId = TeamInfoLoader.getInstance().getMyId();
        MentionAnalysisInfo mentionAnalysisInfo =
                MentionAnalysisInfo.newBuilder(myId, starMentionVO.getMentions())
                        .textSizeFromResource(R.dimen.jandi_mention_star_list_item_font_size)
                        .forMeBackgroundColor(Color.parseColor("#FF01A4E7"))
                        .forMeTextColor(Color.WHITE)
                        .build();

        SpannableLookUp.text(commentStringBuilder)
                .hyperLink(false)
                .markdown(false)
                .mention(mentionAnalysisInfo, false)
                .lookUp(tvFileName.getContext());

        // for single spannable
        commentStringBuilder.append(" ");
        tvComment.setText(commentStringBuilder);
    }
}

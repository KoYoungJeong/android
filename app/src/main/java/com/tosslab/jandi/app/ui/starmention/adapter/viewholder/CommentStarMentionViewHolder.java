package com.tosslab.jandi.app.ui.starmention.adapter.viewholder;

import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.ui.commonviewmodels.markdown.viewmodel.MarkdownViewModel;
import com.tosslab.jandi.app.ui.starmention.vo.StarMentionVO;
import com.tosslab.jandi.app.utils.GenerateMentionMessageUtil;

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

        MarkdownViewModel markdownViewModel = new MarkdownViewModel(tvFileName, commentStringBuilder, true);
        markdownViewModel.execute();

        GenerateMentionMessageUtil generateMentionMessageUtil = new GenerateMentionMessageUtil(
                tvComment, commentStringBuilder, starMentionVO.getMentions(),
                EntityManager.getInstance().getMe().getId())
                .setMeBackgroundColor(0xFF01a4e7)
                .setMeTextColor(0xFFffffff)
                .setPxSize(R.dimen.jandi_mention_star_list_item_font_size);

        commentStringBuilder = generateMentionMessageUtil.generate(false);
        // for single spannable
        commentStringBuilder.append(" ");
        tvComment.setText(commentStringBuilder);
    }
}

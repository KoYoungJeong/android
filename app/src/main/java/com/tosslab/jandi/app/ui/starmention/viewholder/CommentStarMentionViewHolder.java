package com.tosslab.jandi.app.ui.starmention.viewholder;

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

    @Override
    public void bindView(StarMentionVO starMentionVO) {
        super.bindView(starMentionVO);
        this.getStarMentionFileNameView().setText(starMentionVO.getFileName());
        SpannableStringBuilder commentStringBuilder = new SpannableStringBuilder
                (starMentionVO.getContent());

        GenerateMentionMessageUtil generateMentionMessageUtil = new GenerateMentionMessageUtil(
                this.getStarMentionCommentView(), commentStringBuilder, starMentionVO.getMentions(),
                EntityManager.getInstance(this.getStarMentionCommentView().getContext()).getMe().getId())
                .setMeBackgroundColor(0xFF01a4e7)
                .setMeTextColor(0xFFffffff)
                .setPxSize(R.dimen.jandi_mention_star_list_item_font_size);

        commentStringBuilder = generateMentionMessageUtil.generate();
        // for single spannable
        commentStringBuilder.append(" ");
        this.getStarMentionCommentView().setText(commentStringBuilder);
    }
}

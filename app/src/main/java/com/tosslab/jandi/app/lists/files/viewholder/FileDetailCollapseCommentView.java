package com.tosslab.jandi.app.lists.files.viewholder;

import android.content.res.Resources;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.View;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.LinkifyUtil;
import com.tosslab.jandi.app.views.spannable.DateViewSpannable;
import com.tosslab.jandi.app.views.spannable.ClickableMensionMessageSpannable;

public class FileDetailCollapseCommentView implements CommentViewHolder {

    TextView textViewCommentContent;

    @Override
    public void init(View rootView) {
        textViewCommentContent = (TextView) rootView.findViewById(R.id.txt_file_detail_collapse_comment_content);
    }

    @Override
    public void bind(ResMessages.OriginalMessage originalMessage) {

        ResMessages.CommentMessage commentMessage = (ResMessages.CommentMessage) originalMessage;
        // 날짜
        String createTime = DateTransformator.getTimeString(commentMessage.createTime);
        // 댓글 내용
        String comment = commentMessage.content.body;

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(comment);

        boolean hasLink =
                LinkifyUtil.addLinks(textViewCommentContent.getContext(), spannableStringBuilder);

        if (hasLink) {
            Spannable linkSpannable =
                    Spannable.Factory.getInstance().newSpannable(spannableStringBuilder);
            spannableStringBuilder.setSpan(linkSpannable,
                    0, comment.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            LinkifyUtil.setOnLinkClick(textViewCommentContent);
        }

        spannableStringBuilder.append(" ");

        Resources resources = textViewCommentContent.getResources();

        int dateSpannableTextSize = ((int) resources.getDimension(R.dimen.jandi_messages_date));
        int dateSpannableTextColor = resources.getColor(R.color.jandi_messages_date);

        int startIndex = spannableStringBuilder.length();
        spannableStringBuilder.append(" ");
        int endIndex = spannableStringBuilder.length();

        boolean hasMention = false;
        for (MentionObject mention : commentMessage.mentions) {
            String name = spannableStringBuilder.subSequence(mention.getOffset() + 1,
                    mention.getLength() + mention.getOffset()).toString();
            ClickableMensionMessageSpannable spannable1 = new ClickableMensionMessageSpannable(
                    textViewCommentContent.getContext(), name, mention.getId(), textViewCommentContent.getResources()
                    .getDimensionPixelSize(R.dimen.jandi_mention_comment_item_font_size));
            spannableStringBuilder.setSpan(spannable1, mention.getOffset(),
                    mention.getLength() + mention.getOffset(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (!hasMention) {
                hasMention = true;
            }
        }
        if (hasMention) {
            LinkifyUtil.setOnLinkClick(textViewCommentContent);
        }

        DateViewSpannable spannable =
                new DateViewSpannable(textViewCommentContent.getContext(), createTime);
        spannableStringBuilder.setSpan(spannable,
                startIndex, endIndex,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        textViewCommentContent.setText(spannableStringBuilder);
    }

    @Override
    public int getLayoutResourceId() {
        return R.layout.item_file_detail_collapse_comment;
    }
}

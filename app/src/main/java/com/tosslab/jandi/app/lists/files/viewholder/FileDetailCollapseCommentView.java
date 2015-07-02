package com.tosslab.jandi.app.lists.files.viewholder;

import android.content.res.Resources;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.View;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.LinkifyUtil;
import com.tosslab.jandi.app.views.spannable.DateViewSpannable;

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

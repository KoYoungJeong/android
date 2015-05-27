package com.tosslab.jandi.app.lists.files;

import android.content.Context;
import android.content.res.Resources;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.LinkifyUtil;
import com.tosslab.jandi.app.views.spannable.MessageSpannable;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

@EViewGroup(R.layout.item_file_detail_collapse_comment)
public class FileDetailCollapseCommentView extends LinearLayout {

    @ViewById(R.id.txt_file_detail_collapse_comment_content)
    TextView textViewCommentContent;

    Context mContext;

    public FileDetailCollapseCommentView(Context context) {
        super(context);
        mContext = context;
    }

    public void bind(ResMessages.CommentMessage commentMessage) {
        // 날짜
        String createTime = DateTransformator.getTimeDifference(commentMessage.createTime);
        // 댓글 내용
        String comment = commentMessage.content.body;
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(comment);

        boolean hasLink = LinkifyUtil.addLinks(textViewCommentContent.getContext(), spannableStringBuilder);

        if (hasLink) {
            Spannable linkSpannable = Spannable.Factory.getInstance().newSpannable(spannableStringBuilder);
            spannableStringBuilder.setSpan(linkSpannable, 0, comment.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            LinkifyUtil.setOnLinkClick(textViewCommentContent);
        }

        spannableStringBuilder.append(" ");

        Resources resources = mContext.getResources();

        int dateSpannableTextSize = ((int) resources.getDimension(R.dimen.jandi_messages_date));
        int dateSpannableTextColor = resources.getColor(R.color.jandi_messages_date);

        int startIndex = spannableStringBuilder.length();
        spannableStringBuilder.append(createTime);
        int endIndex = spannableStringBuilder.length();

        MessageSpannable spannable =
                new MessageSpannable(dateSpannableTextSize, dateSpannableTextColor);
        spannableStringBuilder.setSpan(spannable, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        textViewCommentContent.setText(spannableStringBuilder);
    }
}

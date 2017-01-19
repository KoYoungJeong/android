package com.tosslab.jandi.app.ui.filedetail.widget;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.text.Layout;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.View;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.utils.logger.LogUtil;

/**
 * Created by tonyjs on 16. 1. 27..
 */
public class LinkedEllipsizeTextView extends AppCompatTextView {
    public static final String TAG = LinkedEllipsizeTextView.class.getSimpleName();

    private SpannableString moreSpannable;
    private OnRequestMoreClickListener onRequestMoreClickListener;

    public LinkedEllipsizeTextView(Context context) {
        super(context);
        init();
    }

    public LinkedEllipsizeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LinkedEllipsizeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setOnRequestMoreClickListener(OnRequestMoreClickListener onRequestMoreClickListener) {
        this.onRequestMoreClickListener = onRequestMoreClickListener;
    }

    void init() {
        setMovementMethod(LinkMovementMethod.getInstance());

        setVerticalScrollBarEnabled(false);
        setMaxLines(3);

        String more = "... " + getResources().getString(R.string.jandi_action_more);
        moreSpannable = SpannableString.valueOf(more);
        moreSpannable.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                if (onRequestMoreClickListener != null) {
                    onRequestMoreClickListener.onRequestMore();
                }
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(getResources().getColor(R.color.jandi_text_light));
            }
        }, 0, moreSpannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        setLinkedText();
    }

    private void setLinkedText() {
        Layout layout = getLayout();
        if (layout == null) {
            return;
        }

        CharSequence text = getText();

        final int lineCount = layout.getLineCount();
        LogUtil.d(TAG, "lineCount = " + lineCount);

        if (lineCount <= 3) {
            return;
        }

        int lineStart = layout.getLineStart(2);
        int lineEnd = layout.getLineEnd(2);
        float lineMax = layout.getLineMax(2);

        SpannableStringBuilder determineBrokenText =
                new SpannableStringBuilder(text.subSequence(lineStart, lineEnd));
        determineBrokenText.append(moreSpannable);

        int length = determineBrokenText.length();
        int breakPoint =
                layout.getPaint().breakText(determineBrokenText.toString(), true, lineMax, null);
        LogUtil.i(TAG, "length - " + length + " breakPoint - " + breakPoint);
        // ... 더보기 라는 글자를 추가하면 넘친다.
        if (breakPoint < length) {
            text = text.subSequence(0,
                    Math.max(0, lineEnd - moreSpannable.length() - 1 - 1)/* 여백을 위해 한렝스 더 줄인다 */);
            SpannableStringBuilder sb = new SpannableStringBuilder(text);
            sb.append(moreSpannable);
            setText(sb);
        } else {
            SpannableStringBuilder sb = new SpannableStringBuilder(text);
            sb.append(moreSpannable);
            setText(sb);
        }
    }

    public interface OnRequestMoreClickListener {
        void onRequestMore();
    }
}

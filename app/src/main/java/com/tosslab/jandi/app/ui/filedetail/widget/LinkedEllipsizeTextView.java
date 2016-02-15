package com.tosslab.jandi.app.ui.filedetail.widget;

import android.content.Context;
import android.text.Layout;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.utils.logger.LogUtil;

/**
 * Created by tonyjs on 16. 1. 27..
 */
public class LinkedEllipsizeTextView extends TextView {
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

        float lineMax = layout.getWidth();
        float textSize = layout.getPaint().getTextSize();

        int maxTextCountFromLine = (int) Math.floor(lineMax / textSize);
        LogUtil.d(TAG, "maxTextCountFromLine = " + maxTextCountFromLine);

        int lineStart = layout.getLineStart(2);
        int lineEnd = layout.getLineEnd(2);

        LogUtil.e(TAG, String.format("lineMax = %f, lineStart = %d, lineEnd = %d", lineMax, lineStart, lineEnd));

        if ((lineEnd - lineStart) + moreSpannable.length() < maxTextCountFromLine) {
            SpannableStringBuilder sb = new SpannableStringBuilder(text);
            sb.append(moreSpannable);
            setText(sb);
        } else {
            text = text.subSequence(0, lineEnd - moreSpannable.length() - 1);
            SpannableStringBuilder sb = new SpannableStringBuilder(text);
            sb.append(moreSpannable);
            setText(sb);
        }
    }

    public interface OnRequestMoreClickListener {
        void onRequestMore();
    }
}

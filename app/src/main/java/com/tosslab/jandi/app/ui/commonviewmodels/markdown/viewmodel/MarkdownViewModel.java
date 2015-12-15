package com.tosslab.jandi.app.ui.commonviewmodels.markdown.viewmodel;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.widget.TextView;

import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tee on 15. 12. 8..
 */
public class MarkdownViewModel {

    SpannableStringBuilder messageStringBuilder;
    TextView tvMessageView;
    Boolean isPlainText = false;

    public MarkdownViewModel(TextView tvMessageView, SpannableStringBuilder messageStringBuilder, Boolean isPlainText) {
        this.messageStringBuilder = messageStringBuilder;
        this.tvMessageView = tvMessageView;
        this.isPlainText = isPlainText;
    }

    public void execute() {
        if (messageStringBuilder == null) {
            return;
        }
        Pattern p = getPattern();
        Matcher matcher = p.matcher(messageStringBuilder);

        while (matcher.find()) {
            Step step = getStep(matcher);
            int startIndex = getStartIndexOfString(step, matcher);
            int lastIndex = getEndIndexOfString(step, matcher);
            drawMarkDown(messageStringBuilder, startIndex, lastIndex, step);
            matcher.reset(messageStringBuilder);
        }
    }

    private Pattern getPattern() {
        return Pattern.compile("((\\~{2})([^~*]+)\\~{2})|((\\*{3})([^~*]+)\\*{3})|((\\*{2})([^~*]+)\\*{2})|((\\*)([^~*]+)\\*)");
    }

    public void drawMarkDown(SpannableStringBuilder messageStringBuilder, int startIndex, int endIndex, Step step) {
        LogUtil.e("메세지 :" + messageStringBuilder.toString() + startIndex + "부터" + endIndex + "까지");
        int cSize = getMarkdownCharacterLength(step);

        Object[] allSpans = messageStringBuilder.getSpans(startIndex + cSize, endIndex - cSize, Object.class);

        convertPlainTextFromPlainMarkdown(messageStringBuilder, startIndex, endIndex, step);

        if (!isPlainText) {
            setMarkdown(messageStringBuilder, startIndex, endIndex - cSize * 2, step);
            for (Object spanObject : allSpans) {
                removeOrChangeNestedSpan(messageStringBuilder, spanObject, step);
            }
        }
    }

    public void convertPlainTextFromPlainMarkdown(SpannableStringBuilder messageStringBuilder, int startIndex, int endIndex, Step step) {
        int cSize = getMarkdownCharacterLength(step);
        String message = messageStringBuilder.subSequence(startIndex + cSize, endIndex - cSize).toString();
        messageStringBuilder.replace(startIndex, endIndex, message);
    }

    public void setMarkdown(SpannableStringBuilder messageStringBuilder, int startIndex, int endIndex, Step step) {
        if (step == Step.STRIKE) {
            StrikethroughSpan span;
            span = new StrikethroughSpan();
            messageStringBuilder.setSpan(span, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            StyleSpan styleSpan = null;
            if (step == Step.BOLD_ITALIC) {
                styleSpan = new StyleSpan(Typeface.BOLD_ITALIC);
            } else if (step == Step.BOLD) {
                styleSpan = new StyleSpan(Typeface.BOLD);
            } else if (step == Step.ITALIC) {
                styleSpan = new StyleSpan(Typeface.ITALIC);
            }
            if (styleSpan != null) {
                messageStringBuilder.setSpan(styleSpan, startIndex,
                        endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    public void removeOrChangeNestedSpan(SpannableStringBuilder messageStringBuilder, Object spanObject, Step step) {
        if (spanObject instanceof StyleSpan) {
            StyleSpan span = (StyleSpan) spanObject;
            if (step == Step.BOLD_ITALIC) {
                if (span.getStyle() == Typeface.ITALIC
                        || span.getStyle() == Typeface.BOLD) {
                    messageStringBuilder.removeSpan(span);
                }
            } else if (step == Step.BOLD) {
                if (span.getStyle() == Typeface.ITALIC) {
                    int startIndex = messageStringBuilder.getSpanStart(span);
                    int endIndex = messageStringBuilder.getSpanEnd(span);
                    messageStringBuilder.removeSpan(span);
                    StyleSpan changedStyleSpan = new StyleSpan(Typeface.BOLD_ITALIC);
                    try {
                        messageStringBuilder.setSpan(changedStyleSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                }
            } else if (step == Step.ITALIC) {
                if (span.getStyle() == Typeface.BOLD) {
                    int startIndex = messageStringBuilder.getSpanStart(span);
                    int endIndex = messageStringBuilder.getSpanEnd(span);
                    messageStringBuilder.removeSpan(span);
                    StyleSpan changedStyleSpan = new StyleSpan(Typeface.BOLD_ITALIC);
                    try {
                        messageStringBuilder.setSpan(changedStyleSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public Step getStep(Matcher matcher) {
        if (!TextUtils.isEmpty(matcher.group(2))) {
            return Step.STRIKE;
        } else if (!TextUtils.isEmpty((matcher.group(5)))) {
            return Step.BOLD_ITALIC;
        } else if (!TextUtils.isEmpty((matcher.group(8)))) {
            return Step.BOLD;
        } else if (!TextUtils.isEmpty((matcher.group(11)))) {
            return Step.ITALIC;
        } else {
            return Step.STRIKE;
        }
    }

    private int getMarkdownCharacterLength(Step step) {
        if (step == Step.ITALIC) {
            return 1;
        } else if (step == Step.BOLD_ITALIC) {
            return 3;
        } else if (step == Step.BOLD) {
            return 2;
        } else {
            return 2;
        }
    }

    public int getStartIndexOfString(Step step, Matcher matcher) {
        if (step == Step.STRIKE) {
            return matcher.start(1);
        } else if (step == Step.BOLD_ITALIC) {
            return matcher.start(4);
        } else if (step == Step.BOLD) {
            return matcher.start(7);
        } else {
            return matcher.start(10);
        }
    }

    public int getEndIndexOfString(Step step, Matcher matcher) {
        if (step == Step.STRIKE) {
            return matcher.end(1);
        } else if (step == Step.BOLD_ITALIC) {
            return matcher.end(4);
        } else if (step == Step.BOLD) {
            return matcher.end(7);
        } else {
            return matcher.end(10);
        }
    }

    public static enum Step {
        BOLD_ITALIC, ITALIC, BOLD, STRIKE;
    }

}
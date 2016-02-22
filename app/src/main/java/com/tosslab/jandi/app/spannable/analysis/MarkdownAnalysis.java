package com.tosslab.jandi.app.spannable.analysis;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tonyjs on 16. 2. 19..
 */
public class MarkdownAnalysis implements RuleAnalysis {
    private static final Pattern pattern;

    static {
        pattern = Pattern.compile("((\\~{2})([^~*\n]+)\\~{2})|((\\*{3})([^~*\n]+)\\*{3})|((\\*{2})([^~*\n]+)\\*{2})|((\\*)([^~*\n]+)\\*)");
    }

    @Override
    public void analysis(Context context,
                         SpannableStringBuilder spannableStringBuilder, boolean plainText) {
        Matcher matcher = pattern.matcher(spannableStringBuilder);
        while (matcher.find()) {
            TextStyle style = getStyle(matcher);

            int startIndex = matcher.start(style.getStartIndex());
            int endIndex = matcher.end(style.getEndIndex());
            int needCharacterLength = style.getNeedCharacterLength();

            CharSequence sequence = spannableStringBuilder.subSequence(
                    startIndex + needCharacterLength, endIndex - needCharacterLength);
            spannableStringBuilder.replace(startIndex, endIndex, sequence);

            if (plainText) {
                return;
            }

            CharacterStyle span = style.getSpan();

            spannableStringBuilder.setSpan(span,
                    startIndex, endIndex - needCharacterLength * 2,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            matcher.reset(spannableStringBuilder);
        }
    }

    public TextStyle getStyle(Matcher matcher) {
        if (!TextUtils.isEmpty(matcher.group(2))) {
            return TextStyle.STRIKE;
        } else if (!TextUtils.isEmpty((matcher.group(5)))) {
            return TextStyle.BOLD_ITALIC;
        } else if (!TextUtils.isEmpty((matcher.group(8)))) {
            return TextStyle.BOLD;
        } else if (!TextUtils.isEmpty((matcher.group(11)))) {
            return TextStyle.ITALIC;
        } else {
            return TextStyle.STRIKE;
        }
    }

    public enum TextStyle {
        BOLD_ITALIC(3, 4, 4, new StyleSpan(Typeface.BOLD | Typeface.ITALIC)),
        ITALIC(1, 10, 10, new StyleSpan(Typeface.ITALIC)),
        BOLD(2, 7, 7, new StyleSpan(Typeface.BOLD)),
        STRIKE(2, 1, 1, new StrikethroughSpan());

        int needCharacterLength, startIndex, endIndex;
        CharacterStyle span;

        TextStyle(int needCharacterLength, int startIndex, int endIndex, CharacterStyle span) {
            this.needCharacterLength = needCharacterLength;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.span = span;
        }

        public int getNeedCharacterLength() {
            return needCharacterLength;
        }

        public int getStartIndex() {
            return startIndex;
        }

        public int getEndIndex() {
            return endIndex;
        }

        public CharacterStyle getSpan() {
            return span;
        }
    }
}

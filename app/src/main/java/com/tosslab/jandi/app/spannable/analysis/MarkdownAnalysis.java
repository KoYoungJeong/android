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

public class MarkdownAnalysis implements RuleAnalysis {

    private static final Pattern sPattern;
    private static int STRIKE_THROUGH = 99;

    static {
        sPattern = Pattern.compile(
                "([~]{2})((?:[^~\\s])|(?:[^~\\s](?:.*?)[^~\\s]))([~]{2})" +
                        "|([*]{3})((?:[^*\\s])|(?:[^*\\s](?:.*?)[^*\\s]))([*]{3})" +
                        "|([*]{2})((?:[^*\\s])|(?:[^*\\s](?:.*?)[^*\\s]))([*]{2})" +
                        "|([*]{1})((?:[^*\\s])|(?:[^*\\s](?:.*?)[^*\\s]))([*]{1})");
    }

    @Override
    public void analysis(Context context,
                         SpannableStringBuilder spannableStringBuilder, boolean plainText) {
        Matcher matcher = sPattern.matcher(spannableStringBuilder);
        while (matcher.find()) {
            TextStyle style = getStyle(matcher);

            int startIndex = matcher.start(style.getStartIndex());
            int endIndex = matcher.end(style.getEndIndex());

            CharSequence sequence = spannableStringBuilder.subSequence(startIndex, endIndex);
            startIndex = matcher.start();
            spannableStringBuilder.replace(startIndex, matcher.end(), sequence);

            if (plainText) {
                return;
            }

            endIndex = startIndex + sequence.length();

            CharacterStyle span = style.getSpan();

            spannableStringBuilder.setSpan(span,
                    startIndex, endIndex,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            matcher.reset(spannableStringBuilder);
        }
    }

    private TextStyle getStyle(Matcher matcher) {
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

    private enum TextStyle {
        STRIKE(2, 2, STRIKE_THROUGH),
        BOLD_ITALIC(5, 5, Typeface.BOLD | Typeface.ITALIC),
        BOLD(8, 8, Typeface.BOLD),
        ITALIC(11, 11, Typeface.ITALIC);

        private final int typeFace;
        private final int startIndex;
        private final int endIndex;

        TextStyle(int startIndex, int endIndex,
                  int typeFace) {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.typeFace = typeFace;
        }

        public int getStartIndex() {
            return startIndex;
        }

        public int getEndIndex() {
            return endIndex;
        }

        public CharacterStyle getSpan() {
            if (typeFace != STRIKE_THROUGH) {
                return new StyleSpan(typeFace);
            } else {
                return new StrikethroughSpan();
            }
        }
    }
}

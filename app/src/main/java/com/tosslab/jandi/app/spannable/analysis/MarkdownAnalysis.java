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

    private static final Pattern sPattern;
    public static int STRIKE_THROUGH = 99;

    static {
        sPattern = Pattern.compile(
                "(( \\~{2}[^ ])([^~\n]+)\\~{2})" +
                        "|((\\*{3})([^*\n]+)\\*{3})" +
                        "|((\\*{2})([^*\n]+)\\*{2})" +
                        "|((\\*)([^*\n]+)\\*)");
    }

    @Override
    public void analysis(Context context,
                         SpannableStringBuilder spannableStringBuilder, boolean plainText) {
        Matcher matcher = sPattern.matcher(spannableStringBuilder);
        while (matcher.find()) {
            TextStyle style = getStyle(matcher);

            int startIndex = matcher.start(style.getStartIndex());
            int endIndex = matcher.end(style.getEndIndex());
            int needCharacterLength = style.getNeedCharacterLength();
            int beforeWhiteSpaces = style.getBeforeWhiteSpaces();
            int afterWhiteSpaces = style.getAfterWhiteSpaces();

            CharSequence sequence = spannableStringBuilder.subSequence(
                    startIndex + needCharacterLength + beforeWhiteSpaces,
                    endIndex - needCharacterLength - afterWhiteSpaces);
            spannableStringBuilder.replace(startIndex, endIndex, sequence);

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
        BOLD_ITALIC(3, 4, 4, 0, 0, Typeface.BOLD | Typeface.ITALIC),
        ITALIC(1, 10, 10, 0, 0, Typeface.ITALIC),
        BOLD(2, 7, 7, 0, 0, Typeface.BOLD),
        STRIKE(2, 1, 1, 1, 0, STRIKE_THROUGH);

        private final int typeFace;
        int needCharacterLength, startIndex, endIndex, beforeWhiteSpaces, afterWhiteSpaces;

        TextStyle(int needCharacterLength, int startIndex, int endIndex,
                  int beforeWhiteSpaces, int afterWhiteSpaces, int typeFace) {
            this.needCharacterLength = needCharacterLength;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.beforeWhiteSpaces = beforeWhiteSpaces;
            this.afterWhiteSpaces = afterWhiteSpaces;
            this.typeFace = typeFace;
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

        public int getBeforeWhiteSpaces() {
            return beforeWhiteSpaces;
        }

        public int getAfterWhiteSpaces() {
            return afterWhiteSpaces;
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

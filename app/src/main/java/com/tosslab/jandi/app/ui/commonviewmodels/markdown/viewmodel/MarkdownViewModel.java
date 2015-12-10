package com.tosslab.jandi.app.ui.commonviewmodels.markdown.viewmodel;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.TypedValue;
import android.widget.TextView;

import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.views.spannable.MarkdownSpannable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tee on 15. 12. 8..
 */
public class MarkdownViewModel {

    SpannableStringBuilder messageStringBuilder;
    TextView tvMessageView;

    public MarkdownViewModel(TextView tvMessageView, SpannableStringBuilder messageStringBuilder) {
        this.messageStringBuilder = messageStringBuilder;
        this.tvMessageView = tvMessageView;
    }

    public void execute() {
        LogUtil.e(messageStringBuilder.toString());
        if (messageStringBuilder != null) {
            recursiveBuildMarkdown(messageStringBuilder, 0,
                    messageStringBuilder.length() - 1, new DrewMarkDownInfo());
        }
    }

    public void recursiveBuildMarkdown(SpannableStringBuilder messageStringBuilder,
                                       int startIndex, int endIndex, DrewMarkDownInfo drewMarkdownInfo) {
        // 전달된 인덱스로 부터 선별된 메세지의 내용
        String subMessage = null;
        // 마크다운 형태로 매칭된 스트링
        String matchingString = null;

        // 매칭된 메세지의 시작과 끝 문자 인덱스
        int matchingTotalStringStartIndex = -1;
        int matchingTotalStringEndIndex = -1;

        // 메칭된 메세지에서 마크다운 캐릭터를 제외한 핵심 스트링 시작/끝 인덱스
        int machingStringStartIndex = -1;
        int machingStringEndIndex = -1;
        int markdownCharacterSize = -1;

        subMessage = messageStringBuilder.subSequence(startIndex, endIndex + 1).toString();
        Pattern p = getPattern();
        Matcher matcher = p.matcher(subMessage);

        if (matcher.find()) {
            matcher.reset();
            while (matcher.find()) {
                Step step = getStep(matcher);
                matchingString = getMatchingString(step, matcher);
                markdownCharacterSize = getMarkdownCharacterLength(step);

                // 매칭된 문자열은 0부터 시작하므로 당연히 전달된 시작 인덱스와 더해줘야 함.
                machingStringStartIndex = startIndex + getStartIndexOfString(step, matcher);
                machingStringEndIndex = machingStringStartIndex + matchingString.length() - 1;

                matchingTotalStringStartIndex = machingStringStartIndex - markdownCharacterSize;
                matchingTotalStringEndIndex = machingStringEndIndex + markdownCharacterSize;

                drawMarkDown(messageStringBuilder,
                        matchingTotalStringStartIndex, matchingTotalStringEndIndex, step, drewMarkdownInfo);

                // 매칭된 것은 현재와 같은 단계의 재귀 호출을 한다.
                recursiveBuildMarkdown(messageStringBuilder,
                        machingStringStartIndex, machingStringEndIndex, updateDrewMarkDownInfo(step, drewMarkdownInfo));
            }
        } else {
            //매칭된 것이 더 이상 없으므로 종료
            return;
        }
    }

    private Pattern getPattern() {
        return Pattern.compile("(\\~{2})(.+)\\~{2}|(\\*{3})([^\\*]+)\\*{3}|(\\*{2})([^\\*]+)\\*{2}|(\\*)([^\\*]+)\\*");
    }

    public void drawMarkDown(SpannableStringBuilder messageStringBuilder, int startIndex, int endIndex, Step step, DrewMarkDownInfo drewMarkDownInfo) {
        LogUtil.e("메세지 :" + messageStringBuilder.toString() + startIndex + "부터" + endIndex + "까지");
        String message = null;
        MarkdownSpannable spannable = null;

        if (step == Step.BOLD_ITALIC) {
            LogUtil.e("볼드 이태릭으로 그린다.");
            message = messageStringBuilder.subSequence(startIndex + 3,
                    endIndex - 2).toString();
            spannable = new MarkdownSpannable(
                    message, tvMessageView.getTextSize());
            spannable.setIsBold(true);
            spannable.setIsItalic(true);
        } else if (step == Step.BOLD) {
            LogUtil.e("볼드로 그린다.");
            message = messageStringBuilder.subSequence(startIndex + 2,
                    endIndex - 1).toString();
            spannable = new MarkdownSpannable(
                    message, tvMessageView.getTextSize());
            spannable.setIsBold(true);
        } else if (step == Step.ITALIC) {
            LogUtil.e("이태릭으로 그린다.");
            message = messageStringBuilder.subSequence(startIndex + 1,
                    endIndex).toString();
            spannable = new MarkdownSpannable(
                    message, tvMessageView.getTextSize());
            spannable.setIsItalic(true);
        } else if (step == Step.STRIKE) {
            LogUtil.e("취소선 그린다.");
            message = messageStringBuilder.subSequence(startIndex + 2,
                    endIndex - 1).toString();
            spannable = new MarkdownSpannable(
                    message, tvMessageView.getTextSize());
            spannable.setIsStrike(true);
        }

        if (drewMarkDownInfo.isStrike()) {
            spannable.setIsStrike(true);
        }

        if (drewMarkDownInfo.isItalic()) {
            spannable.setIsItalic(true);
        }

        if (drewMarkDownInfo.isBold()) {
            spannable.setIsBold(true);
        }

        int dp = 260;
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dp, tvMessageView.getContext().getResources().getDisplayMetrics());
        spannable.setViewMaxWidthSize((int) px);
        messageStringBuilder.setSpan(spannable, startIndex, endIndex + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    DrewMarkDownInfo updateDrewMarkDownInfo(Step step, DrewMarkDownInfo drewMarkDownInfo) {
        DrewMarkDownInfo newDrewMarkdownInfo = new DrewMarkDownInfo();

        if (step == Step.BOLD_ITALIC) {
            newDrewMarkdownInfo.setItalic(true);
            newDrewMarkdownInfo.setBold(true);
        } else if (step == Step.ITALIC) {
            newDrewMarkdownInfo.setItalic(true);
        } else if (step == Step.BOLD) {
            newDrewMarkdownInfo.setBold(true);
        } else if (step == Step.STRIKE) {
            newDrewMarkdownInfo.setStrike(true);
        }

        if (drewMarkDownInfo.isEmpty()) {
            return newDrewMarkdownInfo;
        } else {
            newDrewMarkdownInfo.setEmpty(false);
        }

        if (drewMarkDownInfo.isItalic()) {
            newDrewMarkdownInfo.setItalic(true);
        }

        if (drewMarkDownInfo.isBold()) {
            newDrewMarkdownInfo.setBold(true);
        }

        if (drewMarkDownInfo.isStrike()) {
            newDrewMarkdownInfo.setStrike(true);
        }

        return newDrewMarkdownInfo;
    }

    public Step getStep(Matcher matcher) {
        if (!TextUtils.isEmpty(matcher.group(1))) {
            return Step.STRIKE;
        } else if (!TextUtils.isEmpty(matcher.group(3))) {
            return Step.BOLD_ITALIC;
        } else if (!TextUtils.isEmpty((matcher.group(5)))) {
            return Step.BOLD;
        } else {
            return Step.ITALIC;
        }
    }

    private int getMarkdownCharacterLength(Step step) {
        if (step == Step.BOLD_ITALIC) {
            return 3;
        } else if (step == Step.ITALIC) {
            return 1;
        } else if (step == Step.BOLD) {
            return 2;
        } else {
            return 2;
        }
    }

    public String getMatchingString(Step step, Matcher matcher) {
        if (step == Step.STRIKE) {
            return matcher.group(2);
        } else if (step == Step.BOLD_ITALIC) {
            return matcher.group(4);
        } else if (step == Step.BOLD) {
            return matcher.group(6);
        } else {
            return matcher.group(8);
        }
    }

    public int getStartIndexOfString(Step step, Matcher matcher) {
        if (step == Step.STRIKE) {
            return matcher.start(2);
        } else if (step == Step.BOLD_ITALIC) {
            return matcher.start(4);
        } else if (step == Step.BOLD) {
            return matcher.start(6);
        } else {
            return matcher.start(8);
        }
    }

    public static enum Step {
        BOLD_ITALIC, ITALIC, BOLD, STRIKE;
    }

    public class DrewMarkDownInfo {
        private boolean bold = false;
        private boolean italic = false;
        private boolean strike = false;
        private boolean empty = true;

        public boolean isBold() {
            return bold;
        }

        public void setBold(boolean bold) {
            this.bold = bold;
        }

        public boolean isItalic() {
            return italic;
        }

        public void setItalic(boolean italic) {
            this.italic = italic;
        }

        public boolean isStrike() {
            return strike;
        }

        public void setStrike(boolean strike) {
            this.strike = strike;
        }

        public boolean isEmpty() {
            return empty;
        }

        public void setEmpty(boolean empty) {
            this.empty = empty;
        }
    }

}
package com.tosslab.jandi.app.ui.commonviewmodels.markdown.viewmodel;

import android.support.annotation.NonNull;
import android.text.SpannableStringBuilder;

import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tee on 15. 12. 8..
 */
public class MarkdownViewModel {

    public void executeBuildMarkdown(SpannableStringBuilder messageStringBuilder) {
        LogUtil.e(messageStringBuilder.toString());
        recursiveBuildMarkdown(messageStringBuilder, 0,
                messageStringBuilder.length() - 1, Step.BOLD_ITALIC, new DrewMarkDownInfo());
    }

    public void recursiveBuildMarkdown(SpannableStringBuilder messageStringBuilder,
                                       int startIndex, int endIndex, Step step, DrewMarkDownInfo drewMarkdownInfo) {
        //재귀 탈출 조건 - step이 Finish일 경우
        if (step == Step.FINISH) {
            return;
        }

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

        // 마크다운 캐릭터 사이즈 ( EX. *** = 3 ** = 2 * = 1 ~~ = 2 )
        int markdownCharacterSize = getMarkdownCharacterLength(step);

        subMessage = messageStringBuilder.subSequence(startIndex, endIndex + 1).toString();
        Pattern p = getPattern(step);
        Matcher matcher = p.matcher(subMessage);

        int startPoint = startIndex;

        if (matcher.find()) {
            matcher.reset();
            while (matcher.find()) {
                matchingString = matcher.group(3);

                // 매칭된 문자열은 0부터 시작하므로 당연히 전달된 시작 인덱스와 더해줘야 함.
                machingStringStartIndex = startIndex + matcher.start(3);
                machingStringEndIndex = machingStringStartIndex + matchingString.length() - 1;

                matchingTotalStringStartIndex = machingStringStartIndex - markdownCharacterSize;
                matchingTotalStringEndIndex = machingStringEndIndex + markdownCharacterSize;

                drawMarkDown(messageStringBuilder,
                        matchingTotalStringStartIndex, matchingTotalStringEndIndex, step, drewMarkdownInfo);

                // 매칭된 것은 현재와 같은 단계의 재귀 호출을 한다.
                recursiveBuildMarkdown(messageStringBuilder,
                        machingStringStartIndex, machingStringEndIndex,
                        step, updateDrewMarkDownInfo(step, drewMarkdownInfo));

                // 매칭되지 않는 범주의 메세지 조각들을 다음 스텝으로
                if (startPoint != matchingTotalStringStartIndex) {
                    recursiveBuildMarkdown(messageStringBuilder,
                            startPoint, matchingTotalStringStartIndex - 1, getNextStep(step), drewMarkdownInfo);
                    startPoint = matchingTotalStringEndIndex + 1;
                } else {
                    startPoint = matchingTotalStringEndIndex + 1;
                }
            }
            // 마지막 매칭되지 않는 범주의 메세지 조각을 다음 스텝으로
            if (startPoint != endIndex) {
                recursiveBuildMarkdown(messageStringBuilder, startPoint, endIndex, getNextStep(step), drewMarkdownInfo);
            }

        } else {
            // 매칭된 것이 없으므로 2번째 스텝으로
            recursiveBuildMarkdown(messageStringBuilder, startIndex, endIndex, getNextStep(step), drewMarkdownInfo);
            return;
        }
    }

    private int getMarkdownCharacterLength(Step step) {
        if (step == Step.BOLD_ITALIC) {
            return 3;
        } else if (step == Step.ITALIC) {
            return 2;
        } else if (step == Step.BOLD) {
            return 1;
        } else {
            return 2;
        }
    }

    @NonNull
    private Pattern getPattern(Step step) {
        if (step == Step.BOLD_ITALIC) {
            return Pattern.compile("((\\*{3})(.+)(\\*{3}))");
        } else if (step == Step.ITALIC) {
            return Pattern.compile("((\\*{2})(.+)(\\*{2}))");
        } else if (step == Step.BOLD) {
            return Pattern.compile("((\\*)(.+)(\\*))");
        } else {
            return Pattern.compile("((\\~{2})(.+)(\\~{2}))");
        }
    }

    public void drawMarkDown(SpannableStringBuilder messageStringBuilder, int startIndex, int endIndex, Step step, DrewMarkDownInfo drewMarkDownInfo) {
        LogUtil.e("메세지 :" + messageStringBuilder + startIndex + "부터" + endIndex + "까지");
        if (drewMarkDownInfo.isItalic() && drewMarkDownInfo.isBold()) {
            // 기존에 BOLD / ITALIC일 경우 BOLD_ITALIC / BOLD / ITALIC 모두 다시 DRAWING할 필요가 없다.
            if (step == Step.STRIKE) {
                // 그린다.
                LogUtil.e("취소선 그린다.");
            }
        } else if (drewMarkDownInfo.isItalic()) {
            if (step == Step.BOLD || step == Step.BOLD_ITALIC) {
                LogUtil.e("볼드 이태릭으로 그린다.");
            }
            if (step == Step.STRIKE) {
                // 그린다.
                LogUtil.e("취소선 그린다.");
            }
        } else if (drewMarkDownInfo.isBold()) {
            if (step == Step.ITALIC || step == Step.BOLD_ITALIC) {
                LogUtil.e("볼드 이태릭으로 그린다.");
            }
            if (step == Step.STRIKE) {
                // 그린다.
                LogUtil.e("취소선 그린다.");
            }
        } else {
            if (step == Step.BOLD_ITALIC) {
                LogUtil.e("볼드 이태릭으로 그린다.");
            } else if (step == Step.BOLD) {
                LogUtil.e("볼드로 그린다.");
            } else if (step == Step.ITALIC) {
                LogUtil.e("이태릭으로 그린다.");
            }
            if (step == Step.STRIKE) {
                // 그린다.
                LogUtil.e("취소선 그린다.");
            }
        }
    }

    public Step getNextStep(Step step) {
        if (step == Step.BOLD_ITALIC) {
            return Step.ITALIC;
        } else if (step == Step.ITALIC) {
            return Step.BOLD;
        } else if (step == Step.BOLD) {
            return Step.STRIKE;
        } else {
            return Step.FINISH;
        }
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

    public static enum Step {
        BOLD_ITALIC, ITALIC, BOLD, STRIKE, FINISH;
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
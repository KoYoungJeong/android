package com.tosslab.jandi.app.utils;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.widget.TextView;

import com.tosslab.jandi.app.ui.commonviewmodels.markdown.vo.MarkdownVO;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tee on 15. 12. 7..
 */
public class GenerateMarkdownMessageUtil {

    Context context;
    TextView tvMessageView;
    SpannableStringBuilder stringBuilder;


    public GenerateMarkdownMessageUtil(TextView tvMessageView,
                                       SpannableStringBuilder stringBuilder) {
        context = tvMessageView.getContext();
        this.tvMessageView = tvMessageView;
        this.stringBuilder = stringBuilder;
    }

    public void generate() {

    }

    public List<MarkdownVO> generateMarkdownModels(String message) {
        if (TextUtils.isEmpty(message)) {
            return null;
        }

        Pattern p = Pattern.compile("(>|^|\\s)([\\*_~]{1,3})([^\\2].*?)\\2(<\\/|$|\\s)");
        Matcher matcher = p.matcher(message);
        String matchingType = null;
        String matchingString = null;
        int startIndex = -1;
        int endIndex = -1;
        List<MarkdownVO> markdowns = new ArrayList();

        while (matcher.find()) {
            matchingType = matcher.group(2);
            matchingString = matcher.group(3);
            // matching type으로 시작 인덱스 획득
            startIndex = matcher.start(2);
            // 마지막 인덱스 계산
            endIndex = startIndex + matchingType.length() * 2 + matchingString.length();

            MarkdownVO markdown = new MarkdownVO();
            markdown.setStartIndex(startIndex);
            markdown.setEndIndex(endIndex);
            markdown.setMarkdownString(matchingString);

            if (TextUtils.equals(matchingType, "*")) {
                // 기울임으로 변경
                markdown.setType(MarkdownVO.TYPE.ITALIC);
            } else if (TextUtils.equals(matchingType, "**")) {
                // 볼드체로 변경
                markdown.setType(MarkdownVO.TYPE.BOLD);
            } else if (TextUtils.equals(matchingType, "***")) {
                // 기울임/볼드체로 변경
                markdown.setType(MarkdownVO.TYPE.ITALICBOLD);
            }

//            else if (TextUtils.equals(matchingType, "***")) {
//                // 기울임/볼드체로 변경
//                markdown.setType(MarkdownVO.TYPE.STRIKE);
//            }

            markdowns.add(markdown);
        }

        return markdowns;
    }

}

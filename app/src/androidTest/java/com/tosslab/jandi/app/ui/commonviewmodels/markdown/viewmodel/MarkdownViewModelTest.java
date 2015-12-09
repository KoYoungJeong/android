package com.tosslab.jandi.app.ui.commonviewmodels.markdown.viewmodel;

import android.text.SpannableStringBuilder;

import org.junit.Test;

/**
 * Created by tee on 15. 12. 8..
 */
public class MarkdownViewModelTest {
    @Test
    public void testRecursiveBuildMarkdown() throws Exception {
        MarkdownViewModel markdownViewModel = new MarkdownViewModel();
        SpannableStringBuilder messageStringBuilder = new SpannableStringBuilder();
        messageStringBuilder.append("**안녕** ~~이러지마~~ *반갑습니다* ***안녕하세요***");
        markdownViewModel.executeBuildMarkdown(messageStringBuilder);
    }
}
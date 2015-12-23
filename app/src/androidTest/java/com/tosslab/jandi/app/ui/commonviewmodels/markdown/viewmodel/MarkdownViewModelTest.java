package com.tosslab.jandi.app.ui.commonviewmodels.markdown.viewmodel;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.widget.TextView;

import org.junit.Test;
import org.mockito.Mockito;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Created by tee on 15. 12. 8..
 */
public class MarkdownViewModelTest {
//    @Test
//    public void testRecursiveBuildMarkdown() throws Exception {
//
//        TextView tv = Mockito.mock(TextView.class);
//        SpannableStringBuilder messageStringBuilder = new SpannableStringBuilder();
////        messageStringBuilder.append("~~안녕 *이러지마* **반갑습니다** ***반가워***하세요~~ **안녕 ~~하이욥~ **");
//        messageStringBuilder.append("*하하* **하하하** ***하하하하***");
//        MarkdownViewModel markdownViewModel = new MarkdownViewModel(tv, messageStringBuilder);
//        markdownViewModel.execute();
//    }

    @Test
    public void testRemoveOrChangeNestedSpan() {
        // Given
        TextView tv = Mockito.mock(TextView.class);
        SpannableStringBuilder messageStringBuilder = new SpannableStringBuilder();

        messageStringBuilder.append("hahaha");
        messageStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0,
                messageStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        MarkdownViewModel markdownViewModel = new MarkdownViewModel(tv, messageStringBuilder, false);
        Object[] span = messageStringBuilder.getSpans(0, messageStringBuilder.length(), Object.class);

        // When
        markdownViewModel.removeOrChangeNestedSpan(messageStringBuilder, span[0], MarkdownViewModel.Step.ITALIC);

        // Then
        Object[] objectSpans = messageStringBuilder.getSpans(0, messageStringBuilder.length(), Object.class);

        assertThat(objectSpans.length, is(1));

        assertThat("", objectSpans[0] instanceof StyleSpan);
        StyleSpan styleSpan = (StyleSpan) objectSpans[0];
        assertThat(styleSpan.getStyle(), is(Typeface.BOLD_ITALIC));

        ////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Given
        messageStringBuilder = new SpannableStringBuilder();
        messageStringBuilder.append("hahaha");
        messageStringBuilder.setSpan(new StyleSpan(Typeface.ITALIC), 2,
                messageStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        span = messageStringBuilder.getSpans(0, messageStringBuilder.length(), Object.class);
        messageStringBuilder.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), 0,
                messageStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        markdownViewModel = new MarkdownViewModel(tv, messageStringBuilder, false);

        // When
        markdownViewModel.removeOrChangeNestedSpan(messageStringBuilder, span[0], MarkdownViewModel.Step.BOLD_ITALIC);

        // Then
        objectSpans = messageStringBuilder.getSpans(0, messageStringBuilder.length(), Object.class);

        assertThat(objectSpans.length, is(1));

        assertThat("", objectSpans[0] instanceof StyleSpan);
        styleSpan = (StyleSpan) objectSpans[0];
        assertThat(styleSpan.getStyle(), is(Typeface.BOLD_ITALIC));

        ////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Given
        messageStringBuilder = new SpannableStringBuilder();
        messageStringBuilder.append("hahaha");
        messageStringBuilder.setSpan(new StyleSpan(Typeface.ITALIC), 0,
                messageStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        markdownViewModel = new MarkdownViewModel(tv, messageStringBuilder, false);
        span = messageStringBuilder.getSpans(0, messageStringBuilder.length(), Object.class);

        // When
        markdownViewModel.removeOrChangeNestedSpan(messageStringBuilder, span[0], MarkdownViewModel.Step.BOLD_ITALIC);

        // Then
        objectSpans = messageStringBuilder.getSpans(0, messageStringBuilder.length(), Object.class);

        assertThat(objectSpans.length, is(0));

        ////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Given
        messageStringBuilder = new SpannableStringBuilder();
        messageStringBuilder.append("hahaha");
        messageStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0,
                messageStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        markdownViewModel = new MarkdownViewModel(tv, messageStringBuilder, false);
        span = messageStringBuilder.getSpans(0, messageStringBuilder.length(), Object.class);

        // When
        markdownViewModel.removeOrChangeNestedSpan(messageStringBuilder, span[0], MarkdownViewModel.Step.BOLD_ITALIC);

        // Then
        objectSpans = messageStringBuilder.getSpans(0, messageStringBuilder.length(), Object.class);

        assertThat(objectSpans.length, is(0));

        ////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Given
        messageStringBuilder = new SpannableStringBuilder();
        messageStringBuilder.append("hahaha");
        messageStringBuilder.setSpan(new StyleSpan(Typeface.ITALIC), 0,
                4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        messageStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0,
                6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        markdownViewModel = new MarkdownViewModel(tv, messageStringBuilder, false);
        span = messageStringBuilder.getSpans(0, messageStringBuilder.length(), Object.class);

        // When
        for (int i = 0; i < 2; i++) {
            markdownViewModel.removeOrChangeNestedSpan(messageStringBuilder, span[i], MarkdownViewModel.Step.BOLD);
        }

        // Then
        objectSpans = messageStringBuilder.getSpans(0, messageStringBuilder.length(), Object.class);
        assertThat(objectSpans.length, is(2));

        StyleSpan o1 = (StyleSpan) objectSpans[0];
        assertThat(o1.getStyle(), is(Typeface.BOLD));
        assertThat(messageStringBuilder.getSpanStart(objectSpans[0]), is(0));
        assertThat(messageStringBuilder.getSpanEnd(objectSpans[0]), is(6));

        StyleSpan o2 = (StyleSpan) objectSpans[1];
        assertThat(o2.getStyle(), is(Typeface.BOLD_ITALIC));
        assertThat(messageStringBuilder.getSpanStart(objectSpans[1]), is(0));
        assertThat(messageStringBuilder.getSpanEnd(objectSpans[1]), is(4));
    }

    @Test
    public void testSetMarkdown() {
        // Given
        TextView tv = Mockito.mock(TextView.class);
        SpannableStringBuilder messageStringBuilder = new SpannableStringBuilder();

        messageStringBuilder.append("hahaha");

        MarkdownViewModel markdownViewModel = new MarkdownViewModel(tv, messageStringBuilder, false);
        // When
        markdownViewModel.setMarkdown(messageStringBuilder, 0, messageStringBuilder.length(), MarkdownViewModel.Step.BOLD_ITALIC);

        // Then
        Object[] objectSpans = messageStringBuilder.getSpans(0, messageStringBuilder.length(), Object.class);

        assertThat(objectSpans.length, is(1));
        assertThat("", objectSpans[0] instanceof StyleSpan);
        StyleSpan styleSpan = (StyleSpan) objectSpans[0];

        assertThat(styleSpan.getStyle(), is(Typeface.BOLD_ITALIC));
        assertThat(messageStringBuilder.getSpanStart(objectSpans[0]), is(0));
        assertThat(messageStringBuilder.getSpanEnd(objectSpans[0]), is(messageStringBuilder.length()));
    }

    @Test
    public void testConvertPlainTextFromPlainMarkdown() {
        //Given
        TextView tv = Mockito.mock(TextView.class);
        SpannableStringBuilder messageStringBuilder = new SpannableStringBuilder();
        messageStringBuilder.append("***hahaha***");

        //When
        MarkdownViewModel markdownViewModel = new MarkdownViewModel(tv, messageStringBuilder, false);
        int startIndex = 0;
        int lastIndex = messageStringBuilder.length();
        markdownViewModel.convertPlainTextFromPlainMarkdown(messageStringBuilder, startIndex, lastIndex, MarkdownViewModel.Step.BOLD_ITALIC);

        //Then
        assertThat(messageStringBuilder.toString(), is("hahaha"));
    }
}
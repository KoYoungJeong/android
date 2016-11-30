package com.tosslab.jandi.app.ui.maintab.tabs.team.filter.dept.model;

import android.text.SpannableStringBuilder;
import android.util.Pair;

import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.dept.domain.DeptJob;
import com.tosslab.jandi.app.views.spannable.HighlightSpannable;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.functions.Func0;
import rx.observers.TestSubscriber;

import static org.assertj.core.api.Assertions.assertThat;

@org.junit.runner.RunWith(android.support.test.runner.AndroidJUnit4.class)
public class DeptJobModelTest {
    private DeptJobModel deptJobModel;

    @Before
    public void setUp() throws Exception {
        deptJobModel = new DeptJobModel();
    }

    @Test
    public void textToSpan_has_span() throws Exception {
        TestSubscriber<DeptJob> subscriber = TestSubscriber.create();
        Observable.just("abc", "bac", "cba", "aaa")
                .map(it -> Pair.create(it, 1))
                .compose(deptJobModel.textToSpan("a"))
                .subscribe(subscriber);

        subscriber.awaitTerminalEvent();

        subscriber.assertValueCount(4);
        List<DeptJob> onNextEvents = subscriber.getOnNextEvents();
        assertThat(onNextEvents).hasSize(4)
                .extracting(it -> it.getName().toString())
                .contains("abc", "bac", "cba", "aaa");

        for (DeptJob charSequence : onNextEvents) {
            HighlightSpannable[] spans = new SpannableStringBuilder(charSequence.getName())
                    .getSpans(0, charSequence.getName().length(), HighlightSpannable.class);

            assertThat(spans.length).as("%s", charSequence.toString()).isGreaterThanOrEqualTo(1);
        }


    }

    @Test
    public void textToSpan_has_nothing() throws Exception {
        TestSubscriber<DeptJob> subscriber = TestSubscriber.create();
        Observable.just("abc", "bac", "cba", "aaa")
                .map(it -> Pair.create(it, 1))
                .compose(deptJobModel.textToSpan("1"))
                .subscribe(subscriber);

        subscriber.awaitTerminalEvent();

        List<DeptJob> onNextEvents = subscriber.getOnNextEvents();
        assertThat(onNextEvents).hasSize(4)
                .extracting(DeptJob::getName)
                .contains("abc", "bac", "cba", "aaa");

        for (DeptJob charSequence : onNextEvents) {
            HighlightSpannable[] spans = new SpannableStringBuilder(charSequence.getName())
                    .getSpans(0, charSequence.getName().length(), HighlightSpannable.class);

            assertThat(spans.length).as("%s", charSequence.toString()).isEqualTo(0);
        }


    }

    @Test
    public void dataMap() throws Exception {
        TestSubscriber<List<Pair<CharSequence, String>>> subscriber = TestSubscriber.create();

        Observable.just("aaa", "bbb", "가", "나")
                .collect((Func0<ArrayList<CharSequence>>) ArrayList::new, List::add)
                .compose(deptJobModel.dataMap())
                .subscribe(subscriber);

        subscriber.awaitTerminalEvent();

        List<Pair<CharSequence, String>> pairs = subscriber.getOnNextEvents().get(0);

        assertThat(pairs).hasSize(4)
                .extracting(pair -> pair.second)
                .containsSubsequence("A", "B", "ㄱ", "ㄴ");

    }

    @Test
    public void containFilter() throws Exception {
        TestSubscriber<String> subscriber = TestSubscriber.create();
        Observable.just("aaa","abbb","bcd")
                .compose(deptJobModel.containFilter("b"))
                .subscribe(subscriber);

        subscriber.awaitTerminalEvent();

        subscriber.assertValueCount(2);
        subscriber.assertValues("abbb","bcd");
    }

}
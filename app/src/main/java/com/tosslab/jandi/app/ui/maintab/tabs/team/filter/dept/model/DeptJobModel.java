package com.tosslab.jandi.app.ui.maintab.tabs.team.filter.dept.model;


import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Pair;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.utils.FirstCharacterUtil;
import com.tosslab.jandi.app.views.spannable.HighlightSpannable;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func1;

public class DeptJobModel {

    private HighlightSpannable highlightSpan;

    @Inject
    public DeptJobModel() {
        int highlighteColor = JandiApplication.getContext().getResources().getColor(R.color.rgb_00abe8);
        highlightSpan = new HighlightSpannable(Color.TRANSPARENT, highlighteColor);
    }

    public Observable.Transformer<? super List<String>, ? extends List<CharSequence>> textToSpan(final String it) {
        return observable -> observable.concatMap(new Func1<List<String>, Observable<? extends List<CharSequence>>>() {
            @Override
            public Observable<? extends ArrayList<CharSequence>> call(List<String> its) {
                if (TextUtils.isEmpty(it)) {
                    return Observable.just(new ArrayList<CharSequence>(its));
                } else {
                    return Observable.from(its)
                            .map(text -> {

                                int index = text.toLowerCase().indexOf(it.toLowerCase());
                                if (index >= 0) {
                                    SpannableStringBuilder builder = new SpannableStringBuilder(text);
                                    builder.setSpan(highlightSpan, index, index + it.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    return builder;
                                } else {
                                    return text;
                                }
                            })
                            .collect(ArrayList::new, List::add);
                }
            }
        });
    }

    public Observable.Transformer<? super List<CharSequence>, ? extends List<Pair<CharSequence, String>>> dataMap() {
        return ob -> ob.map(its -> {
            List<Pair<CharSequence, String>> list = new ArrayList<>();
            for (CharSequence it : its) {
                list.add(Pair.create(it, FirstCharacterUtil.firstCharacter(it.toString())));
            }
            return list;
        });
    }

    public Observable.Transformer<String, String> containFilter(String it) {
        return stringObservable -> stringObservable.filter(division -> {
            if (TextUtils.isEmpty(it)) {
                return true;
            } else {
                return division.toLowerCase().contains(it);
            }
        });
    }
}

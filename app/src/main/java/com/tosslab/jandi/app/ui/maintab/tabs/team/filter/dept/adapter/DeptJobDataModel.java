package com.tosslab.jandi.app.ui.maintab.tabs.team.filter.dept.adapter;


import android.util.Pair;

import java.util.List;

public interface DeptJobDataModel {
    Pair<CharSequence, String> getItem(int position);

    int getSize();

    void add(Pair<CharSequence, String> item);

    void addAll(List<Pair<CharSequence, String>> items);

    void clear();
}

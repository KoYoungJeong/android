package com.tosslab.jandi.app.ui.maintab.team.filter.dept.adapter;


import android.util.Pair;

import java.util.List;

public interface DeptJobDataModel {
    Pair<String, String> getItem(int position);
    void setKeyword(String keyword);

    int getSize();

    void add(Pair<String, String> item);

    void addAll(List<Pair<String, String>> items);

    void clear();
}

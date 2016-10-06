package com.tosslab.jandi.app.ui.maintab.tabs.team.filter.dept.domain;


public class DeptJob {
    private final CharSequence name;
    private final String header;
    private final int count;

    private DeptJob(CharSequence name, String header, int count) {
        this.name = name;
        this.header = header;
        this.count = count;
    }

    public static DeptJob create(CharSequence name, String header, int count) {
        return new DeptJob(name, header, count);
    }

    public CharSequence getName() {
        return name;
    }

    public String getHeader() {
        return header;
    }

    public int getCount() {
        return count;
    }


}

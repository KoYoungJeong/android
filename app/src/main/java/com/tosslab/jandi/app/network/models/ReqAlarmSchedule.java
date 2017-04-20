package com.tosslab.jandi.app.network.models;

import java.util.List;

/**
 * Created by tee on 2017. 4. 18..
 */

public class ReqAlarmSchedule {

    private List<Integer> days;
    private int start_time;
    private int end_time;
    private int timezone;

    public ReqAlarmSchedule(List<Integer> days, int start_time, int end_time, int timezone) {
        this.days = days;
        this.start_time = start_time;
        this.end_time = end_time;
        this.timezone = timezone;
    }

    public List<Integer> getDays() {
        return days;
    }

    public void setDays(List<Integer> days) {
        this.days = days;
    }

    public int getStart_time() {
        return start_time;
    }

    public void setStart_time(int start_time) {
        this.start_time = start_time;
    }

    public int getEnd_time() {
        return end_time;
    }

    public void setEnd_time(int end_time) {
        this.end_time = end_time;
    }

    public int getTimezone() {
        return timezone;
    }

    public void setTimezone(int timezone) {
        this.timezone = timezone;
    }
}

package com.tosslab.jandi.lib.sprinkler;

import android.content.Context;

import com.tosslab.jandi.lib.sprinkler.domain.Event;
import com.tosslab.jandi.lib.sprinkler.domain.property.Property;
import com.tosslab.jandi.lib.sprinkler.tracker.Tracker;

/**
 * Created by Steve SeongUg Jung on 15. 7. 2..
 */
public class Sprinkler {
    private static Sprinkler sInstance;

    private Context context;
    private Sprinkler(Context context) {
        this.context = context;
    }

    public static Sprinkler with(Context context) {
        if (sInstance == null) {
            sInstance = new Sprinkler(context.getApplicationContext());
        }
        return sInstance;
    }

    private Event event;
    private Property property;

    public Sprinkler event(Event event) {
        this.event = event;
        return this;
    }

    public Sprinkler property(Property property) {
        this.property = property;
        return this;
    }

    public Sprinkler input() {
        return this;
    }

}

package com.tosslab.jandi.lib.sprinkler;

import android.content.Context;

import com.tosslab.jandi.lib.sprinkler.tracker.Tracker;

/**
 * Created by Steve SeongUg Jung on 15. 7. 2..
 */
public class Sprinkler {

    private static Sprinkler sprinkler;
    private Context context;
    private Tracker tracker;

    private Sprinkler(Context context) {
        this.context = context;
        tracker = new Tracker(context);
    }

    public static Sprinkler getInstance(Context context) {
        if (sprinkler == null) {
            sprinkler = new Sprinkler(context);
        }

        return sprinkler;
    }

    public Tracker getTracker() {
        return tracker;
    }
}

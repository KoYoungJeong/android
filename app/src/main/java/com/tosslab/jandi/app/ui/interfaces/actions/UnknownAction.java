package com.tosslab.jandi.app.ui.interfaces.actions;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;

import com.tosslab.jandi.app.ui.intro.IntroActivity_;

/**
 * Created by Steve SeongUg Jung on 14. 12. 28..
 */
class UnknownAction implements Action {

    private final Context context;

    private UnknownAction(Context context) {
        this.context = context;
    }

    static UnknownAction create(Context context) {
        return new UnknownAction(context);
    }

    @Override
    public void execute(Uri uri) {
        IntroActivity_
                .intent(context)
                .start();
        ((Activity) context).finish();

    }
}
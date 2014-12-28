package com.tosslab.jandi.app.ui.interfaces.actions;

import android.content.Context;
import android.text.TextUtils;

/**
 * Created by Steve SeongUg Jung on 14. 12. 28..
 */
public class ActionFactory {

    public static Action getAction(Context context, String path) {

        if (isUnkownAction(path)) {
            return UnknownAction.create(context);
        }

        if (TextUtils.equals(path.toLowerCase(), "open")) {
            return OpenAction_.getInstance_(context);
        }

        return UnknownAction.create(context);
    }

    private static boolean isUnkownAction(String path) {
        return TextUtils.isEmpty(path);
    }


}

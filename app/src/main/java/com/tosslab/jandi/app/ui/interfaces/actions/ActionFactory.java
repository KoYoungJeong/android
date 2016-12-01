package com.tosslab.jandi.app.ui.interfaces.actions;

import android.app.Activity;
import android.net.Uri;
import android.text.TextUtils;

/**
 * Created by Steve SeongUg Jung on 14. 12. 28..
 */
public class ActionFactory {

    public static Action getAction(Activity activity, Uri data) {

        if (data == null) {
            return UnknownAction.create(activity);
        }

        if (isUnkownAction(data.getAuthority())) {
            return UnknownAction.create(activity);
        }

        if (TextUtils.equals(data.getAuthority().toLowerCase(), "open")) {
            return new OpenAction(activity);
        }

        return UnknownAction.create(activity);
    }

    private static boolean isUnkownAction(String path) {
        return TextUtils.isEmpty(path);
    }


}

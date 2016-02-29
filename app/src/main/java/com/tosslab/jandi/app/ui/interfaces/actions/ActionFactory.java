package com.tosslab.jandi.app.ui.interfaces.actions;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

/**
 * Created by Steve SeongUg Jung on 14. 12. 28..
 */
public class ActionFactory {

    public static Action getAction(Context context, Uri data) {

        if (data == null) {
            return UnknownAction.create(context);
        }

        if (isUnkownAction(data.getAuthority())) {
            return UnknownAction.create(context);
        }

        if (TextUtils.equals(data.getAuthority().toLowerCase(), "open")) {
            return OpenAction_.getInstance_(context);
        }

        return UnknownAction.create(context);
    }

    private static boolean isUnkownAction(String path) {
        return TextUtils.isEmpty(path);
    }


}

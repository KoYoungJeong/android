package com.tosslab.jandi.app.utils.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.utils.JandiPreference;

/**
 * Created by Steve SeongUg Jung on 15. 7. 24..
 */
public class NetworkCheckUtil {

    public static boolean isConnected() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) JandiApplication.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static boolean isConnectedDependsOnPreferences() {
        int lastNetworkConnect = JandiPreference.getLastNetworkConnect(JandiApplication.getContext());
        return lastNetworkConnect == 1;
    }
}

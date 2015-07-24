package com.tosslab.jandi.lib.sprinkler.io;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Pair;

import com.tosslab.jandi.lib.sprinkler.Logger;
import com.tosslab.jandi.lib.sprinkler.io.model.Track;

import java.util.List;

import retrofit.RetrofitError;

/**
 * Created by tonyjs on 15. 7. 23..
 */
final class Flusher {
    public static final String TAG = Logger.makeTag(Flusher.class);

    private TrackDatabaseHelper databaseHelper;
    private RequestManager requestManager;

    public Flusher(Context context) {
        databaseHelper = TrackDatabaseHelper.getInstance(context);
        requestManager = new RequestManager();
    }

    public boolean availableNetworking(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean wifiAvailable = wifiInfo != null && wifiInfo.isAvailable() && wifiInfo.isConnected();

        NetworkInfo mobileINfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        boolean mobileAvailable =
                mobileINfo != null && mobileINfo.isAvailable() && mobileINfo.isConnected();

        boolean availableNetworking = wifiAvailable || mobileAvailable;
        if (availableNetworking) {
            String log = wifiAvailable
                    ? "Available networking with Wifi." : "Available networking with Mobile.";
            Logger.i(TAG, log);
            return true;
        } else {
            Logger.e(TAG, "Unavailable Networking.");
            return false;
        }
    }

    public Pair<Integer, List<Track>> query() {
        return databaseHelper.query();
    }

    public boolean needToFlush(Pair<Integer, List<Track>> query) {
        int count = query.first;
        List<Track> data = query.second;

        return count > 0 && (data != null && !data.isEmpty());
    }

    public ResponseBody flush(int num, String deviceId, long lastDate, List<Track> data)
            throws RetrofitError {
        RequestManager.Request<ResponseBody> request = getRequest(num, deviceId, lastDate, data);
        return requestManager.request(request);
    }

    public void deleteRows(int startIndex, int endIndex) {
        databaseHelper.deleteRows(startIndex, endIndex);
    }

    private RequestManager.Request<ResponseBody> getRequest(
            final int num, final String deviceId, final long lastDate, final List<Track> data) {

        final RequestClient client = requestManager.getClient(RequestClient.class);
        return new RequestManager.Request<ResponseBody>() {
            @Override
            public ResponseBody performRequest() throws RetrofitError {
                RequestBody body = new RequestBody(num, deviceId, lastDate, data);
                ResponseBody response = client.post(body);
                return response;
            }
        };
    }

    public void stopRequest() {
        requestManager.stop();
    }
}

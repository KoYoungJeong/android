package com.tosslab.jandi.lib.sprinkler.io.flush;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Pair;

import com.tosslab.jandi.lib.sprinkler.io.database.SprinklerDatabaseHelper;
import com.tosslab.jandi.lib.sprinkler.io.domain.flush.RequestBody;
import com.tosslab.jandi.lib.sprinkler.io.domain.flush.ResponseBody;
import com.tosslab.jandi.lib.sprinkler.util.Logger;
import com.tosslab.jandi.lib.sprinkler.io.domain.track.Track;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Created by tonyjs on 15. 7. 23..
 */
public final class Flusher {
    public static final String TAG = Logger.makeTag(Flusher.class);

    private SprinklerDatabaseHelper databaseHelper;
    private RequestManager requestManager;

    public Flusher(Context context) {
        databaseHelper = SprinklerDatabaseHelper.getInstance(context);
        requestManager = RequestManager.get();
    }

    public Pair<Integer, List<Track>> query() {
        List<Track> list = new ArrayList<>();

        Cursor cursor = databaseHelper.query();
        int count = cursor.getCount();
        while (cursor.moveToNext()) {
            int index =
                    cursor.getInt(cursor.getColumnIndex(SprinklerDatabaseHelper.TableColumns._ID));
            String event =
                    cursor.getString(cursor.getColumnIndex(SprinklerDatabaseHelper.TableColumns.EVENT));
            String identifiers =
                    cursor.getString(cursor.getColumnIndex(SprinklerDatabaseHelper.TableColumns.IDENTIFIERS));
            String platform =
                    cursor.getString(cursor.getColumnIndex(SprinklerDatabaseHelper.TableColumns.PLATFORM));
            String properties =
                    cursor.getString(cursor.getColumnIndex(SprinklerDatabaseHelper.TableColumns.PROPERTIES));
            long time =
                    cursor.getLong(cursor.getColumnIndex(SprinklerDatabaseHelper.TableColumns.TIME));
            String version =
                    cursor.getString(cursor.getColumnIndex(SprinklerDatabaseHelper.TableColumns.VERSION));

            list.add(new Track(index,
                    event, getMapFromString(identifiers), platform, getMapFromString(properties), time, version));
        }
        cursor.close();

        return new Pair<>(count, list);
    }

    public int queryForCount() {
        Cursor cursor = databaseHelper.queryForCount();
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public int deleteRows(int startIndex, int endIndex) {
        return databaseHelper.deleteRows(startIndex, endIndex);
    }

    public int deleteFromBottom() {
        return databaseHelper.deleteFromBottom();
    }

    public Map<String, Object> getMapFromString(String jsonString) {
        Map<String, Object> map = new HashMap<>();

        if (TextUtils.isEmpty(jsonString)) {
            return map;
        }

        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                map.put(key, jsonObject.get(key));
            }
        } catch (JSONException e) {
            Logger.print(e);
        }

        return map;
    }

    public boolean needToFlush(Pair<Integer, List<Track>> query) {
        int count = query.first;
        List<Track> data = query.second;

        return count > 0 && (data != null && !data.isEmpty());
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

    public boolean isEndPointAlive() {
        Logger.i(TAG, "ping start");
        try {
            requestManager.request(new RequestManager.Request<ResponseBody>() {
                @Override
                public ResponseBody performRequest() throws Exception {
                    RequestClient client = requestManager.getClient(RequestClient.class);
                    return client.ping().clone().execute().body();
                }
            });
            Logger.d(TAG, "ping success");
            Logger.i(TAG, "ping end");
            return true;
        } catch (Exception retrofitError) {
            Logger.print(retrofitError);
            Logger.d(TAG, "ping fail");
            Logger.i(TAG, "ping end");
            return false;
        }
    }

    public ResponseBody flush(boolean retry, int num, String deviceId, long lastDate, List<Track> data, String version)
            throws Exception {
        RequestManager.Request<ResponseBody> request = getRequest(num, deviceId, lastDate, data, version);
        if (retry) {
            return requestManager.requestWithRetry(request);
        }
        return requestManager.request(request);
    }

    private RequestManager.Request<ResponseBody> getRequest(
            final int num, final String deviceId, final long lastDate, final List<Track> data, final String version) {

        return new RequestManager.Request<ResponseBody>() {
            @Override
            public ResponseBody performRequest() throws IOException {
                RequestBody body = new RequestBody(num, deviceId, lastDate, data, version);
                RequestClient client = requestManager.getClient(RequestClient.class);
                ResponseBody response = client.post(body).clone().execute().body();
                return response;
            }
        };
    }

    public void stopRequest() {
        requestManager.stop();
    }
}

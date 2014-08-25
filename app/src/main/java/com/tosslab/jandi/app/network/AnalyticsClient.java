package com.tosslab.jandi.app.network;

import android.content.Context;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.tosslab.jandi.app.JandiConstants;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by justinygchoi on 2014. 8. 22..
 */
public class AnalyticsClient {
    private final String PROP_SIGN_IN           = "Sign In";
    private final String PROP_CREATE_ENTITY     = "Entity Create";
    private final String PROP_DELETE_ENTITY     = "Entity Delete";
    private final String PROP_CHANGE_ENTITY_NAME    = "Entity Name Change";
    private final String PROP_LEAVE_ENTITY          = "Entity Leave";
    private final String PROP_INVITE_ENTITY         = "Entity Invite";
    private final String PROP_JOIN_CHANNEL          = "Channel Join";
    private final String PROP_UPLOAD_FILE           = "File Upload";
    private final String PROP_SHARE_FILE            = "File Share";
    private final String PROP_UNSHARE_FILE          = "File Unshare";
    private final String PROP_DOWNLOAD_FILE         = "File Download";

    private MixpanelAPI mMixpanel;
    private String mDistictId;
    private static AnalyticsClient __instance__;

    public Context context;

    public AnalyticsClient(Context context, String distictId) {
        mMixpanel = MixpanelAPI.getInstance(context, JandiConstants.MIXPANEL_TOKEN);
        mDistictId = distictId;
        mMixpanel.identify(mDistictId);
    }

    public static AnalyticsClient getInstance(Context context, String distictId) {
        if (__instance__ == null || __instance__.context != context) {
            __instance__ = new AnalyticsClient(context, distictId);
        }
        return __instance__;
    }

    public void trackSingingIn() {
        mMixpanel.track(PROP_SIGN_IN, null);
    }

    public void trackCreatingEntity(boolean isChannel) throws JSONException {
        mMixpanel.track(PROP_CREATE_ENTITY, getEntityTypeProperty(isChannel));
    }

    public void trackDeletingEntity(boolean isChannel) throws JSONException {
        mMixpanel.track(PROP_DELETE_ENTITY, getEntityTypeProperty(isChannel));
    }

    public void trackChangingEntityName(boolean isChannel) throws JSONException {
        mMixpanel.track(PROP_CHANGE_ENTITY_NAME, getEntityTypeProperty(isChannel));
    }

    public void trackLeavingEntity(boolean isChannel) throws JSONException {
        mMixpanel.track(PROP_LEAVE_ENTITY, getEntityTypeProperty(isChannel));
    }

    public void trackInvitingToEntity(boolean isChannel) throws JSONException {
        mMixpanel.track(PROP_INVITE_ENTITY, getEntityTypeProperty(isChannel));
    }

    public void trackJoinChannel() {
        mMixpanel.track(PROP_JOIN_CHANNEL, null);
    }

    private JSONObject getEntityTypeProperty(boolean isChannel) throws JSONException {
        JSONObject props = new JSONObject();
        props.put("type", (isChannel)?"channel":"private");
        return props;
    }

    public void flush() {
        mMixpanel.flush();
    }
}

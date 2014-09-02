package com.tosslab.jandi.app.network;

import android.content.Context;

import com.google.gson.JsonObject;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by justinygchoi on 2014. 8. 22..
 */
public class AnalyticsClient {
    private final Logger log = Logger.getLogger(AnalyticsClient.class);

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
    private final String PROP_PROFILE               = "Set Profile";

    private final String KEY_CHANNEL        = "channel";
    private final String KEY_PRIVATE_GROUP  = "private";
    private final String KEY_DIRECT_MESSAGE = "direct message";

    private MixpanelAPI mMixpanel;
    private String mDistictId;
    private static AnalyticsClient __instance__;

    public Context context;

    public AnalyticsClient(Context context, String distictId) {
        log.debug("Create instance of AnalyticsClient");
        String mixpanelId = context.getString(R.string.jandi_mixpanel_track_id);
        mMixpanel = MixpanelAPI.getInstance(context, mixpanelId);
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

    public void trackUploadingFile(int entityType, JsonObject uploadedFileInfo)
            throws JSONException {
        JsonObject fileInfo = uploadedFileInfo.get("fileInfo").getAsJsonObject();
        String mimeType = fileInfo.get("type").getAsString();
        String extension = fileInfo.get("ext").getAsString();
        long fileSize = fileInfo.get("size").getAsLong();

        JSONObject props = new JSONObject();
        props.put("entity type", convertEntityTypeToString(entityType));
        props.put("category", getMimeTypeCategory(mimeType));
        props.put("extension", extension);
        props.put("mime type", mimeType);
        props.put("size", fileSize);
        mMixpanel.track(PROP_UPLOAD_FILE, props);
    }

    public void trackDownloadFile(ResMessages.FileMessage downloadFileInfo)
            throws JSONException {
        String mimeType = downloadFileInfo.content.type;
        String extension = downloadFileInfo.content.ext;

        JSONObject props = new JSONObject();
        props.put("category", getMimeTypeCategory(mimeType));
        props.put("extension", extension);
        props.put("mime type", mimeType);
        props.put("size", downloadFileInfo.content.size);
        mMixpanel.track(PROP_DOWNLOAD_FILE, props);
    }

    public void trackSharingFile(int entityType, ResMessages.FileMessage fileInfo)
            throws JSONException {
        String mimeType = fileInfo.content.type;
        String extension = fileInfo.content.ext;

        JSONObject props = new JSONObject();
        props.put("entity type", convertEntityTypeToString(entityType));
        props.put("category", getMimeTypeCategory(mimeType));
        props.put("extension", extension);
        props.put("mime type", mimeType);
        props.put("size", fileInfo.content.size);
        mMixpanel.track(PROP_SHARE_FILE, props);
    }

    public void trackUnsharingFile(int entityType, ResMessages.FileMessage fileInfo)
            throws JSONException {
        String mimeType = fileInfo.content.type;
        String extension = fileInfo.content.ext;

        JSONObject props = new JSONObject();
        props.put("entity type", convertEntityTypeToString(entityType));
        props.put("category", getMimeTypeCategory(mimeType));
        props.put("extension", extension);
        props.put("mime type", mimeType);
        props.put("size", fileInfo.content.size);
        mMixpanel.track(PROP_UNSHARE_FILE, props);
    }

    public void trackProfile(ResLeftSideMenu.User updatedMyProfile)
            throws JSONException {
        String mobile = "";
        String division = "";
        String position = "";
        ResLeftSideMenu.ExtraData extraData = updatedMyProfile.u_extraData;
        if (extraData != null) {
            mobile = (extraData.phoneNumber != null) ? extraData.phoneNumber : "";
            division = (extraData.department != null) ? extraData.department : "";
            position = (extraData.position != null) ? extraData.position : "";
        }

        mMixpanel.getPeople().identify(mDistictId);
        JSONObject profile = new JSONObject();
        profile.put("nickname", updatedMyProfile.u_nickname);
        profile.put("mobile", mobile);
        profile.put("division", division);
        profile.put("position", position);
        mMixpanel.getPeople().set(profile);

        JSONObject props = new JSONObject();
        mMixpanel.track(PROP_PROFILE, props);

    }

    private String getMimeTypeCategory(String mimeType) {
        return mimeType.substring(0, mimeType.indexOf("/"));
    }

    private String convertEntityTypeToString(int entityType) {
        switch (entityType) {
            case JandiConstants.TYPE_CHANNEL:
                return KEY_CHANNEL;
            case JandiConstants.TYPE_DIRECT_MESSAGE:
                return KEY_DIRECT_MESSAGE;
            case JandiConstants.TYPE_PRIVATE_GROUP:
            default:
                return KEY_PRIVATE_GROUP;
        }
    }

    private JSONObject getEntityTypeProperty(boolean isChannel) throws JSONException {
        JSONObject props = new JSONObject();
        props.put("type", (isChannel) ? KEY_CHANNEL : KEY_PRIVATE_GROUP);
        return props;
    }

    public void flush() {
        mMixpanel.flush();
    }
}

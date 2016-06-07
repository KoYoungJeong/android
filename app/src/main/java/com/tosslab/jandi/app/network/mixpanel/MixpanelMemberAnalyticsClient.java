package com.tosslab.jandi.app.network.mixpanel;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.JsonObject;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by justinygchoi on 2014. 8. 22..
 */
public class MixpanelMemberAnalyticsClient {
    private static final String PROP_PAGE_VIEW = "Page Viewed";
    private static final String PROP_SIGN_IN = "Sign In";
    private static final String PROP_SIGN_UP = "Sign Up";
    private static final String PROP_SIGN_OUT = "Sign Out";
    private static final String PROP_CREATE_ENTITY = "Chat Create";
    private static final String PROP_DELETE_ENTITY = "Chat Delete";
    private static final String PROP_CHANGE_ENTITY_NAME = "Chat Name Change";
    private static final String PROP_LEAVE_ENTITY = "Chat Leave";
    private static final String PROP_INVITE_ENTITY = "Chat Invite";
    private static final String PROP_JOIN_CHANNEL = "Topic Join";
    private static final String PROP_UPLOAD_FILE = "File Upload";
    private static final String PROP_SHARE_FILE = "File Share";
    private static final String PROP_UNSHARE_FILE = "File Unshare";
    private static final String PROP_DOWNLOAD_FILE = "File Download";
    private static final String PROP_PROFILE = "Set Profile";
    private static final String PROP_INVITE_TEAM = "User Invitation";
    private static final String PROP_TEAM_CHANGE = "Team Switch";
    private static final String KEY_CHANNEL = "public topic";
    private static final String KEY_PRIVATE_GROUP = "private topic";
    private static final String KEY_DIRECT_MESSAGE = "direct message";
    //    private static MixpanelMemberAnalyticsClient __instance__;
    public Context context;
    private MixpanelAPI memberMixpanel;
    private String mDistictId;

    private MixpanelMemberAnalyticsClient(Context context, String distictId) {
        LogUtil.d("Create instance of MixpanelAnalyticsClient");
        String memberMixpanelId = JandiConstantsForFlavors.MIXPANEL_MEMBER_TRACK_ID;

        memberMixpanel = MixpanelAPI.getInstance(context, memberMixpanelId);
        mDistictId = distictId;
        if (!TextUtils.isEmpty(mDistictId)) {
            memberMixpanel.identify(mDistictId);
        }
    }

    public static MixpanelMemberAnalyticsClient getInstance(Context context, String distictId) {
//        if (__instance__ == null || __instance__.context != context) {
//            __instance__ = new MixpanelMemberAnalyticsClient(context, distictId);
//        }

//        __instance__.log.debug("DistictId : " + __instance__.memberMixpanel.getDistinctId());

        return new MixpanelMemberAnalyticsClient(context, distictId);
    }

    public void trackMemberSingingIn() {
        memberMixpanel.track(PROP_SIGN_IN, null);
    }

    public void trackCreatingEntity(boolean isChannel) throws JSONException {
        memberMixpanel.track(PROP_CREATE_ENTITY, getEntityTypeProperty(isChannel));
    }

    public void trackDeletingEntity(boolean isChannel) throws JSONException {
        memberMixpanel.track(PROP_DELETE_ENTITY, getEntityTypeProperty(isChannel));
    }

    public void trackChangingEntityName(boolean isChannel) throws JSONException {
        memberMixpanel.track(PROP_CHANGE_ENTITY_NAME, getEntityTypeProperty(isChannel));
    }

    public void trackLeavingEntity(boolean isChannel) throws JSONException {
        memberMixpanel.track(PROP_LEAVE_ENTITY, getEntityTypeProperty(isChannel));
    }

    public void trackInvitingToEntity(boolean isChannel) throws JSONException {
        memberMixpanel.track(PROP_INVITE_ENTITY, getEntityTypeProperty(isChannel));
    }

    public void trackJoinChannel() {
        memberMixpanel.track(PROP_JOIN_CHANNEL, null);
    }

    public MixpanelMemberAnalyticsClient trackSignOut() {
        memberMixpanel.track(PROP_SIGN_OUT, null);
        return this;
    }

    public void trackTeamInvitation() {
        memberMixpanel.track(PROP_INVITE_TEAM, null);
        memberMixpanel.getPeople().increment("invite", 1);
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
        memberMixpanel.track(PROP_UPLOAD_FILE, props);
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
        memberMixpanel.track(PROP_DOWNLOAD_FILE, props);
    }

    public void trackDownloadFile(String type, String ext, int size)
            throws JSONException {
        String mimeType = type;
        String extension = ext;

        JSONObject props = new JSONObject();
        props.put("category", getMimeTypeCategory(mimeType));
        props.put("extension", extension);
        props.put("mime type", mimeType);
        props.put("size", size);
        memberMixpanel.track(PROP_DOWNLOAD_FILE, props);
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
        memberMixpanel.track(PROP_SHARE_FILE, props);
    }

    private String getMimeTypeCategory(String mimeType) {
        return mimeType.substring(0, mimeType.indexOf("/"));
    }

    private String convertEntityTypeToString(int entityType) {
        switch (entityType) {
            case JandiConstants.TYPE_PUBLIC_TOPIC:
                return KEY_CHANNEL;
            case JandiConstants.TYPE_DIRECT_MESSAGE:
                return KEY_DIRECT_MESSAGE;
            case JandiConstants.TYPE_PRIVATE_TOPIC:
            default:
                return KEY_PRIVATE_GROUP;
        }
    }

    private JSONObject getEntityTypeProperty(boolean isChannel) throws JSONException {
        JSONObject props = new JSONObject();
        props.put("type", (isChannel) ? KEY_CHANNEL : KEY_PRIVATE_GROUP);
        return props;
    }

    public void pageViewTeamCreate() {
        try {
            JSONObject properties = new JSONObject();
            properties.put("page", "Android/team_create");
            memberMixpanel.track(PROP_PAGE_VIEW, properties);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void pageViewTeamCreateSuccess() {
        try {
            JSONObject properties = new JSONObject();
            properties.put("page", "Android/team_create/success");
            memberMixpanel.track(PROP_PAGE_VIEW, properties);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void pageViewMemberCreate() {
        try {
            JSONObject properties = new JSONObject();
            properties.put("page", "Android/member_create");
            memberMixpanel.track(PROP_PAGE_VIEW, properties);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void pageViewMemberCreateSuccess() {
        try {
            JSONObject properties = new JSONObject();
            properties.put("page", "Android/member_create/success");
            memberMixpanel.track(PROP_PAGE_VIEW, properties);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public MixpanelMemberAnalyticsClient flush() {
        memberMixpanel.flush();
        return this;
    }

    public MixpanelMemberAnalyticsClient clear() {
        memberMixpanel.reset();
        return this;
    }

    public MixpanelMemberAnalyticsClient setNewIdentify(String newIdentify) {
        mDistictId = newIdentify;
        String distinctId = memberMixpanel.getDistinctId();

        LogUtil.d("Old Id : " + distinctId + " , new Id : " + newIdentify);

        memberMixpanel.alias(newIdentify, distinctId);
        memberMixpanel.identify(newIdentify);
        return this;
    }

    public void trackSignUp() {
        memberMixpanel.track(PROP_SIGN_UP, null);
    }
}

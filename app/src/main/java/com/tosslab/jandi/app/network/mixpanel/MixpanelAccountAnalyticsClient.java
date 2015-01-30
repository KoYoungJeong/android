package com.tosslab.jandi.app.network.mixpanel;

import android.content.Context;
import android.text.TextUtils;

import com.mixpanel.android.mpmetrics.MPConfig;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.tosslab.jandi.app.JandiConstantsForFlavors;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by justinygchoi on 2014. 8. 22..
 */
public class MixpanelAccountAnalyticsClient {
    private static final Logger log = Logger.getLogger(MixpanelAccountAnalyticsClient.class);
    private static final String PROP_SIGN_IN = "Sign In";
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
    private static final String PROP_PAGE_VIEW = "Page Viewed";
    private static final String PROP_SET_ACCOUNT = "Set Account";
    private static final String KEY_CHANNEL = "public topic";
    private static final String KEY_PRIVATE_GROUP = "private topic";
    private static final String KEY_DIRECT_MESSAGE = "direct message";

    private static MixpanelAccountAnalyticsClient __instance__;
    public Context context;
    private MixpanelAPI accountMixpanel;
    private String mDistictId;

    public MixpanelAccountAnalyticsClient(Context context, String accountId) {
        log.debug("Create instance of MixpanelAnalyticsClient");
        String accountMixpanelId = JandiConstantsForFlavors.MIXPANEL_ACCOUNT_TRACK_ID;

        accountMixpanel = MixpanelAPI.getInstance(context, accountMixpanelId);
        mDistictId = getIdentityId(accountId);
        if (!TextUtils.isEmpty(mDistictId)) {
            accountMixpanel.identify(mDistictId);
        }
    }

    private static String getIdentityId(String accountId) {
        return "account_" + accountId;
    }

    public static MixpanelAccountAnalyticsClient getInstance(Context context, String accountId) {
        if (__instance__ == null || __instance__.context != context) {
            __instance__ = new MixpanelAccountAnalyticsClient(context, accountId);
        }


        if (__instance__.mDistictId == null) {
            __instance__.mDistictId = getIdentityId(accountId);
        }

        return __instance__;
    }

    public MixpanelAccountAnalyticsClient trackAccountSingingIn() {
        accountMixpanel.track(PROP_SIGN_IN, null);
        return this;
    }

    public MixpanelAccountAnalyticsClient trackAccountSigningOut() {
        accountMixpanel.track(PROP_SIGN_OUT, null);
        return this;
    }

    public void pageViewAccountCreate() {
        try {
            JSONObject properties = new JSONObject();
            properties.put("page", "Android/account_create");
            accountMixpanel.track(PROP_PAGE_VIEW, properties);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void pageViewAccountCreateSuccess() {
        try {
            JSONObject properties = new JSONObject();
            properties.put("page", "Android/account_create/success");
            accountMixpanel.track(PROP_PAGE_VIEW, properties);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public MixpanelAccountAnalyticsClient flush() {
        accountMixpanel.flush();
        return this;
    }

    public MixpanelAccountAnalyticsClient clear() {
        accountMixpanel.reset();
        return this;
    }

    public void trackSetAccount() {
        accountMixpanel.track(PROP_SET_ACCOUNT, null);
    }
}

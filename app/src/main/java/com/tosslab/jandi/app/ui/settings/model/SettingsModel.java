package com.tosslab.jandi.app.ui.settings.model;

import android.content.pm.ActivityInfo;
import android.text.TextUtils;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.utils.ApplicationUtil;
import com.tosslab.jandi.app.utils.LanguageUtil;

public class SettingsModel {

    private static final String SUPPORT_URL_KO = "https://jandi.zendesk.com/hc/ko";
    private static final String SUPPORT_URL_JA = "https://jandi.zendesk.com/hc/ja";
    private static final String SUPPORT_URL_ZH_CN = "https://jandi.zendesk.com/hc/zh-cn";
    private static final String SUPPORT_URL_ZH_TW = "https://jandi.zendesk.com/hc/zh-tw";
    private static final String SUPPORT_URL_EN = "https://jandi.zendesk.com/hc/en-us";

    public static int getOrientationValue(String value) {

        if (TextUtils.isEmpty(value) || TextUtils.equals(value, "0")) {
            // 자동
            return ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        } else if (TextUtils.equals(value, "1")) {
            // 세로 고정
            return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        } else if (TextUtils.equals(value, "2")) {
            return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        }
        return ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
    }

    public static int getOrientationSummary(String value) {
        if (TextUtils.isEmpty(value) || TextUtils.equals(value, "0")) {
            // 자동
            return R.string.jandi_screen_auto;
        } else if (TextUtils.equals(value, "1")) {
            // 세로 고정
            return R.string.jandi_screen_vertical;
        } else if (TextUtils.equals(value, "2")) {
            return R.string.jandi_screen_horizontal;
        }
        return R.string.jandi_screen_auto;
    }

    public static int getPushPreviewSummary(String value) {
        if (TextUtils.isEmpty(value) || TextUtils.equals(value, "0")) {
            // 자동
            return R.string.jandi_push_preview_all_message;
        } else if (TextUtils.equals(value, "1")) {
            // 세로 고정
            return R.string.jandi_push_preview_public_only;
        } else if (TextUtils.equals(value, "2")) {
            return R.string.jandi_push_no_preview;
        }
        return R.string.jandi_push_preview_all_message;
    }

    public static String getVersionName() {
        return ApplicationUtil.getAppVersionName();
    }

    public static String getSupportUrl() {
        String currentLanguage = LanguageUtil.getLanguage();
        if (TextUtils.equals(currentLanguage, LanguageUtil.LANG_KO)) {
            return SUPPORT_URL_KO;
        } else if (TextUtils.equals(currentLanguage, LanguageUtil.LANG_JA)) {
            return SUPPORT_URL_EN; //일본어 컨텐츠가 없어서 영어버전 사용
        } else if (TextUtils.equals(currentLanguage, LanguageUtil.LANG_ZH_CN)) {
            return SUPPORT_URL_ZH_CN;
        } else if (TextUtils.equals(currentLanguage, LanguageUtil.LANG_ZH_TW)) {
            return SUPPORT_URL_ZH_TW;
        }

        return SUPPORT_URL_EN;
    }
}

package com.tosslab.jandi.app.utils;

import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;

import java.util.Locale;

/**
 * Created by Steve SeongUg Jung on 14. 12. 25..
 */
public class LanguageUtil {

    public static final String LANG_EN = "en";
    public static final String LANG_JA = "ja";
    public static final String LANG_ZH_CN = "zh-cn";
    public static final String LANG_ZH_TW = "zh-tw";
    public static final String LANG_KO = "ko";
    private static final String[] LANGS = {LANG_EN, LANG_JA, LANG_KO, LANG_ZH_CN, LANG_ZH_TW};

    public static String getLanguage() {

        Locale locale = JandiApplication.getContext().getResources().getConfiguration().locale;

        if (locale == null) {
            return LANG_EN;
        }

        String language = locale.getLanguage();
        String country = locale.getCountry();

        boolean languageEmpty = TextUtils.isEmpty(language);
        boolean countryEmpty = TextUtils.isEmpty(country);

        if (languageEmpty) {
            return LANG_EN;
        }

        if (countryEmpty) {
            return language;
        }

        StringBuffer buffer = new StringBuffer();
        buffer.append(language);//.append("-").append(country);

        if (language.startsWith("zh")) {
            buffer.append("-").append(country);
        }

        String langCode = buffer.toString().toLowerCase();

        if (includeLangCode(langCode)) {
            return langCode;
        } else {
            return LANG_EN;
        }

    }

    private static boolean includeLangCode(String langCode) {

        for (String lang : LANGS) {
            if (TextUtils.equals(lang, langCode)) {
                return true;
            }
        }

        return false;
    }
}

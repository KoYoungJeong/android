package com.tosslab.jandi.app.utils;

import android.content.Context;
import android.text.TextUtils;

import java.util.Locale;

/**
 * Created by Steve SeongUg Jung on 14. 12. 25..
 */
public class LanguageUtil {

    private static final String[] LANGS = {"en", "ja", "ko", "zh-cn", "zh-tw"};

    public static String getLanguage(Context context) {

        if (context == null) {
            return "en";
        }

        Locale locale = context.getResources().getConfiguration().locale;

        if (locale == null) {
            return "en";
        }

        String language = locale.getLanguage();
        String country = locale.getCountry();

        boolean languageEmpty = TextUtils.isEmpty(language);
        boolean countryEmpty = TextUtils.isEmpty(country);

        if (languageEmpty) {
            return "en";
        }

        if (countryEmpty) {
            return language;
        }

        StringBuffer buffer = new StringBuffer();
        buffer.append(language);//.append("-").append(country);

        if (language.startsWith("zh")) {
            buffer.append("-").append(country);
        }

        String langCode = buffer.toString();

        if (includeLangCode(langCode)) {
            return langCode;
        } else {
            return "en";
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

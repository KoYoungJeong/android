package com.tosslab.jandi.app.utils;

import android.content.Context;
import android.text.TextUtils;

import java.util.Locale;

/**
 * Created by Steve SeongUg Jung on 14. 12. 25..
 */
public class LanguageUtil {

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

        return buffer.toString();
    }
}

package com.tosslab.jandi.app.utils;


import android.os.Build;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static java.lang.Character.UnicodeBlock;

public class FirstCharacterUtil {

    private static final List<Character> HANGUL = Arrays.asList('ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ');
    private static final int HANGUL_FIRST_1 = 21 * 28;
    private static final int HANGUL_FIRST_2 = 44032;

    public static final int TYPE_ENGLISH = 0;
    public static final int TYPE_HANGUL = 1;
    public static final int TYPE_CHINESE = 2;
    public static final int TYPE_JAPANESE = 3;
    public static final int TYPE_ETC = 4;

    public static int getLocale(char text) {

        UnicodeBlock unicodeBlock = UnicodeBlock.of(text);

        if (isHangul(unicodeBlock)) {
            return TYPE_HANGUL;
        }
        if (isChinese(unicodeBlock)) {
            return TYPE_CHINESE;
        }

        if (isJapanese(unicodeBlock)) {

            return TYPE_JAPANESE;
        }

        if (isEnglish(text)) {
            return TYPE_ENGLISH;
        }

        return TYPE_ETC;
    }

    public static boolean isEnglish(char text) {
        return (0x61 <= text && text <= 0x7A) || (0x41 <= text && text <= 0x5A);
    }

    private static boolean isJapanese(UnicodeBlock unicodeBlock) {
        return UnicodeBlock.HIRAGANA.equals(unicodeBlock) ||
                UnicodeBlock.KATAKANA.equals(unicodeBlock) ||
                UnicodeBlock.KATAKANA_PHONETIC_EXTENSIONS.equals(unicodeBlock);
    }

    private static String getJapaneseFirstCharacter(char text) {
        UnicodeBlock unicodeBlock = UnicodeBlock.of(text);
        if (unicodeBlock == UnicodeBlock.HIRAGANA) {
            return String.valueOf(text);
        } else if (unicodeBlock == UnicodeBlock.KATAKANA){
            return String.valueOf(((char) (text - 96)));
        } else {
            return String.valueOf(text);
        }
    }

    private static boolean isChinese(UnicodeBlock unicodeBlock) {
        return UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS.equals(unicodeBlock) ||
                UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A.equals(unicodeBlock) ||
                UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B.equals(unicodeBlock) ||
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT &&
                        UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C.equals(unicodeBlock) ||
                        UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D.equals(unicodeBlock)) ||
                UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS.equals(unicodeBlock) ||
                UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT.equals(unicodeBlock);
    }

    private static boolean isHangul(UnicodeBlock unicodeBlock) {
        return UnicodeBlock.HANGUL_SYLLABLES.equals(unicodeBlock) ||
                UnicodeBlock.HANGUL_COMPATIBILITY_JAMO.equals(unicodeBlock) ||
                UnicodeBlock.HANGUL_JAMO.equals(unicodeBlock);
    }

    private static String getHangulFirstCharacter(char text) {

        if (HANGUL.contains(text)) {
            return String.valueOf(text);
        }

        int location = (text - HANGUL_FIRST_2) / HANGUL_FIRST_1;
        if (location >= 0 && location < HANGUL.size()) {
            return String.valueOf(HANGUL.get(location));
        } else {
            return "";
        }
    }

    public static String firstCharacter(String text) {

        if (TextUtils.isEmpty(text)) {
            return "";
        }

        String temp = text.toLowerCase();
        int locale = getLocale(temp.charAt(0));

        switch (locale) {
            case TYPE_HANGUL:
                return getHangulFirstCharacter(temp.charAt(0));
            case TYPE_JAPANESE:
                return getJapaneseFirstCharacter(temp.charAt(0));
            case TYPE_CHINESE:
                return String.valueOf(temp.charAt(0));
            case TYPE_ENGLISH:
                return String.valueOf(temp.charAt(0)).toUpperCase();
            default:
                return "#";
        }
    }

    public static int getLocaleType() {
        Locale locale = Locale.getDefault();

        switch (locale.getLanguage().toLowerCase()) {
            case "en":
                return TYPE_ENGLISH;
            case "ko":
                return TYPE_HANGUL;
            case "zh":
                return TYPE_CHINESE;
            case "ja":
                return TYPE_JAPANESE;
            default:
                return TYPE_ETC;
        }
    }
}

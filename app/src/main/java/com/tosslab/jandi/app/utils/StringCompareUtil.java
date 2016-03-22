package com.tosslab.jandi.app.utils;

/**
 * Created by tee on 16. 3. 3..
 */
public class StringCompareUtil {

    public static int compare(String string1, String string2) {
        int end = Math.min(string1.length(), string2.length());
        char c1, c2;
        for (int i = 0; i < end; ++i) {
            c1 = string1.charAt(i);
            c2 = string2.charAt(i);
            if (c1 == c2) {
                continue;
            }

            // 공백
            if (isSpaceCharacter(c1) && !isSpaceCharacter(c2)) {
                return -1;
            } else if (!isSpaceCharacter(c1) && isSpaceCharacter(c2)) {
                return 1;
            } else if (isSpaceCharacter(c1) && isSpaceCharacter(c2)) {
                continue;
            }

            // 특수 문자
            if (isSpecialCharacters(c1) && !isSpecialCharacters(c2)) {
                return -1;
            } else if (!isSpecialCharacters(c1) && isSpecialCharacters(c2)) {
                return 1;
            } else if (isSpecialCharacters(c1) && isSpecialCharacters(c2)) {
                return c1 - c2;
            }

            // 숫자
            if (isNumberCharacter(c1) && !isNumberCharacter(c2)) {
                return -1;
            } else if (!isNumberCharacter(c1) && isNumberCharacter(c2)) {
                return 1;
            } else if (isNumberCharacter(c1) && isNumberCharacter(c2)) {
                return c1 - c2;
            }

            // 같은 캐릭터라면 대문자가 먼저 나오도록
            if (getAbsChar(c1) == getAbsChar(c2)) {
                return c1 - c2;
            }

            return getAbsChar(c1) - getAbsChar(c2);
        }
        return 1;
    }

    private static int getAbsChar(char c) {
        if (c >= 'a' && c <= 'z') {
            return c - 32; // 대문자와 소문자의 아스키코드 차이
        }
        return c;
    }

    private static boolean isSpaceCharacter(char c) {
        return Character.isSpaceChar(c);
    }

    private static boolean isSpecialCharacters(char c) {
        if (!Character.isLetterOrDigit(c)
                && !Character.isSpaceChar(c)) {
            return true;
        }
        return false;
    }

    private static boolean isNumberCharacter(char c) {
        return Character.isDigit(c);
    }
}

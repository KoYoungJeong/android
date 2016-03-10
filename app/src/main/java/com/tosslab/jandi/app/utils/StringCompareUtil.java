package com.tosslab.jandi.app.utils;

/**
 * Created by tee on 16. 3. 3..
 */
public class StringCompareUtil {

    public static int compare(String string1, String string2) {
        int end = string1.length() < string2.length() ? string2.length() : string2.length();
        char c1, c2;
        for (int i = 0; i < end; ++i) {
            if ((c1 = string1.charAt(i)) == (c2 = string2.charAt(i))) {
                continue;
            }

            // 공백
            if (isSpaceCharacter(c1) && !isSpaceCharacter(c2)) {
                return 1;
            } else if (!isSpaceCharacter(c1) && isSpaceCharacter(c2)) {
                return -1;
            }

            // 특수 문자
            if (isSpecialCharacters(c1) && !isSpecialCharacters(c2)) {
                return 1;
            } else if (!isSpecialCharacters(c1) && isSpecialCharacters(c2)) {
                return -1;
            }

            // 숫자
            if (isNumberCharacter(c1) && !isNumberCharacter(c2)) {
                return 1;
            } else if (!isNumberCharacter(c1) && isNumberCharacter(c2)) {
                return -1;
            }

            return c1 - c2;
        }
        return 1;
    }

    private static boolean isSpaceCharacter(char c) {
        if (c == ' ') {
            return true;
        } else {
            return false;
        }
    }

    private static boolean isSpecialCharacters(char c) {
        if ((c >= '!' && c <= '/') // 아스키 코드 33 ~ 47
                || (c >= ':' && c <= '@') // 58 ~ 64
                || (c >= '[' && c <= '\'') // 91 ~ 96
                || (c >= '{' && c <= '}') // 123 ~ 125
                ) {
            return true;
        }
        return false;
    }

    private static boolean isNumberCharacter(char c) {
        if (c >= '0' && c <= '9') {
            return true;
        }
        return false;
    }
}

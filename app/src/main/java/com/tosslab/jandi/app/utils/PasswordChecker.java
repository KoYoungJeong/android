package com.tosslab.jandi.app.utils;

import com.tosslab.jandi.app.R;

/**
 * Created by justinygchoi on 14. 12. 11..
 */
public class PasswordChecker {
    public static final int TOO_SHORT = 0;
    public static final int WEAK = 1;
    public static final int AVERAGE = 2;
    public static final int SAFE = 3;
    public static final int STRONG = 4;

    static final int MINIMUN_LENGTH = 8;

    static final String REG_EX_CONTAIN_LOWER = ".*[a-z]+.*";
    static final String REG_EX_CONTAIN_CAPITAL = ".*[A-Z]+.*";
    static final String REG_EX_CONTAIN_DIGIT = ".*[0-9]+.*";
    // ` ~ ! @ # $ % ^ & * ( ) _ - + = { } [ ] \ | : ; " ' < > , . ? /
    static final String REG_EX_CONTAIN_SPECIAL_CHARACTER = ".*[`~!@#$%^&*()_\\-+={}\\[\\]\\\\|:;\"'<>,.?\\/]+.*";

    static boolean hasLowerLetter(String targetString) {
        return targetString.matches(REG_EX_CONTAIN_LOWER);
    }

    static boolean hasCapitalLetter(String targetString) {
        return targetString.matches(REG_EX_CONTAIN_CAPITAL);
    }

    static boolean hasDigit(String targetString) {
        return targetString.matches(REG_EX_CONTAIN_DIGIT);
    }

    static boolean hasSpecialCharacter(String targetString) {
        return targetString.matches(REG_EX_CONTAIN_SPECIAL_CHARACTER);
    }

    public static int checkStrength(String password) {
        int strength = TOO_SHORT;
        if (password.length() < MINIMUN_LENGTH) {
            return strength;
        }

        if (hasLowerLetter(password))
            strength++;
        if (hasCapitalLetter(password))
            strength++;
        if (hasDigit(password))
            strength++;
        if (hasSpecialCharacter(password))
            strength++;

        return strength;
    }

    public static int getBarometerStringRes(int level) {
        switch (level) {
            case PasswordChecker.WEAK:
                return R.string.jandi_password_strength_weak;
            case PasswordChecker.AVERAGE:
                return R.string.jandi_password_strength_average;
            case PasswordChecker.SAFE:
                return R.string.jandi_password_strength_safe;
            case PasswordChecker.STRONG:
                return R.string.jandi_password_strength_strong;
            default:    // TOO_SHORT
                return R.string.jandi_password_strength_too_short;
        }
    }

    public static int getBarometerColorRes(int level) {
        switch (level) {
            case PasswordChecker.AVERAGE:
                return R.color.jandi_password_strength_average;
            case PasswordChecker.SAFE:
                return R.color.jandi_password_strength_safe;
            case PasswordChecker.STRONG:
                return R.color.jandi_password_strength_strong;
            default:    // TOO_SHORT, WEAK
                return R.color.jandi_password_strength_weak;
        }
    }
}

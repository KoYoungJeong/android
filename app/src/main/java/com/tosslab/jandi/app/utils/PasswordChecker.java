package com.tosslab.jandi.app.utils;

/**
 * Created by justinygchoi on 14. 12. 11..
 */
public class PasswordChecker {
    public static final int TOO_SHORT   = 0;
    public static final int WEAK        = 1;
    public static final int AVERAGE     = 2;
    public static final int SAFE        = 3;
    public static final int STRONG      = 4;

    static final int MINIMUN_LENGTH     = 8;

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
}

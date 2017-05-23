package com.tosslab.jandi.app.utils.regex;

import java.util.regex.Pattern;

/**
 * Created by tonyjs on 15. 8. 31..
 */
public class TelRegex {

    // (021234567, 02 123 4567, 02.123.4567, 02-123-4567)
    private static final String PHONE_NUMBER_WITHOUT_SPACE = "(\\d{9,11})";
    private static final String PHONE_NUMBER_WITH_SPACE = "(\\d{2,4}\\s\\d{3,4}\\s\\d{4})";
    private static final String PHONE_NUMBER_WITH_DOT = "(\\d{2,4}\\.\\d{3,4}\\.\\d{4})";
    private static final String PHONE_NUMBER_WITH_DASH = "(\\d{2,4}\\-\\d{3,4}\\-\\d{4})";
    // 국가번호는 1000의 자리까지 존재하고 국가번호가 들어가면 지역번호 앞자리가 생략된다.(+10 ~ +999999)
    private static final String GLOBAL_PHONE_NUMBER_WITHOUT_SPACE = "(\\+\\d{9,14})";
    private static final String GLOBAL_PHONE_NUMBER_WITH_SPACE = "(\\+\\d{2,6}\\s\\d{3,4}\\s\\d{4})|(\\+\\d{1,4}\\s\\d{1,2}\\s\\d{3,4}\\s\\d{4})";
    private static final String GLOBAL_PHONE_NUMBER_WITH_DOT = "(\\+\\d{2,6}\\.\\d{3,4}\\.\\d{4})|(\\+\\d{1,4}\\s\\d{1,2}\\.\\d{3,4}\\.\\d{4})";
    private static final String GLOBAL_PHONE_NUMBER_WITH_DASH = "(\\+\\d{2,6}\\-\\d{3,4}\\-\\d{4})|(\\+\\d{1,4}\\s\\d{1,2}\\-\\d{3,4}\\-\\d{4})";
    public static final String VALID_TEL_PATTERN_STRING = PHONE_NUMBER_WITHOUT_SPACE + "|"
            + PHONE_NUMBER_WITH_SPACE + "|"
            + PHONE_NUMBER_WITH_DOT + "|"
            + PHONE_NUMBER_WITH_DASH + "|"
            + GLOBAL_PHONE_NUMBER_WITHOUT_SPACE + "|"
            + GLOBAL_PHONE_NUMBER_WITH_SPACE + "|"
            + GLOBAL_PHONE_NUMBER_WITH_DOT + "|"
            + GLOBAL_PHONE_NUMBER_WITH_DASH;
    public static final Pattern VALID_TEL_PATTERN = Pattern.compile(
            VALID_TEL_PATTERN_STRING, Pattern.CASE_INSENSITIVE);

}

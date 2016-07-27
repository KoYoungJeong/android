package com.tosslab.jandi.app.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by justinygchoi on 2014. 6. 24..
 */
public class FormatConverter {
    static final String REG_EX_START_WHITE_SPACE = "^\\s";

    static final int MB = 1024 * 1024;
    static final int KB = 1024;

    private static final String[] TYPE_DOCUMENT = {
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-word.document.macroEnabled.12"
    };
    private static final String[] TYPE_PRESENTATION = {
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "application/vnd.openxmlformats-officedocument.presentationml.slideshow",
            "application/vnd.ms-powerpoint.presentation.macroEnabled.12",
            "application/vnd.ms-powerpoint.slideshow.macroEnabled.12"
    };
    private static final String[] TYPE_SPREADSHEET = {
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-excel.sheet.macroEnabled.12"
    };

    public static boolean isInvalidEmailString(String targetString) {
        return !targetString.matches(LinkifyUtil.REG_EX_EMAIL);
    }

    public static boolean isInvalidPasswd(String targetString) {
        return (targetString == null || targetString.isEmpty());
    }

    public static boolean isInvalidString(String targetString) {
        return targetString.matches(REG_EX_START_WHITE_SPACE);
    }

    public static boolean isMsOfficeMimeType(String type) {

        List<String> officeFileTypes = new ArrayList<String>();
        officeFileTypes.addAll(Arrays.asList(TYPE_DOCUMENT));
        officeFileTypes.addAll(Arrays.asList(TYPE_PRESENTATION));
        officeFileTypes.addAll(Arrays.asList(TYPE_SPREADSHEET));

        for (String mimeType : officeFileTypes) {
            if (type.startsWith(mimeType)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isDocmentMimeType(String type) {
        for (String mimeType : TYPE_DOCUMENT) {
            if (type.startsWith(mimeType)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isPresentationMimeType(String type) {
        for (String mimeType : TYPE_PRESENTATION) {
            if (type.startsWith(mimeType)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSpreadSheetMimeType(String type) {
        for (String mimeType : TYPE_SPREADSHEET) {
            if (type.startsWith(mimeType)) {
                return true;
            }
        }
        return false;
    }
}

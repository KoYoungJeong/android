package com.tosslab.jandi.app.utils;

/**
 * Created by justinygchoi on 2014. 6. 24..
 */
public class FormatConverter {
    static final String REG_EX_EMAIL = "^[_a-z0-9-]+(\\.[_a-z0-9-]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)*(\\.[a-z]{2,4})$";
    static final String REG_EX_START_WHITE_SPACE = "^\\s";

    static final int MB = 1024 * 1024;
    static final int KB = 1024;

    public static String formatFileSize(int byteSize) {

        if (byteSize > MB) {
            float f = (float)byteSize / MB;
            return String.format("%.2f MB", f);
        } else if (byteSize > KB) {
            return (byteSize / KB) + " KB";
        } else {
            return byteSize + " bytes";
        }
    }

    public static boolean isInvalidEmailString(String targetString) {
        return !targetString.matches(REG_EX_EMAIL);
    }

    public static boolean isInvalidPasswd(String targetString) {
        return (targetString == null || targetString.isEmpty());
    }

    public static boolean isInvalidString(String targetString) {
        return targetString.matches(REG_EX_START_WHITE_SPACE);
    }

    public static boolean isMsOfficeMimeType(String type) {
        String mineTypes[] = {
                // DOC
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-word.document.macroEnabled.12",
                // XLS
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application/vnd.ms-excel.sheet.macroEnabled.12",
                // PPT
                "application/vnd.ms-powerpoint",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "application/vnd.openxmlformats-officedocument.presentationml.slideshow",
                "application/vnd.ms-powerpoint.presentation.macroEnabled.12",
                "application/vnd.ms-powerpoint.slideshow.macroEnabled.12"
        };

        for (String mimeType : mineTypes) {
            if (type.startsWith(mimeType)) {
                return true;
            }
        }
        return false;
    }
}

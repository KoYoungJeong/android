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
//    public static String cdpName(String originName, int cdpType) {
//        switch (cdpType) {
//            case JandiConstants.TYPE_CHANNEL:
//                return "# " + originName;
//            case JandiConstants.TYPE_DIRECT_MESSAGE:
//                return "@ " + originName;
//            default:
//                return originName;
//        }
//    }
}

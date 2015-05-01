package com.tosslab.jandi.app.utils;

import android.text.TextUtils;

import com.tosslab.jandi.app.R;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Bill Minwook Heo on 15. 4. 14..
 */
public class FileExtensionCheck {


    private enum Extensions {
        IMAGE("jpg", "jpeg", "gif", "bmp", "png", "tif"), VIDEO("avi", "mpg", "mpeg", "wmv", "mp4", "mkv", "asf", "flv"),
        AUDIO("mp3", "wma", "wav", "mid"), PDF("pdf"), TXT("txt"), HWP("hwp"), EXEL("xls", "xlsx"), DOC("doc", "docx"), PPT("ppt", "pptx"), ETC("");
        private final List<String> extensionList;

        private Extensions(String... extensions) {
            extensionList = Arrays.asList(extensions);
        }

        public List<String> getExtensionList() {
            return Collections.unmodifiableList(extensionList);
        }
    }

    public static int fileExtensionCheck(String fileName) {
        String ext = null;
        Extensions[] values = Extensions.values();
        Extensions extValue = null;

        if (!TextUtils.isEmpty(fileName)) {
            int dotPosition = fileName.lastIndexOf(".");

            if (dotPosition > -1) {
                ext = fileName.toLowerCase().substring(dotPosition + 1);
            }
        }

        for (Extensions value : values) {
            for (String fixedExt : value.getExtensionList()) {
                if (TextUtils.equals(ext, fixedExt)) {
                    extValue = value;
                    break;
                }
            }
        }

        if (extValue == null) {
            extValue = Extensions.ETC;
        }

        switch (extValue) {

            case IMAGE:
                return R.drawable.jandi_fl_icon_img;
            case VIDEO:
                return R.drawable.jandi_fl_icon_video;
            case AUDIO:
                return R.drawable.jandi_fl_icon_audio;
            case PDF:
                return R.drawable.jandi_fl_icon_pdf;
            case TXT:
                return R.drawable.jandi_fl_icon_txt;
            case HWP:
                return R.drawable.jandi_fl_icon_hwp;
            case EXEL:
                return R.drawable.jandi_fl_icon_exel;
            case DOC:
                return R.drawable.jandi_fl_icon_txt;
            case PPT:
                return R.drawable.jandi_fl_icon_ppt;
            default:
            case ETC:
                return R.drawable.jandi_fl_icon_etc;

        }
    }
}

package com.tosslab.jandi.app.utils;

import android.text.TextUtils;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Bill Minwook Heo on 15. 4. 14..
 */
public class FileExtensionsUtil {

    public enum Extensions {
        IMAGE("jpg", "jpeg", "gif", "bmp", "png", "tif"),
        VIDEO("avi", "mpg", "mpeg", "wmv", "mp4", "mkv", "asf", "flv"),
        AUDIO("mp3", "wma", "wav", "mid"),
        PDF("pdf"),
        TXT("txt"),
        HWP("hwp"),
        EXEL("xls", "xlsx"),
        DOC("doc", "docx"),
        PPT("ppt", "pptx"),
        ETC("");

        private final List<String> extensionList;

        Extensions(String... extensions) {
            extensionList = Arrays.asList(extensions);
        }

        public List<String> getExtensionList() {
            return Collections.unmodifiableList(extensionList);
        }
    }

    public static int getFileTypeImageResource(String fileName) {
        Extensions extensions = getExtensions(fileName);

        switch (extensions) {
            case IMAGE:
                return R.drawable.file_icon_img;
            case VIDEO:
                return R.drawable.file_icon_video;
            case AUDIO:
                return R.drawable.file_icon_audio;
            case PDF:
                return R.drawable.file_icon_pdf;
            case TXT:
                return R.drawable.file_icon_txt;
            case HWP:
                return R.drawable.file_icon_hwp;
            case EXEL:
                return R.drawable.file_icon_exel;
            case DOC:
                return R.drawable.file_icon_txt;
            case PPT:
                return R.drawable.file_icon_ppt;
            default:
            case ETC:
                return R.drawable.file_icon_etc;
        }
    }
    
    public static Extensions getExtensions(String fileName) {
        Extensions extensions = Extensions.ETC;
        if (TextUtils.isEmpty(fileName)) {
            return extensions;
        }

        for (Extensions value : Extensions.values()) {
            boolean find = false;
            for (String ext : value.extensionList) {
                if (fileName.endsWith(ext)) {
                    LogUtil.d(fileName + " & " + ext);
                    extensions = value;
                    find = true;
                    break;
                }
            }
            if (find) {
                break;
            }
        }

        return extensions;
    }

}

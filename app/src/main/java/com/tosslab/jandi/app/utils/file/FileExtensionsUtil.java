package com.tosslab.jandi.app.utils.file;

import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Bill Minwook Heo on 15. 4. 14..
 */
public class FileExtensionsUtil {

    public static int getFileTypeImageResource(String fileName) {
        Extensions extensions = getExtensions(fileName);

        return getTypeResourceId(extensions);
    }

    public static int getTypeResourceId(Extensions extensions) {
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

    public static String getFileTypeText(String fileName) {
        Extensions extensions = getExtensions(fileName);

        switch (extensions) {
            case IMAGE:
                return JandiApplication.getContext().getString(R.string.jandi_file_category_image);
            case VIDEO:
                return JandiApplication.getContext().getString(R.string.jandi_file_category_video);
            case AUDIO:
                return JandiApplication.getContext().getString(R.string.jandi_file_category_audio);
            case PDF:
                return JandiApplication.getContext().getString(R.string.jandi_file_category_pdf);
            case TXT:
                return "TXT";
            case HWP:
                return JandiApplication.getContext().getString(R.string.jandi_file_category_document);
            case EXEL:
                return JandiApplication.getContext().getString(R.string.jandi_file_category_spreadsheet);
            case DOC:
                return JandiApplication.getContext().getString(R.string.jandi_file_category_document);
            case PPT:
                return JandiApplication.getContext().getString(R.string.jandi_file_category_presentation);
            default:
            case ETC:
                return "ETC";
        }
    }

    public static Extensions getExtensions(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return Extensions.ETC;
        }

        int extSeperatorIndex = fileName.lastIndexOf(".");
        if (extSeperatorIndex == -1) {
            return Extensions.ETC;
        }

        String rawExt = fileName.substring(extSeperatorIndex + 1).toLowerCase();

        for (Extensions value : Extensions.values()) {
            if (value.extensionList.contains(rawExt)) {
                return value;
            }
        }

        return Extensions.ETC;
    }

    public static int getFileTypeBigImageResource(String fileName) {
        Extensions extensions = getExtensions(fileName);

        return getTypeResourceId(extensions);
    }

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

}

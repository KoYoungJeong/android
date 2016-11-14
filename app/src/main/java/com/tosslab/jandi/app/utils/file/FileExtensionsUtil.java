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

    public static int getFileTypeImageResourceForFileExplorer(String fileName) {
        Extensions extensions = getExtensions(fileName);
        return getTypeResourceIdForFileExplorer(extensions);
    }

    public static int getTypeResourceId(Extensions extensions) {
        switch (extensions) {
            case IMAGE:
                return R.drawable.file_detail_img;
            case VIDEO:
                return R.drawable.file_detail_video;
            case AUDIO:
                return R.drawable.file_detail_audio;
            case PDF:
                return R.drawable.file_detail_pdf;
            case TXT:
                return R.drawable.file_detail_text;
            case HWP:
                return R.drawable.file_detail_hwp;
            case EXEL:
                return R.drawable.file_detail_excel;
            case DOC:
                return R.drawable.file_detail_text;
            case PPT:
                return R.drawable.file_detail_ppt;
            case ZIP:
                return R.drawable.file_detail_zip;
            default:
            case ETC:
                return R.drawable.file_detail_etc;
        }
    }

    public static int getTypeResourceIdForFileExplorer(Extensions extensions) {
        switch (extensions) {
            case IMAGE:
                return R.drawable.file_icon_img_135;
            case VIDEO:
                return R.drawable.file_icon_video_135;
            case AUDIO:
                return R.drawable.file_icon_audio_135;
            case PDF:
                return R.drawable.file_icon_pdf_135;
            case TXT:
                return R.drawable.file_icon_text_135;
            case HWP:
                return R.drawable.file_icon_hwp_135;
            case EXEL:
                return R.drawable.file_icon_excel_135;
            case DOC:
                return R.drawable.file_icon_text_135;
            case PPT:
                return R.drawable.file_icon_ppt_135;
            case ZIP:
                return R.drawable.file_icon_zip_135;
            default:
            case ETC:
                return R.drawable.file_icon_etc_135;
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
            case ZIP:
                return JandiApplication.getContext().getString(R.string.jandi_file_category_zip);
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

    // jpg, jpeg, gif, png, webp 만 서포트
    public static boolean shouldSupportImageExtensions(String ext) {
        return !TextUtils.isEmpty(ext)
                &&
                (ext.contains("jpg")
                        || ext.contains("jpeg")
                        || ext.contains("gif")
                        || ext.contains("png")
                        || ext.contains("webp"));
    }

    public static int getFileDetailBackground(Extensions extensions) {
        int color = 0xffa7a7a7;
        switch (extensions) {

            case IMAGE:
                color = 0xff288064;
                break;
            case VIDEO:
                color = 0xff8267c1;
                break;
            case AUDIO:
                color = 0xffff992c;
                break;
            case PDF:
                color = 0xffef5050;
                break;
            case TXT:
                color = 0xff426bb7;
                break;
            case HWP:
                color = 0xff07adad;
                break;
            case EXEL:
                color = 0xff109d57;
                break;
            case DOC:
                color = 0xff426bb7;
                break;
            case PPT:
                color = 0xffed6e3c;
                break;
            case ZIP:
                color = 0xff828282;
                break;
            default:
            case ETC:
                color = 0xffa7a7a7;
        }
        return color;
    }

    public enum Extensions {
        IMAGE("jpg", "jpeg", "gif", "bmp", "png", "tif"),
        VIDEO("avi", "mpg", "mpeg", "wmv", "mp4", "mkv", "asf", "flv", "mov"),
        AUDIO("mp3", "wma", "wav", "mid"),
        PDF("pdf"),
        TXT("txt"),
        HWP("hwp", "hwpx", "hwt", "hml"),
        EXEL("xls", "xlsx", "numbers", "csv", "cell", "xlt", "nxl", "nxt"),
        DOC("doc", "docx", "pages", "rtf", "gui"),
        PPT("ppt", "pptx", "key", "show", "pps", "hpt"),
        ZIP("zip", "zipx", "alz", "rar", "egg", "7z", "tar", "tgz", "tar.gz", "tar.bz2", "tar.z"),
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

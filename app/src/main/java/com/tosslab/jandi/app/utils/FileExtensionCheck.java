package com.tosslab.jandi.app.utils;

import com.tosslab.jandi.app.R;

/**
 * Created by Bill Minwook Heo on 15. 4. 14..
 */
public class FileExtensionCheck {

    public static int fileExtensionCheck(String fileName) {

        String ext = null;
        try {
            int dotPositon = fileName.lastIndexOf(".");
            ext = fileName.toLowerCase().substring(dotPositon + 1);
        } catch (Exception e) {
            return R.drawable.jandi_fview_icon_etc;
        }

        boolean isImageExtension = ext.equals("jpg") || ext.equals("jpeg") || ext.equals("gif") || ext.equals("bmp") || ext.equals("png") || ext.equals("tif");
        boolean isVideoExtension = ext.equals("avi") || ext.equals("mpg") || ext.equals("mpeg") || ext.equals("wmv") || ext.equals("mp4") || ext.equals("mkv")
                || ext.equals("asf") || ext.equals("flv");
        boolean isAudioExtension = ext.equals("mp3") || ext.equals("wma") || ext.equals("wav") || ext.equals("mid");
        boolean isPdfExtension = ext.equals("pdf");
        boolean isTxtExtension = ext.equals("txt");
        boolean isHwpExtension = ext.equals("hwp");
        boolean isExelExtension = ext.equals("xls") || ext.equals("xlsx");
        boolean isDocExtension = ext.equals("doc") || ext.equals("docx");
        boolean isPptExtension = ext.equals("ppt") || ext.equals("pptx");

        if (isImageExtension) {
            return R.drawable.jandi_fview_icon_etc;
        } else if (isVideoExtension) {
            return R.drawable.jandi_fview_icon_video;
        } else if (isAudioExtension) {
            return R.drawable.jandi_fview_icon_audio;
        } else if (isPdfExtension) {
            return R.drawable.jandi_fview_icon_pdf;
        } else if (isTxtExtension) {
            return R.drawable.jandi_fview_icon_txt;
        } else if (isHwpExtension) {
            return R.drawable.jandi_fl_icon_hwp;
        } else if (isExelExtension) {
            return R.drawable.jandi_fl_icon_exel;
        } else if (isDocExtension) {
            return R.drawable.jandi_fview_icon_txt;
        } else if (isPptExtension) {
            return R.drawable.jandi_fview_icon_ppt;
        } else {
            return R.drawable.jandi_fview_icon_etc;
        }

    }
}
